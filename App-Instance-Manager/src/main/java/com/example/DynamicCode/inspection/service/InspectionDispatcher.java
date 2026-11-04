package com.example.DynamicCode.inspection.service;

import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.OperationSystem;
import com.example.DynamicCode.deploy.service.RemoteSerwerService;
import com.example.DynamicCode.inspection.config.InspectionProperties;
import com.example.DynamicCode.inspection.entity.InspectionRun;
import com.example.DynamicCode.inspection.enums.InspectionFailureKind;
import com.example.DynamicCode.inspection.repository.InspectionHostKeyRepository;
import com.example.DynamicCode.inspection.repository.InspectionRunRepository;
import com.example.inspection.grpc.v1.InspectRequest;
import com.example.inspection.grpc.v1.ServerInspectionGrpc;
import com.example.inspection.grpc.v1.TargetPlatform;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionDispatcher {

    private static final Set<Status.Code> RETRIABLE = Set.of(Status.Code.RESOURCE_EXHAUSTED, Status.Code.UNAVAILABLE);
    private static final Map<Status.Code, InspectionFailureKind> FAILURE_KINDS = Map.of(
            Status.Code.UNAUTHENTICATED, InspectionFailureKind.LOGIN_REJECTED,
            Status.Code.PERMISSION_DENIED, InspectionFailureKind.HOST_KEY_CHANGED);
    private static final String SERVICE_UNAVAILABLE = "Mikroserwis badający jest niedostępny. Uruchom badanie ponownie.";
    private static final String STOPPED = "Badanie przerwane.";

    private final InspectionProperties properties;
    private final InspectionRunRepository runs;
    private final InspectionHostKeyRepository hostKeys;
    private final RemoteSerwerService servers;
    private final InspectionStore store;
    private final ServerInspectionGrpc.ServerInspectionStub stub;
    private final ExecutorService inspectionPersistenceExecutor;
    private final ScheduledExecutorService inspectionRetryScheduler;

    private final Queue<Long> queued = new ConcurrentLinkedQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger();
    private final Map<Long, Integer> retries = new ConcurrentHashMap<>();
    private final Map<Long, InspectionStreamObserver> activeCalls = new ConcurrentHashMap<>();

    public void enqueue(Long runId) {
        queued.add(runId);
        inspectionPersistenceExecutor.execute(this::drain);
    }

    public void cancelRun(Long runId) {
        queued.remove(runId);
        InspectionStreamObserver call = activeCalls.get(runId);
        if (call != null) {
            call.cancel();
        }
    }

    void onFinished(Long runId) {
        release(runId);
        retries.remove(runId);
        store.finishIfUnfinished(runId, STOPPED);
    }

    void onFailed(Long runId, Status status) {
        release(runId);
        int attempt = retries.merge(runId, 1, Integer::sum);
        if (RETRIABLE.contains(status.getCode()) && attempt <= properties.maxRetries()) {
            store.requeue(runId);
            inspectionRetryScheduler.schedule(() -> enqueue(runId), properties.retryDelay().toMillis(), TimeUnit.MILLISECONDS);
            return;
        }
        retries.remove(runId);
        store.fail(runId, describe(status), FAILURE_KINDS.getOrDefault(status.getCode(), InspectionFailureKind.OTHER));
    }

    private void drain() {
        while (inFlight.get() < properties.maxInFlight()) {
            Long runId = queued.poll();
            if (runId == null) {
                return;
            }
            start(runId);
        }
    }

    private void start(Long runId) {
        try {
            InspectionRun run = runs.findById(runId).orElseThrow();
            RemoteSerwerConfiguration server = servers.getConfigById(run.getServerId());
            store.markRunning(runId, server.getIp());
            InspectionStreamObserver observer = new InspectionStreamObserver(runId, this, store, inspectionPersistenceExecutor);
            inFlight.incrementAndGet();
            activeCalls.put(runId, observer);
            stub.withDeadlineAfter(properties.callTimeout().toSeconds(), TimeUnit.SECONDS).inspect(toRequest(server), observer);
        } catch (RuntimeException e) {
            log.warn("Inspection run {} could not start: {}", runId, e.getClass().getSimpleName());
            store.fail(runId, STOPPED, InspectionFailureKind.OTHER);
        }
    }

    private InspectRequest toRequest(RemoteSerwerConfiguration server) {
        String pinned = hostKeys.findById(server.getIdConfiguration())
                .filter(key -> key.getIp().equals(server.getIp()))
                .map(key -> key.getFingerprint())
                .orElse("");
        boolean windows = server.getOperationSystem() == OperationSystem.WINDOWS;
        return InspectRequest.newBuilder()
                .setHost(server.getIp())
                .setUser(server.getUser())
                .setPassword(server.getPass())
                .setPinnedHostKey(pinned)
                .setPreferredPlatform(windows ? TargetPlatform.TARGET_PLATFORM_WINDOWS : TargetPlatform.TARGET_PLATFORM_POSIX)
                .build();
    }

    private void release(Long runId) {
        activeCalls.remove(runId);
        inFlight.decrementAndGet();
        inspectionPersistenceExecutor.execute(this::drain);
    }

    private String describe(Status status) {
        boolean unavailable = RETRIABLE.contains(status.getCode()) || status.getDescription() == null;
        return unavailable ? SERVICE_UNAVAILABLE : status.getDescription();
    }
}
