package com.example.inspector.inspection;

import com.example.inspection.grpc.v1.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FactsParser {

    private static final String MARKER = "@@";
    private static final int MAX_SECTION_CHARS = 256 * 1024;

    private FactsParser() {
    }

    public static boolean isFacts(String output) {
        return output != null && output.contains(MARKER + "hostname");
    }

    public static List<Section> parse(String output) {
        Map<String, StringBuilder> raw = split(output);
        List<Section> sections = new ArrayList<>();
        for (FactSection fact : FactSection.values()) {
            String value = raw.getOrDefault(fact.key(), new StringBuilder()).toString().strip();
            if (!value.isEmpty()) {
                sections.add(toSection(fact, value));
            }
        }
        return sections;
    }

    private static Map<String, StringBuilder> split(String output) {
        Map<String, StringBuilder> raw = new HashMap<>();
        StringBuilder current = null;
        for (String line : output.split("\\R")) {
            if (line.startsWith(MARKER)) {
                current = raw.computeIfAbsent(line.substring(MARKER.length()).trim(), key -> new StringBuilder());
            } else if (current != null) {
                current.append(line).append('\n');
            }
        }
        return raw;
    }

    private static Section toSection(FactSection fact, String value) {
        return Section.newBuilder()
                .setKey(fact.key())
                .setLabel(fact.label())
                .setCategory(fact.categoryLabel())
                .setValue(value.substring(0, Math.min(MAX_SECTION_CHARS, value.length())))
                .build();
    }
}
