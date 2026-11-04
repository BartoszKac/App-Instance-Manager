package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.InventorySection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class InventoryOutputParser {

    private static final String MARKER = "@@";
    private static final int MAX_SECTION_LENGTH = 4000;
    private static final Map<String, String> LABELS = labels();

    private InventoryOutputParser() {
    }

    static boolean isInventory(String output) {
        return output != null && output.contains(MARKER + "hostname");
    }

    static List<InventorySection> parse(String output) {
        Map<String, StringBuilder> raw = new LinkedHashMap<>();
        StringBuilder current = null;
        for (String line : output.split("\\R")) {
            if (line.startsWith(MARKER)) {
                current = raw.computeIfAbsent(line.substring(MARKER.length()).trim(), key -> new StringBuilder());
            } else if (current != null) {
                current.append(line).append('\n');
            }
        }
        return toSections(raw);
    }

    private static List<InventorySection> toSections(Map<String, StringBuilder> raw) {
        List<InventorySection> sections = new ArrayList<>();
        LABELS.forEach((key, label) -> {
            String value = raw.containsKey(key) ? raw.get(key).toString().strip() : "";
            if (!value.isEmpty()) {
                sections.add(new InventorySection(key, label, value.substring(0, Math.min(MAX_SECTION_LENGTH, value.length()))));
            }
        });
        return sections;
    }

    private static Map<String, String> labels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("hostname", "Nazwa");
        labels.put("os", "System");
        labels.put("kernel", "Jądro");
        labels.put("arch", "Architektura");
        labels.put("model", "Sprzęt");
        labels.put("virt", "Wirtualizacja");
        labels.put("uptime", "Czas działania");
        labels.put("boot", "Ostatni start");
        labels.put("cpu", "Procesor");
        labels.put("cores", "Rdzenie");
        labels.put("load", "Obciążenie");
        labels.put("memory", "Pamięć RAM");
        labels.put("swap", "Swap");
        labels.put("disks", "Dyski");
        labels.put("gpu", "Karta graficzna");
        labels.put("ip", "Adresy IP");
        labels.put("mac", "Adresy MAC");
        labels.put("gateway", "Brama");
        labels.put("dns", "Serwery DNS");
        labels.put("listening", "Nasłuchujące porty");
        labels.put("services", "Usługi");
        labels.put("running", "Działające usługi");
        labels.put("docker", "Docker");
        labels.put("containers", "Kontenery");
        labels.put("runtimes", "Oprogramowanie");
        labels.put("packages", "Pakiety");
        labels.put("users", "Zalogowani");
        return labels;
    }
}
