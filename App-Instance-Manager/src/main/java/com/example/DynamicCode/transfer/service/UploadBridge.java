package com.example.DynamicCode.transfer.service;

import com.example.inspection.grpc.v1.UploadAccepted;
import com.example.inspection.grpc.v1.UploadChunk;
import com.example.inspection.grpc.v1.UploadEvent;
import com.example.inspection.grpc.v1.UploadResult;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

import java.util.concurrent.CompletableFuture;

final class UploadBridge implements ClientResponseObserver<UploadChunk, UploadEvent> {

    private static final long READY_POLL_MS = 200;

    private final CompletableFuture<UploadAccepted> accepted = new CompletableFuture<>();
    private final CompletableFuture<UploadResult> result = new CompletableFuture<>();
    private final Object readiness = new Object();
    private ClientCallStreamObserver<UploadChunk> call;

    @Override
    public void beforeStart(ClientCallStreamObserver<UploadChunk> requestStream) {
        call = requestStream;
        requestStream.setOnReadyHandler(this::signalReady);
    }

    @Override
    public void onNext(UploadEvent event) {
        if (event.hasAccepted()) {
            accepted.complete(event.getAccepted());
        } else if (event.hasResult()) {
            result.complete(event.getResult());
        }
    }

    @Override
    public void onError(Throwable error) {
        accepted.completeExceptionally(error);
        result.completeExceptionally(error);
        signalReady();
    }

    @Override
    public void onCompleted() {
        IllegalStateException early = new IllegalStateException("Inspector closed the upload early");
        accepted.completeExceptionally(early);
        result.completeExceptionally(early);
        signalReady();
    }

    CompletableFuture<UploadAccepted> accepted() {
        return accepted;
    }

    CompletableFuture<UploadResult> result() {
        return result;
    }

    boolean send(UploadChunk chunk) throws InterruptedException {
        synchronized (readiness) {
            while (!call.isReady() && !result.isDone()) {
                readiness.wait(READY_POLL_MS);
            }
        }
        if (result.isDone()) {
            return false;
        }
        call.onNext(chunk);
        return true;
    }

    void complete() {
        call.onCompleted();
    }

    void cancel() {
        call.cancel("Upload stopped", null);
    }

    private void signalReady() {
        synchronized (readiness) {
            readiness.notifyAll();
        }
    }
}
