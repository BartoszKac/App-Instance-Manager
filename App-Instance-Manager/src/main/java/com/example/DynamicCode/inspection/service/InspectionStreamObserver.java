package com.example.DynamicCode.inspection.service;

import com.example.inspection.grpc.v1.InspectEvent;
import com.example.inspection.grpc.v1.InspectRequest;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
class InspectionStreamObserver implements ClientResponseObserver<InspectRequest, InspectEvent> {

    private final Long runId;
    private final InspectionDispatcher dispatcher;
    private final InspectionStore store;
    private final Executor executor;

    private ClientCallStreamObserver<InspectRequest> call;
    private CompletableFuture<Void> tail = CompletableFuture.completedFuture(null);

    InspectionStreamObserver(Long runId, InspectionDispatcher dispatcher, InspectionStore store, Executor executor) {
        this.runId = runId;
        this.dispatcher = dispatcher;
        this.store = store;
        this.executor = executor;
    }

    @Override
    public void beforeStart(ClientCallStreamObserver<InspectRequest> requestStream) {
        call = requestStream;
        requestStream.disableAutoRequestWithInitial(1);
    }

    @Override
    public void onNext(InspectEvent event) {
        enqueue(() -> {
            store.apply(runId, event);
            call.request(1);
        });
    }

    @Override
    public void onError(Throwable error) {
        enqueue(() -> dispatcher.onFailed(runId, Status.fromThrowable(error)));
    }

    @Override
    public void onCompleted() {
        enqueue(() -> dispatcher.onFinished(runId));
    }

    void cancel() {
        call.cancel("Inspection cancelled", null);
    }

    private synchronized void enqueue(Runnable step) {
        tail = tail.thenRunAsync(step, executor).exceptionally(this::abortAfterFailure);
    }

    private Void abortAfterFailure(Throwable error) {
        log.warn("Inspection run {} persistence step failed: {}", runId, error.getClass().getSimpleName());
        cancel();
        return null;
    }
}
