package com.example.DynamicCode.transfer.dto;

import java.util.List;

public record DirectUploadPageResponse(List<DirectUploadResponse> entries, long total) {
}
