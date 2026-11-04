package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.enums.OperationSystem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class DeviceClassifier {

    record OsMatch(String name, OperationSystem family) {
    }

    private static final Map<String, OsMatch> OS_BY_KEYWORD = osByKeyword();
    private static final Map<String, String> TYPE_BY_NAME = typeByName();
    private static final Map<Integer, String> TYPE_BY_PORT = typeByPort();

    private DeviceClassifier() {
    }

    static OsMatch detectOs(List<String> evidence) {
        String text = String.join(" ", evidence).toLowerCase(Locale.ROOT);
        return OS_BY_KEYWORD.entrySet().stream()
                .filter(entry -> text.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    static String deviceType(List<Integer> openPorts, String hostname) {
        String name = hostname == null ? "" : hostname.toLowerCase(Locale.ROOT);
        return TYPE_BY_NAME.entrySet().stream()
                .filter(entry -> name.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseGet(() -> typeFromPorts(openPorts));
    }

    private static String typeFromPorts(List<Integer> openPorts) {
        return TYPE_BY_PORT.entrySet().stream()
                .filter(entry -> openPorts.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private static Map<String, OsMatch> osByKeyword() {
        Map<String, OsMatch> os = new LinkedHashMap<>();
        os.put("windows", new OsMatch("Windows", OperationSystem.WINDOWS));
        os.put("ubuntu", new OsMatch("Linux (Ubuntu)", OperationSystem.LINUX));
        os.put("raspbian", new OsMatch("Linux (Raspberry Pi OS)", OperationSystem.LINUX));
        os.put("debian", new OsMatch("Linux (Debian)", OperationSystem.LINUX));
        os.put("fedora", new OsMatch("Linux (Fedora)", OperationSystem.LINUX));
        os.put("red hat", new OsMatch("Linux (Red Hat)", OperationSystem.LINUX));
        os.put("centos", new OsMatch("Linux (CentOS)", OperationSystem.LINUX));
        os.put("linux", new OsMatch("Linux", OperationSystem.LINUX));
        os.put("mac os", new OsMatch("macOS", OperationSystem.MAC));
        os.put("darwin", new OsMatch("macOS", OperationSystem.MAC));
        return os;
    }

    private static Map<String, String> typeByName() {
        Map<String, String> types = new LinkedHashMap<>();
        for (String phone : List.of("pixel", "iphone", "galaxy", "android", "redmi", "xiaomi", "oneplus")) {
            types.put(phone, "Telefon");
        }
        types.put("ipad", "Tablet");
        types.put("macbook", "Laptop Apple");
        types.put("printer", "Drukarka");
        return types;
    }

    private static Map<Integer, String> typeByPort() {
        Map<Integer, String> types = new LinkedHashMap<>();
        types.put(9100, "Drukarka");
        types.put(631, "Drukarka");
        types.put(62078, "Telefon lub tablet Apple");
        types.put(554, "Kamera lub rejestrator");
        types.put(53, "Router lub serwer DNS");
        types.put(3389, "Komputer lub serwer");
        types.put(22, "Komputer lub serwer");
        types.put(445, "Komputer lub serwer");
        return types;
    }
}
