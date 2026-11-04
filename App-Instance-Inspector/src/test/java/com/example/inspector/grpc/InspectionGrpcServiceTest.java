package com.example.inspector.grpc;

import com.example.inspection.grpc.v1.Completed;
import com.example.inspection.grpc.v1.InspectEvent;
import com.example.inspection.grpc.v1.InspectRequest;
import com.example.inspection.grpc.v1.ServerInspectionGrpc;
import com.example.inspector.config.InspectorProperties;
import com.example.inspector.inspection.InspectionFailure;
import com.example.inspector.inspection.InspectionJob;
import com.example.inspector.inspection.InspectionRunner;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class InspectionGrpcServiceTest {

    private static final InspectRequest VALID = InspectRequest.newBuilder().setHost("10.0.0.1").setUser("u").setPassword("p").build();

    private final InspectionRunner runner = mock(InspectionRunner.class);
    private Server server;
    private ManagedChannel channel;
    private ServerInspectionGrpc.ServerInspectionBlockingStub stub;

    @BeforeEach
    void start() throws IOException {
        InspectorProperties properties = new InspectorProperties(0, 1, 10, 10, Duration.ofMinutes(1), 1, Duration.ofMinutes(1));
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name).addService(new InspectionGrpcService(runner, properties)).build().start();
        channel = InProcessChannelBuilder.forName(name).build();
        stub = ServerInspectionGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void stop() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void shouldStreamEventsFromRunner() {
        InspectEvent event = InspectEvent.newBuilder().setCompleted(Completed.getDefaultInstance()).build();
        doAnswer(call -> {
            call.<InspectionJob>getArgument(1).sink().send(event);
            return null;
        }).when(runner).run(any(), any());

        Iterator<InspectEvent> events = stub.inspect(VALID);

        assertThat(events.next()).isEqualTo(event);
        assertThat(events.hasNext()).isFalse();
    }

    @Test
    void shouldRejectRequestWhenPasswordIsMissing() {
        InspectRequest noPassword = VALID.toBuilder().clearPassword().build();

        assertThatThrownBy(() -> stub.inspect(noPassword).hasNext())
                .isInstanceOfSatisfying(StatusRuntimeException.class,
                        e -> assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    @Test
    void shouldMapFailureToItsStatusWithoutLeakingOtherDetails() {
        doThrow(new InspectionFailure(Status.UNAUTHENTICATED.withDescription("odrzucone"))).when(runner).run(any(), any());

        assertThatThrownBy(() -> stub.inspect(VALID).hasNext())
                .isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                    assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.UNAUTHENTICATED);
                    assertThat(e.getStatus().getDescription()).isEqualTo("odrzucone");
                });
    }

    @Test
    void shouldAnswerResourceExhaustedWhenAtCapacityAndRecoverAfterwards() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(call -> {
            started.countDown();
            release.await(10, TimeUnit.SECONDS);
            return null;
        }).when(runner).run(any(), any());
        Iterator<InspectEvent> first = stub.inspect(VALID);
        Thread drain = new Thread(first::hasNext);
        drain.start();
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

        assertThatThrownBy(() -> stub.inspect(VALID).hasNext())
                .isInstanceOfSatisfying(StatusRuntimeException.class,
                        e -> assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.RESOURCE_EXHAUSTED));

        release.countDown();
        drain.join(5000);
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertThat(awaitCapacity()).isTrue());
    }

    private boolean awaitCapacity() throws InterruptedException {
        while (true) {
            try {
                return !stub.inspect(VALID).hasNext();
            } catch (StatusRuntimeException e) {
                Thread.sleep(50);
            }
        }
    }
}
