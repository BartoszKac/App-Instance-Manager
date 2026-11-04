package com.example.DynamicCode.shared.metrics;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InspectorWorkGauge {

    private final AtomicInteger activeUploads = new AtomicInteger();

    public int tryBeginUpload(int limit) {
        int active = activeUploads.incrementAndGet();
        if (active > limit) {
            activeUploads.decrementAndGet();
            return -1;
        }
        return active;
    }

    public void endUpload() {
        activeUploads.decrementAndGet();
    }

    public int activeUploads() {
        return activeUploads.get();
    }
}
