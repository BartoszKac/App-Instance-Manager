package com.example.DynamicCode.inspection.dto;

import com.example.DynamicCode.inspection.enums.InspectionFailureKind;
import com.example.DynamicCode.inspection.enums.InspectionState;

import java.time.Instant;

public record InspectionStatusResponse(Long serverId, String name, String ip, String user,
                                       InspectionState state, String stage, long filesFound,
                                       String error, InspectionFailureKind failureKind,
                                       Instant dataFrom, boolean truncated) {
}
