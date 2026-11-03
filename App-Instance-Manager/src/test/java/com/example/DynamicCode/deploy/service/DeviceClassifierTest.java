package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.enums.OperationSystem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceClassifierTest {

    @Test
    void shouldPreferSpecificDistributionOverGenericLinux() {
        assertThat(DeviceClassifier.detectOs(List.of("Linux", "9.6p1 Ubuntu")).name()).isEqualTo("Linux (Ubuntu)");
    }

    @Test
    void shouldDetectWindowsAndMac() {
        assertThat(DeviceClassifier.detectOs(List.of("Microsoft Windows RPC")).family()).isEqualTo(OperationSystem.WINDOWS);
        assertThat(DeviceClassifier.detectOs(List.of("Mac OS X")).family()).isEqualTo(OperationSystem.MAC);
    }

    @Test
    void shouldReturnNoOsWithoutEvidence() {
        assertThat(DeviceClassifier.detectOs(List.of("HTTP Server"))).isNull();
    }

    @Test
    void shouldClassifyByMostSpecificPort() {
        assertThat(DeviceClassifier.deviceType(List.of(80, 9100))).isEqualTo("Drukarka");
        assertThat(DeviceClassifier.deviceType(List.of(53, 80, 445))).isEqualTo("Router lub serwer DNS");
        assertThat(DeviceClassifier.deviceType(List.of(22))).isEqualTo("Komputer lub serwer");
        assertThat(DeviceClassifier.deviceType(List.of(80))).isNull();
    }
}
