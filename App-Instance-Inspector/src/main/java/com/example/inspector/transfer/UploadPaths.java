package com.example.inspector.transfer;

import com.example.inspector.inspection.InspectionFailure;
import io.grpc.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class UploadPaths {

    private static final int MAX_PATH_CHARS = 4000;
    private static final Set<String> FORBIDDEN_SEGMENTS = Set.of("", ".", "..");

    private final String directory;
    private final String name;

    private UploadPaths(String directory, String name) {
        this.directory = directory;
        this.name = name;
    }

    public static UploadPaths resolve(String destinationDir, String relativePath) {
        String destination = destinationDir.length() > 1 && destinationDir.endsWith("/")
                ? destinationDir.substring(0, destinationDir.length() - 1) : destinationDir;
        if (!destination.startsWith("/") || !isSafe(destination.substring(1), destination.equals("/"))) {
            throw invalid("Katalog docelowy musi być pełną ścieżką bez \"..\", np. /opt/app.");
        }
        if (relativePath.isEmpty() || !isSafe(relativePath, false)) {
            throw invalid("Niedozwolona nazwa pliku: " + relativePath.replaceAll("\\p{Cntrl}", "?"));
        }
        String full = destination.equals("/") ? "/" + relativePath : destination + "/" + relativePath;
        if (full.length() > MAX_PATH_CHARS) {
            throw invalid("Ścieżka pliku jest za długa.");
        }
        int last = full.lastIndexOf('/');
        return new UploadPaths(last == 0 ? "/" : full.substring(0, last), full.substring(last + 1));
    }

    public String directory() {
        return directory;
    }

    public String target() {
        return join(name);
    }

    public String join(String child) {
        return directory.equals("/") ? "/" + child : directory + "/" + child;
    }

    public List<String> directoryChain() {
        List<String> chain = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String segment : directory.substring(1).split("/")) {
            if (!segment.isEmpty()) {
                chain.add(current.append('/').append(segment).toString());
            }
        }
        return chain;
    }

    private static boolean isSafe(String path, boolean emptyAllowed) {
        if (path.isEmpty()) {
            return emptyAllowed;
        }
        boolean hasControl = path.chars().anyMatch(Character::isISOControl);
        boolean hasForbidden = Arrays.stream(path.split("/", -1)).anyMatch(FORBIDDEN_SEGMENTS::contains);
        return !hasControl && !hasForbidden;
    }

    private static InspectionFailure invalid(String message) {
        return new InspectionFailure(Status.INVALID_ARGUMENT.withDescription(message));
    }
}
