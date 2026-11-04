package com.example.inspector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("inspector")
public record InspectorProperties(int port, int maxConcurrent, int maxEntries, int batchSize, Duration jobTimeout,
                                  int maxConcurrentUploads, Duration uploadIdleTimeout) {
}
