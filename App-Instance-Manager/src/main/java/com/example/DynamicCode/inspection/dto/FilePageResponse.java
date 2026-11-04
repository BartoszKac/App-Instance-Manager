package com.example.DynamicCode.inspection.dto;

import java.util.List;

public record FilePageResponse(List<FileEntryResponse> entries, long total) {

    public static final FilePageResponse EMPTY = new FilePageResponse(List.of(), 0);
}
