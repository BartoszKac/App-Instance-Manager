package com.example.DynamicCode.deploy.dto;

import java.util.List;

public record DiscoveredService(
        int port,
        String name,
        String product,
        String version,
        String extraInfo,
        String title,
        String tlsSubject,
        List<String> cpe,
        List<ScriptResult> scripts) {
}
