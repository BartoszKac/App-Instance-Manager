package com.example.DynamicCode.transfer.service;

import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.service.RemoteSerwerService;
import com.example.DynamicCode.inspection.service.InspectionHostKeys;
import com.example.DynamicCode.shared.metrics.InspectorWorkGauge;
import com.example.DynamicCode.transfer.dto.DirectUploadCommand;
import com.example.DynamicCode.transfer.dto.DirectUploadResponse;
import com.example.DynamicCode.transfer.entity.DirectUploadRecord;
import com.example.DynamicCode.transfer.enums.DirectUploadState;
import com.example.DynamicCode.transfer.exception.DirectUploadException;
import com.example.DynamicCode.transfer.repository.DirectUploadRecordRepository;
import com.example.inspection.grpc.v1.ServerFileTransferGrpc;
import com.example.inspection.grpc.v1.UploadAccepted;
import com.example.inspection.grpc.v1.UploadChunk;
import com.example.inspection.grpc.v1.UploadEvent;
import com.example.inspection.grpc.v1.UploadResult;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DirectUploadServiceTest {

    private static final DirectUploadCommand COMMAND = new DirectUploadCommand("/opt", "dir/a.txt", false, 5);
    private static final byte[] BODY = {1, 2, 3, 4, 5};

    private final RemoteSerwerService servers = mock(RemoteSerwerService.class);
    private final InspectionHostKeys hostKeys = mock(InspectionHostKeys.class);
    private final DirectUploadRecordRepository repository = mock(DirectUploadRecordRepository.class);
    private final InspectorWorkGauge gauge = new InspectorWorkGauge();
    private final ByteArrayOutputStream received = new ByteArrayOutputStream();

    private Server server;
    private ManagedChannel channel;
    private DirectUploadService service;
    private Status rejectWith;
    private volatile String receivedPassword;

    @BeforeEach
    void start() throws IOException {
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name).addService(new FakeInspector()).build().start();
        channel = InProcessChannelBuilder.forName(name).build();
        service = new DirectUploadService(servers, hostKeys, new DirectUploadRecords(repository), gauge, ServerFileTransferGrpc.newStub(channel));
        ReflectionTestUtils.setField(service, "maxActiveUploads", 1);
        when(servers.getConfigById(1L)).thenReturn(new RemoteSerwerConfiguration(1L, "box", "10.0.0.1", "u", "secret", null));
        when(hostKeys.pinnedFor(anyLong(), anyString())).thenReturn("");
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @AfterEach
    void stop() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void shouldStreamBodyToInspectorAndKeepOnlyMetadata() {
        DirectUploadResponse response = service.upload(1L, COMMAND, new ByteArrayInputStream(BODY));

        assertThat(received.toByteArray()).isEqualTo(BODY);
        assertThat(receivedPassword).isEqualTo("secret");
        assertThat(response.state()).isEqualTo(DirectUploadState.DONE);
        assertThat(response.remotePath()).isEqualTo("/opt/dir/a.txt");
        assertThat(response.sha256()).isEqualTo("abc");
        assertThat(response.sizeBytes()).isEqualTo(5);
        verify(hostKeys).pin(1L, "10.0.0.1", "SHA256:k");
        assertThat(gauge.activeUploads()).isZero();
        assertThat(java.util.Arrays.stream(DirectUploadRecord.class.getDeclaredFields()).map(java.lang.reflect.Field::getType))
                .doesNotContain((Class) byte[].class);
    }

    @Test
    void shouldAnswerServiceUnavailableWhenInstanceIsFull() {
        rejectWith = Status.RESOURCE_EXHAUSTED.withDescription("pełno");

        assertUploadFails(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(received.size()).isZero();
    }

    @Test
    void shouldAnswerConflictWhenFileExists() {
        rejectWith = Status.ALREADY_EXISTS.withDescription("Plik już istnieje");

        assertUploadFails(HttpStatus.CONFLICT);
    }

    @Test
    void shouldAnswerBadGatewayWhenLoginIsRejected() {
        rejectWith = Status.UNAUTHENTICATED.withDescription("odrzucone");

        assertUploadFails(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void shouldRefuseWhenTooManyUploadsAreActive() {
        gauge.tryBeginUpload(1);

        assertUploadFails(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void shouldRefuseServerWithoutPassword() {
        when(servers.getConfigById(2L)).thenReturn(new RemoteSerwerConfiguration(2L, "box", "10.0.0.2", "u", "", null));

        assertThatThrownBy(() -> service.upload(2L, COMMAND, new ByteArrayInputStream(BODY))).isInstanceOf(DirectUploadException.class);
    }

    private void assertUploadFails(HttpStatus status) {
        assertThatThrownBy(() -> service.upload(1L, COMMAND, new ByteArrayInputStream(BODY)))
                .isInstanceOfSatisfying(DirectUploadException.class, e -> assertThat(e.getStatus()).isEqualTo(status));
    }

    private final class FakeInspector extends ServerFileTransferGrpc.ServerFileTransferImplBase {

        @Override
        public StreamObserver<UploadChunk> upload(StreamObserver<UploadEvent> response) {
            return new StreamObserver<>() {
                @Override
                public void onNext(UploadChunk chunk) {
                    if (chunk.hasHeader()) {
                        accept(chunk, response);
                    } else {
                        received.writeBytes(chunk.getData().toByteArray());
                    }
                }

                @Override
                public void onError(Throwable error) {
                }

                @Override
                public void onCompleted() {
                    response.onNext(UploadEvent.newBuilder().setResult(UploadResult.newBuilder()
                            .setRemotePath("/opt/dir/a.txt").setSizeBytes(received.size()).setSha256("abc")).build());
                    response.onCompleted();
                }
            };
        }

        private void accept(UploadChunk chunk, StreamObserver<UploadEvent> response) {
            receivedPassword = chunk.getHeader().getPassword();
            if (rejectWith != null) {
                response.onError(rejectWith.asRuntimeException());
                return;
            }
            response.onNext(UploadEvent.newBuilder().setAccepted(UploadAccepted.newBuilder().setHostKey("SHA256:k")).build());
        }
    }
}
