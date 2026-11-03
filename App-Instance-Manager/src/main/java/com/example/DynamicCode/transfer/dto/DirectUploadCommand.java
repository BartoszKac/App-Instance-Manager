package com.example.DynamicCode.transfer.dto;

public record DirectUploadCommand(String destination, String relativePath, boolean overwrite, long expectedSize) {
}
