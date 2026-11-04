package com.example.inspector.grpc;

import com.example.inspection.grpc.v1.InspectEvent;
import com.example.inspection.grpc.v1.InspectRequest;
import com.example.inspection.grpc.v1.ServerInspectionGrpc;
import com.example.inspector.config.InspectorProperties;
import com.example.inspector.inspection.InspectionFailure;
import com.example.inspector.inspection.InspectionJob;
import com.example.inspector.inspection.InspectionRunner;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class InspectionGrpcService extends ServerInspectionGrpc.ServerInspectionImplBase {

    private static final String FULL = "Ta instancja ma komplet zadań.";
    private static final String INVALID = "Brak adresu, loginu lub hasła serwera.";

    private final InspectionRunner runner;
    private final InspectorProperties properties;
    private final Semaphore capacity;
    private final ExecutorService workers = Executors.newCachedThreadPool();
    private final ScheduledExecutorService watchdog = Executors.newSingleThreadScheduledExecutor();

    public InspectionGrpcService(InspectionRunner runner, InspectorProperties properties) {
        this.runner = runner;
        this.properties = properties;
        this.capacity = new Semaphore(properties.maxConcurrent());
    }

    @Override
    public void inspect(InspectRequest request, StreamObserver<InspectEvent> observer) {
        if (request.getHost().isBlank() || request.getUser().isBlank() || request.getPassword().isEmpty()) {
            observer.onError(Status.INVALID_ARGUMENT.withDescription(INVALID).asRuntimeException());
            return;
        }
        if (!capacity.tryAcquire()) {
            observer.onError(Status.RESOURCE_EXHAUSTED.withDescription(FULL).asRuntimeException());
            return;
        }
        ServerCallStreamObserver<InspectEvent> call = (ServerCallStreamObserver<InspectEvent>) observer;
        InspectionJob job = new InspectionJob(new FlowControlledSink(call));
        call.setOnCancelHandler(job::cancel);
        workers.execute(() -> runAndRelease(request, job, call));
    }

    private void runAndRelease(InspectRequest request, InspectionJob job, ServerCallStreamObserver<InspectEvent> call) {
        ScheduledFuture<?> timeout = watchdog.schedule(job::timeOut, properties.jobTimeout().toSeconds(), TimeUnit.SECONDS);
        try {
            runner.run(request, job);
            finish(call, null);
        } catch (InspectionFailure failure) {
            log.info("Inspection failed: {}", failure.getStatus().getCode());
            finish(call, failure.getStatus());
        } catch (RuntimeException e) {
            log.warn("Inspection failed unexpectedly: {}", e.getClass().getSimpleName());
            finish(call, Status.INTERNAL.withDescription("Badanie przerwane przez błąd mikroserwisu."));
        } finally {
            timeout.cancel(false);
            capacity.release();
        }
    }

    private void finish(ServerCallStreamObserver<InspectEvent> call, Status status) {
        if (call.isCancelled()) {
            return;
        }
        if (status == null) {
            call.onCompleted();
        } else {
            call.onError(status.asRuntimeException());
        }
    }
}
