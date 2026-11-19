package com.example.DynamicCode.process.dto;

import com.example.DynamicCode.code.enums.LanguageType;

import java.time.Instant;

public record RemoteExecutableSummary(Long id, String name, LanguageType language, String description, Long sizeBytes, Instant uploadedAt) {
}
