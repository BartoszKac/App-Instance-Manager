package com.example.inspector.inspection;

import io.grpc.Status;
import lombok.Getter;

@Getter
public class InspectionFailure extends RuntimeException {

    private final transient Status status;

    public InspectionFailure(Status status) {
        super(status.getDescription());
        this.status = status;
    }
}
