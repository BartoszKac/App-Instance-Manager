package com.example.DynamicCode.deploy.dto;

public record DiscoveredService(
        int port,
        String name,
        String product,
        String version,
        String extraInfo,
        String title,
        String tlsSubject) {
}
