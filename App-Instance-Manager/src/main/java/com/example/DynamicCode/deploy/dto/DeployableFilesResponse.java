package com.example.DynamicCode.deploy.dto;

import com.example.DynamicCode.code.enums.LanguageType;

import java.time.Instant;
import java.util.List;

public record DeployableFilesResponse(List<ProjectFile> projects, List<UploadedFile> uploads) {

    public record ProjectFile(Long mainClassId, String name, LanguageType language, long compiledFileCount) {
    }

    public record UploadedFile(Long id, String name, LanguageType language, String description, Long sizeBytes, Instant uploadedAt) {
    }
}
