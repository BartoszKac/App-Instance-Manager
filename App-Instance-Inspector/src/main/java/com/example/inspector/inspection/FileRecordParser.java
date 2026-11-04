package com.example.inspector.inspection;

import com.example.inspection.grpc.v1.FileEntry;

import java.util.Optional;

public final class FileRecordParser {

    public static final String DENIED_TYPE = "!";

    private static final int FIELD_COUNT = 7;
    private static final int MAX_PATH_CHARS = 4000;
    private static final int MAX_NAME_CHARS = 1000;
    private static final int MAX_LABEL_CHARS = 120;

    private FileRecordParser() {
    }

    public static Optional<FileEntry> parse(String record, Platform platform) {
        String[] fields = record.split("\t", FIELD_COUNT);
        if (fields.length < FIELD_COUNT || fields[0].isEmpty() || fields[6].isEmpty() || fields[6].length() > MAX_PATH_CHARS) {
            return Optional.empty();
        }
        String path = fields[6];
        return Optional.of(FileEntry.newBuilder()
                .setType(normalizeType(fields[0]))
                .setSizeBytes(parseLong(fields[1]))
                .setOwner(limit(fields[2], MAX_LABEL_CHARS))
                .setOwnerGroup(limit(fields[3], MAX_LABEL_CHARS))
                .setMode(limit(fields[4], MAX_LABEL_CHARS))
                .setModifiedEpochSeconds(parseLong(fields[5]))
                .setPath(path)
                .setParent(platform.parentOf(path))
                .setName(limit(platform.nameOf(path), MAX_NAME_CHARS))
                .build());
    }

    private static String normalizeType(String raw) {
        char first = raw.charAt(0);
        return first == '-' ? "f" : String.valueOf(first);
    }

    private static long parseLong(String raw) {
        int dot = raw.indexOf('.');
        String whole = dot < 0 ? raw : raw.substring(0, dot);
        try {
            return Long.parseLong(whole);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String limit(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
