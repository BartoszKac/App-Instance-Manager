package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.DiscoveredService;
import com.example.DynamicCode.deploy.dto.ScanJobResponse;
import com.example.DynamicCode.deploy.dto.ScriptResult;
import com.example.DynamicCode.deploy.enums.OperationSystem;
import com.example.DynamicCode.deploy.enums.ScanStage;
import com.example.DynamicCode.deploy.enums.ScanState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScanJobTest {

    private static final DiscoveredDevice DEVICE = new DiscoveredDevice("192.168.1.100", null, null, null, false, List.of(), null);
    private static final DiscoveredDevice OTHER = new DiscoveredDevice("192.168.1.9", null, null, null, false, List.of(), null);

    @Test
    void shouldStartAtZeroPercent() {
        assertThat(new ScanJob("192.168.1.0/24", 254).snapshot().percent()).isZero();
    }

    @Test
    void shouldReportSweepProgressAndCounts() {
        ScanJob job = new ScanJob("192.168.1.0/24", 254);

        job.addSweepResult(127, List.of(DEVICE));

        ScanJobResponse snapshot = job.snapshot();
        assertThat(snapshot.percent()).isEqualTo(10);
        assertThat(snapshot.addressesScanned()).isEqualTo(127);
        assertThat(snapshot.deviceCount()).isEqualTo(1);
    }

    @Test
    void shouldIncreasePercentThroughStagesAndSortDevices() {
        ScanJob job = new ScanJob("192.168.1.0/24", 254);
        job.addSweepResult(254, List.of(DEVICE, OTHER));
        assertThat(job.snapshot().percent()).isEqualTo(20);

        job.enterStage(ScanStage.PORTS);
        job.setPorts(DEVICE.ip(), List.of(22));
        assertThat(job.snapshot().percent()).isEqualTo(32);
        assertThat(job.snapshot().devices()).extracting(DiscoveredDevice::ip).containsExactly(OTHER.ip(), DEVICE.ip());
        assertThat(job.snapshot().devices().get(1).sshOpen()).isTrue();

        job.enterStage(ScanStage.DETAILS);
        job.startDetails(2);
        job.addDetails(DEVICE.ip(), DeviceDetailsResponse.empty(DEVICE.ip()));
        assertThat(job.snapshot().percent()).isEqualTo(75);

        job.addDetails(DEVICE.ip(), DeviceDetailsResponse.empty(DEVICE.ip()));
        assertThat(job.snapshot().percent()).isEqualTo(100);
        job.complete();
        assertThat(job.snapshot().state()).isEqualTo(ScanState.DONE);
        assertThat(job.isRunning()).isFalse();
    }

    @Test
    void shouldMergeResultsOfOneDeviceKeepingMostSpecificOs() {
        ScanJob job = new ScanJob("192.168.1.0/24", 254);
        DiscoveredService http = new DiscoveredService(80, "http", null, null, null, null, null, List.of(), List.of());
        DiscoveredService ssh = new DiscoveredService(22, "ssh", null, null, null, null, null, List.of(), List.of());

        job.addDetails(DEVICE.ip(), new DeviceDetailsResponse(DEVICE.ip(), null, null, "Linux", OperationSystem.LINUX, null, List.of(http), List.of()));
        job.addDetails(DEVICE.ip(), new DeviceDetailsResponse(DEVICE.ip(), "nas", null, "Linux (Ubuntu)", OperationSystem.LINUX, null, List.of(ssh),
                List.of(new ScriptResult("nbstat", "x"))));

        DeviceDetailsResponse merged = job.snapshot().details().get(DEVICE.ip());
        assertThat(merged.osName()).isEqualTo("Linux (Ubuntu)");
        assertThat(merged.hostname()).isEqualTo("nas");
        assertThat(merged.services()).extracting(DiscoveredService::port).containsExactly(22, 80);
        assertThat(merged.hostScripts()).hasSize(1);
    }

    @Test
    void shouldReachFullPercentWhenNothingToInspect() {
        ScanJob job = new ScanJob("192.168.1.0/24", 254);

        job.complete();

        assertThat(job.snapshot().percent()).isEqualTo(100);
    }

    @Test
    void shouldKeepErrorWhenFailed() {
        ScanJob job = new ScanJob("192.168.1.0/24", 254);

        job.fail("brak nmap");

        assertThat(job.snapshot().state()).isEqualTo(ScanState.FAILED);
        assertThat(job.snapshot().error()).isEqualTo("brak nmap");
    }
}
