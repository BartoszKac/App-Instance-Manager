package com.example.DynamicCode.inspection.enums;

import java.util.List;

public enum InspectionState {
    QUEUED,
    RUNNING,
    DONE,
    FAILED;

    public static final List<InspectionState> ACTIVE = List.of(QUEUED, RUNNING);
}
