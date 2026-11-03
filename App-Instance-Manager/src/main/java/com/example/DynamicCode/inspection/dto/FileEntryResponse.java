package com.example.DynamicCode.inspection.dto;

public record FileEntryResponse(String path, String name, String type, long sizeBytes, String owner, String group,
                                String mode, long modifiedEpochSeconds, boolean denied) {
}
