package com.example.DynamicCode.inspection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("aim.inspection")
public record InspectionProperties(String target, int maxInFlight, Duration retryDelay, int maxRetries,
                                   Duration callTimeout, int persistenceThreads) {
}
