package com.example.inspector.inspection;

import com.example.inspection.grpc.v1.InspectEvent;

public interface EventSink {

    void send(InspectEvent event);
}
