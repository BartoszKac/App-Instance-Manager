package com.example.DynamicCode.inspection.service;

import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.OperationSystem;
import com.example.DynamicCode.deploy.service.RemoteSerwerService;
import com.example.DynamicCode.inspection.config.InspectionProperties;
import com.example.DynamicCode.inspection.entity.InspectionHostKey;
import com.example.DynamicCode.inspection.entity.InspectionRun;
import com.example.DynamicCode.inspection.enums.InspectionFailureKind;
import com.example.DynamicCode.inspection.repository.InspectionHostKeyRepository;
import com.example.DynamicCode.inspection.repository.InspectionRunRepository;
import com.example.inspection.grpc.v1.Completed;
import com.example.inspection.grpc.v1.InspectEvent;
import com.example.inspection.grpc.v1.InspectRequest;
import com.example.inspection.grpc.v1.ServerInspectionGrpc;
import com.example.inspection.grpc.v1.TargetPlatform;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InspectionDispatcherTest {

    private static final long RUN_ID = 7L;
    private static final long WAIT_MS = 3000;

    private final InspectionRunRepository runs = mock(InspectionRunRepository.class);
    private final InspectionHostKeyRepository hostKeys = mock(InspectionHostKeyRepository.class);
    private final RemoteSerwerService servers = mock(RemoteSerwerService.class);
    private final InspectionStore store = mock(InspectionStore.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<InspectRequest> received = new AtomicReference<>();
    private final AtomicInteger calls = new AtomicInteger();

    private Server server;
    private ManagedChannel channel;
    private InspectionDispatcher dispatcher;
    private Status respondWith;

    @BeforeEach
    void start() throws IOException {
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name).addService(new FakeInspector()).build().start();
        channel = InProcessChannelBuilder.forName(name).build();
        InspectionProperties properties = new InspectionProperties("unused", 2, Duration.ofMillis(10), 2, Duration.ofMinutes(1), 2);
        dispatcher = new InspectionDispatcher(properties, runs, hostKeys, servers, store,
                ServerInspectionGrpc.newStub(channel), executor, scheduler);
        InspectionRun run = new InspectionRun(1L, Instant.now());
        when(runs.findById(RUN_ID)).thenReturn(Optional.of(run));
        when(servers.getConfigById(1L)).thenReturn(new RemoteSerwerConfiguration(1L, "box", "10.0.0.1", "u", "secret", OperationSystem.WINDOWS));
        when(hostKeys.findById(1L)).thenReturn(Optional.empty());
    }

    @AfterEach
    void stop() {
        channel.shutdownNow();
        server.shutdownNow();
        executor.shutdownNow();
        scheduler.shutdownNow();
    }

    @Test
    void shouldStreamEventsToStoreAndFinish() {
        dispatcher.enqueue(RUN_ID);

        verify(store, timeout(WAIT_MS)).apply(eq(RUN_ID), any(InspectEvent.class));
        verify(store, timeout(WAIT_MS)).finishIfUnfinished(eq(RUN_ID), any());
        assertThat(received.get().getPassword()).isEqualTo("secret");
        assertThat(received.get().getPreferredPlatform()).isEqualTo(TargetPlatform.TARGET_PLATFORM_WINDOWS);
    }

    @Test
    void shouldSendPinnedKeyOnlyWhenIpMatches() {
        when(hostKeys.findById(1L)).thenReturn(Optional.of(new InspectionHostKey(1L, "10.0.0.1", "SHA256:x")));

        dispatcher.enqueue(RUN_ID);

        verify(store, timeout(WAIT_MS)).finishIfUnfinished(eq(RUN_ID), any());
        assertThat(received.get().getPinnedHostKey()).isEqualTo("SHA256:x");
    }

    @Test
    void shouldNotPinWhenServerAddressChanged() {
        when(hostKeys.findById(1L)).thenReturn(Optional.of(new InspectionHostKey(1L, "10.0.0.99", "SHA256:x")));

        dispatcher.enqueue(RUN_ID);

        verify(store, timeout(WAIT_MS)).finishIfUnfinished(eq(RUN_ID), any());
        assertThat(received.get().getPinnedHostKey()).isEmpty();
    }

    @Test
    void shouldRetryWhenInstanceIsFullThenFailAfterLimit() {
        respondWith = Status.RESOURCE_EXHAUSTED;

        dispatcher.enqueue(RUN_ID);

        verify(store, timeout(WAIT_MS)).fail(eq(RUN_ID), any(), eq(InspectionFailureKind.OTHER));
        verify(store, timeout(WAIT_MS).times(2)).requeue(RUN_ID);
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    void shouldFailWithoutRetryWhenLoginIsRejected() {
        respondWith = Status.UNAUTHENTICATED.withDescription("odrzucone");

        dispatcher.enqueue(RUN_ID);

        verify(store, timeout(WAIT_MS)).fail(RUN_ID, "odrzucone", InspectionFailureKind.LOGIN_REJECTED);
        verify(store, after(100).never()).requeue(RUN_ID);
    }

    @Test
    void shouldReportHostKeyChange() {
        respondWith = Status.PERMISSION_DENIED.withDescription("klucz");

        dispatcher.enqueue(RUN_ID);

        verify(store, timeout(WAIT_MS)).fail(RUN_ID, "klucz", InspectionFailureKind.HOST_KEY_CHANGED);
    }

    @Test
    void shouldNotCallInspectorWhenServerIsGone() {
        when(servers.getConfigById(1L)).thenThrow(new IllegalArgumentException("missing"));

        dispatcher.enqueue(RUN_ID);

        verify(store, timeout(WAIT_MS)).fail(eq(RUN_ID), any(), eq(InspectionFailureKind.OTHER));
        verify(store, never()).markRunning(any(), any());
        assertThat(calls.get()).isZero();
    }

    private final class FakeInspector extends ServerInspectionGrpc.ServerInspectionImplBase {

        @Override
        public void inspect(InspectRequest request, StreamObserver<InspectEvent> observer) {
            calls.incrementAndGet();
            received.set(request);
            if (respondWith != null) {
                observer.onError(respondWith.asRuntimeException());
                return;
            }
            observer.onNext(InspectEvent.newBuilder().setCompleted(Completed.getDefaultInstance()).build());
            observer.onCompleted();
        }
    }
}
