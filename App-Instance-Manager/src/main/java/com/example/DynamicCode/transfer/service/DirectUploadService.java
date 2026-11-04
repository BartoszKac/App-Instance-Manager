package com.example.DynamicCode.transfer.service;

import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.service.RemoteSerwerService;
import com.example.DynamicCode.inspection.service.InspectionHostKeys;
import com.example.DynamicCode.shared.metrics.InspectorWorkGauge;
import com.example.DynamicCode.transfer.dto.DirectUploadCommand;
import com.example.DynamicCode.transfer.dto.DirectUploadResponse;
import com.example.DynamicCode.transfer.entity.DirectUploadRecord;
import com.example.DynamicCode.transfer.exception.DirectUploadException;
import com.example.inspection.grpc.v1.ServerFileTransferGrpc;
import com.example.inspection.grpc.v1.UploadAccepted;
import com.example.inspection.grpc.v1.UploadChunk;
import com.example.inspection.grpc.v1.UploadHeader;
import com.example.inspection.grpc.v1.UploadResult;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectUploadService {

    private static final int CHUNK_BYTES = 64 * 1024;
    private static final long ACCEPT_TIMEOUT_SECONDS = 45;
    private static final long RESULT_TIMEOUT_SECONDS = 180;
    private static final String BUSY = "Mikroserwis jest zajęty lub niedostępny. Spróbuj ponownie.";
    private static final String NO_PASSWORD = "Serwer nie ma zapisanego hasła, więc nie można wysłać pliku.";
    private static final String STOPPED = "Wysyłka przerwana.";
    private static final Map<Status.Code, HttpStatus> HTTP_BY_GRPC = Map.of(
            Status.Code.RESOURCE_EXHAUSTED, HttpStatus.SERVICE_UNAVAILABLE,
            Status.Code.UNAVAILABLE, HttpStatus.SERVICE_UNAVAILABLE,
            Status.Code.ALREADY_EXISTS, HttpStatus.CONFLICT,
            Status.Code.PERMISSION_DENIED, HttpStatus.CONFLICT,
            Status.Code.INVALID_ARGUMENT, HttpStatus.BAD_REQUEST);

    private final RemoteSerwerService servers;
    private final InspectionHostKeys hostKeys;
    private final DirectUploadRecords records;
    private final InspectorWorkGauge workGauge;
    private final ServerFileTransferGrpc.ServerFileTransferStub stub;

    @Value("${aim.transfer.max-active-uploads:32}")
    private int maxActiveUploads;

    public DirectUploadResponse upload(Long serverId, DirectUploadCommand command, InputStream body) {
        RemoteSerwerConfiguration server = servers.getConfigById(serverId);
        if (server.getPass() == null || server.getPass().isEmpty()) {
            throw new DirectUploadException(HttpStatus.BAD_REQUEST, NO_PASSWORD);
        }
        if (workGauge.tryBeginUpload(maxActiveUploads) < 0) {
            throw new DirectUploadException(HttpStatus.SERVICE_UNAVAILABLE, BUSY);
        }
        try {
            return transfer(server, command, body);
        } finally {
            workGauge.endUpload();
        }
    }

    private DirectUploadResponse transfer(RemoteSerwerConfiguration server, DirectUploadCommand command, InputStream body) {
        DirectUploadRecord record = records.start(server.getIdConfiguration(), command);
        UploadBridge bridge = new UploadBridge();
        try {
            stub.upload(bridge);
            bridge.send(UploadChunk.newBuilder().setHeader(toHeader(server, command)).build());
            UploadAccepted accepted = bridge.accepted().get(ACCEPT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            hostKeys.pin(server.getIdConfiguration(), server.getIp(), accepted.getHostKey());
            pump(body, bridge);
            bridge.complete();
            UploadResult result = bridge.result().get(RESULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return DirectUploadResponse.of(records.finish(record, result));
        } catch (ExecutionException e) {
            throw failed(record, bridge, toException(e.getCause()));
        } catch (TimeoutException e) {
            throw failed(record, bridge, new DirectUploadException(HttpStatus.SERVICE_UNAVAILABLE, BUSY));
        } catch (IOException e) {
            throw failed(record, bridge, new DirectUploadException(HttpStatus.BAD_REQUEST, STOPPED));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw failed(record, bridge, new DirectUploadException(HttpStatus.SERVICE_UNAVAILABLE, STOPPED));
        }
    }

    private void pump(InputStream body, UploadBridge bridge) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        byte[] buffer = new byte[CHUNK_BYTES];
        int read = body.read(buffer);
        while (read >= 0) {
            if (!bridge.send(UploadChunk.newBuilder().setData(ByteString.copyFrom(buffer, 0, read)).build())) {
                bridge.result().get(1, TimeUnit.SECONDS);
                return;
            }
            read = body.read(buffer);
        }
    }

    private DirectUploadException failed(DirectUploadRecord record, UploadBridge bridge, DirectUploadException exception) {
        bridge.cancel();
        records.fail(record, exception.getMessage());
        log.info("Direct upload {} failed: {}", record.getId(), exception.getStatus());
        return exception;
    }

    private DirectUploadException toException(Throwable cause) {
        Status status = Status.fromThrowable(cause);
        HttpStatus http = HTTP_BY_GRPC.getOrDefault(status.getCode(), HttpStatus.BAD_GATEWAY);
        boolean useDescription = status.getDescription() != null && http != HttpStatus.SERVICE_UNAVAILABLE;
        return new DirectUploadException(http, useDescription ? status.getDescription() : BUSY);
    }

    private UploadHeader toHeader(RemoteSerwerConfiguration server, DirectUploadCommand command) {
        return UploadHeader.newBuilder()
                .setHost(server.getIp())
                .setUser(server.getUser())
                .setPassword(server.getPass())
                .setPinnedHostKey(hostKeys.pinnedFor(server.getIdConfiguration(), server.getIp()))
                .setDestinationDir(command.destination())
                .setRelativePath(command.relativePath())
                .setOverwrite(command.overwrite())
                .setExpectedSize(command.expectedSize())
                .build();
    }
}
