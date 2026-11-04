package com.example.inspector.transfer;

import com.example.inspection.grpc.v1.UploadAccepted;
import com.example.inspection.grpc.v1.UploadChunk;
import com.example.inspection.grpc.v1.UploadEvent;
import com.example.inspection.grpc.v1.UploadHeader;
import com.example.inspector.inspection.InspectionFailure;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
final class UploadCall implements StreamObserver<UploadChunk> {

    private static final Status FULL = Status.RESOURCE_EXHAUSTED.withDescription("Ta instancja ma komplet wysyłek.");
    private static final Status IDLE = Status.DEADLINE_EXCEEDED.withDescription("Wysyłka stoi zbyt długo. Wyślij plik ponownie.");
    private static final Status ABORTED = Status.CANCELLED.withDescription("Wysyłka przerwana.");
    private static final Status NO_HEADER = Status.INVALID_ARGUMENT.withDescription("Brak nagłówka wysyłki.");
    private static final Item END = new Item(null);
    private static final Item ABORT = new Item(null);

    private final ServerCallStreamObserver<UploadEvent> response;
    private final SftpUploader uploader;
    private final Semaphore capacity;
    private final Executor workers;
    private final Duration idleTimeout;
    private final BlockingQueue<Item> items = new LinkedBlockingQueue<>();
    private boolean started;

    UploadCall(ServerCallStreamObserver<UploadEvent> response, SftpUploader uploader, Semaphore capacity, Executor workers, Duration idleTimeout) {
        this.response = response;
        this.uploader = uploader;
        this.capacity = capacity;
        this.workers = workers;
        this.idleTimeout = idleTimeout;
        response.setOnCancelHandler(() -> items.add(ABORT));
    }

    @Override
    public void onNext(UploadChunk chunk) {
        if (chunk.hasHeader() && !started) {
            start(chunk.getHeader());
        } else if (started) {
            items.add(new Item(chunk.getData()));
        } else {
            response.onError(NO_HEADER.asRuntimeException());
        }
    }

    @Override
    public void onError(Throwable error) {
        items.add(ABORT);
    }

    @Override
    public void onCompleted() {
        items.add(END);
    }

    private void start(UploadHeader header) {
        if (!capacity.tryAcquire()) {
            response.onError(FULL.asRuntimeException());
            return;
        }
        started = true;
        workers.execute(() -> runAndRelease(header));
    }

    private void runAndRelease(UploadHeader header) {
        try {
            SftpUploader.OpenedUpload opened = uploader.open(header);
            try {
                transfer(opened);
            } catch (RuntimeException | InterruptedException e) {
                opened.upload().abort();
                throw e;
            } finally {
                opened.upload().close();
            }
        } catch (InspectionFailure failure) {
            log.info("Upload failed: {}", failure.getStatus().getCode());
            fail(failure.getStatus());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(ABORTED);
        } catch (RuntimeException e) {
            log.warn("Upload failed unexpectedly: {}", e.getClass().getSimpleName());
            fail(Status.INTERNAL.withDescription("Wysyłka przerwana przez błąd mikroserwisu."));
        } finally {
            capacity.release();
        }
    }

    private void transfer(SftpUploader.OpenedUpload opened) throws InterruptedException {
        response.onNext(UploadEvent.newBuilder().setAccepted(UploadAccepted.newBuilder().setHostKey(opened.hostKey())).build());
        response.request(1);
        Item item = next();
        while (item != END) {
            opened.upload().write(item.data());
            response.request(1);
            item = next();
        }
        response.onNext(UploadEvent.newBuilder().setResult(opened.upload().finish()).build());
        response.onCompleted();
    }

    private Item next() throws InterruptedException {
        Item item = items.poll(idleTimeout.toMillis(), TimeUnit.MILLISECONDS);
        if (item == null) {
            throw new InspectionFailure(IDLE);
        }
        if (item == ABORT) {
            throw new InspectionFailure(ABORTED);
        }
        return item;
    }

    private void fail(Status status) {
        if (!response.isCancelled()) {
            response.onError(status.asRuntimeException());
        }
    }

    private record Item(ByteString data) {
    }
}
