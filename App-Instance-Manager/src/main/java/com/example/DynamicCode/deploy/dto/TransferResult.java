package com.example.DynamicCode.deploy.dto;

public record TransferResult(Long mainClassId, Long configurationId, boolean success, String message) {
}
