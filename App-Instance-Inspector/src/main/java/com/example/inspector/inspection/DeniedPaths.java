package com.example.inspector.inspection;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DeniedPaths extends OutputStream {

    private static final int MAX_PATHS = 1000;
    private static final int MAX_PATH_BYTES = 1000;
    private static final Pattern DENIED = Pattern.compile("^find: [‘']?(.*?)[’']?: (Permission denied|Operation not permitted)$");

    private final List<String> paths = new ArrayList<>();
    private final ByteArrayOutputStream line = new ByteArrayOutputStream();

    @Override
    public synchronized void write(int value) {
        if (value == '\n') {
            flushLine();
        } else if (line.size() < MAX_PATH_BYTES) {
            line.write(value);
        }
    }

    synchronized void add(String path) {
        if (paths.size() < MAX_PATHS && path.length() <= MAX_PATH_BYTES) {
            paths.add(path);
        }
    }

    synchronized List<String> paths() {
        return List.copyOf(paths);
    }

    private void flushLine() {
        Matcher matcher = DENIED.matcher(line.toString(StandardCharsets.UTF_8).stripTrailing());
        line.reset();
        if (matcher.matches()) {
            add(matcher.group(1));
        }
    }
}
