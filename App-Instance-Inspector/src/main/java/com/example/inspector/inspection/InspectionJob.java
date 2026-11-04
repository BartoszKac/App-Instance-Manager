package com.example.inspector.inspection;

import com.jcraft.jsch.Session;
import io.grpc.Status;

public class InspectionJob {

    private final EventSink sink;
    private Session session;
    private Status abortStatus;

    public InspectionJob(EventSink sink) {
        this.sink = sink;
    }

    public EventSink sink() {
        return sink;
    }

    public synchronized void attach(Session attached) {
        session = attached;
        if (abortStatus != null) {
            attached.disconnect();
        }
    }

    public void cancel() {
        abort(Status.CANCELLED.withDescription("Badanie anulowane."));
    }

    public void timeOut() {
        abort(Status.DEADLINE_EXCEEDED.withDescription("Badanie przekroczyło limit czasu."));
    }

    public synchronized void throwIfAborted() {
        if (abortStatus != null) {
            throw new InspectionFailure(abortStatus);
        }
    }

    private synchronized void abort(Status status) {
        abortStatus = status;
        if (session != null) {
            session.disconnect();
        }
    }
}
