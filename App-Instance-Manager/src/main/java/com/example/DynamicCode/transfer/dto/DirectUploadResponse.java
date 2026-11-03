package com.example.DynamicCode.transfer.dto;

import com.example.DynamicCode.transfer.entity.DirectUploadRecord;
import com.example.DynamicCode.transfer.enums.DirectUploadState;

import java.time.Instant;

public record DirectUploadResponse(Long id, String relativePath, String remotePath, long sizeBytes, String sha256,
                                   DirectUploadState state, String error, Instant startedAt, Instant finishedAt) {

    public static DirectUploadResponse of(DirectUploadRecord record) {
        return new DirectUploadResponse(record.getId(), record.getRelativePath(), record.getRemotePath(),
                record.getSizeBytes(), record.getSha256(), record.getState(), record.getError(),
                record.getStartedAt(), record.getFinishedAt());
    }
}
