package com.example.inspector.grpc;

import com.example.inspection.grpc.v1.InspectEvent;
import com.example.inspector.inspection.EventSink;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import com.example.inspector.inspection.InspectionFailure;

final class FlowControlledSink implements EventSink {

    private static final long READY_POLL_MS = 500;

    private final ServerCallStreamObserver<InspectEvent> call;
    private final Object readiness = new Object();

    FlowControlledSink(ServerCallStreamObserver<InspectEvent> call) {
        this.call = call;
        call.setOnReadyHandler(this::signalReady);
    }

    @Override
    public void send(InspectEvent event) {
        awaitReady();
        call.onNext(event);
    }

    private void awaitReady() {
        synchronized (readiness) {
            while (!call.isReady() && !call.isCancelled()) {
                waitForSignal();
            }
        }
        if (call.isCancelled()) {
            throw new InspectionFailure(Status.CANCELLED.withDescription("Badanie anulowane."));
        }
    }

    private void waitForSignal() {
        try {
            readiness.wait(READY_POLL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InspectionFailure(Status.CANCELLED.withDescription("Badanie anulowane."));
        }
    }

    private void signalReady() {
        synchronized (readiness) {
            readiness.notifyAll();
        }
    }
}
