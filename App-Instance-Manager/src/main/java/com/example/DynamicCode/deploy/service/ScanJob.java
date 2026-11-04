package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.ScanJobResponse;
import com.example.DynamicCode.deploy.enums.ScanStage;
import com.example.DynamicCode.deploy.enums.ScanState;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;

final class ScanJob {

    private final String id = UUID.randomUUID().toString();
    private final String cidr;
    private final int addressesTotal;
    private final Map<String, DiscoveredDevice> devices = new HashMap<>();
    private final Map<String, DeviceDetailsResponse> details = new LinkedHashMap<>();
    private ScanState state = ScanState.RUNNING;
    private ScanStage stage = ScanStage.SWEEP;
    private int addressesScanned;
    private int portsScanned;
    private int detailsTotal;
    private int detailsDone;
    private String error;

    ScanJob(String cidr, int addressesTotal) {
        this.cidr = cidr;
        this.addressesTotal = addressesTotal;
    }

    synchronized void addSweepResult(int scanned, List<DiscoveredDevice> found) {
        addressesScanned += scanned;
        found.forEach(device -> devices.put(device.ip(), device));
    }

    synchronized List<DiscoveredDevice> enterStage(ScanStage next) {
        stage = next;
        return sortedDevices();
    }

    synchronized void setPorts(String ip, List<Integer> ports) {
        update(ip, device -> device.withOpenPorts(ports));
        portsScanned++;
    }

    synchronized void update(String ip, UnaryOperator<DiscoveredDevice> change) {
        devices.computeIfPresent(ip, (key, device) -> change.apply(device));
    }

    synchronized void startDetails(int total) {
        detailsTotal = total;
    }

    synchronized void addDetails(String ip, DeviceDetailsResponse found) {
        details.merge(ip, found, DeviceDetailsResponse::merge);
        detailsDone++;
    }

    synchronized void complete() {
        state = ScanState.DONE;
    }

    synchronized void fail(String message) {
        state = ScanState.FAILED;
        error = message;
    }

    synchronized boolean isRunning() {
        return state == ScanState.RUNNING;
    }

    synchronized ScanJobResponse snapshot() {
        return new ScanJobResponse(id, cidr, state, stage, percent(), addressesTotal, addressesScanned, portsScanned,
                detailsTotal, detailsDone, devices.size(), sortedDevices(), Map.copyOf(details), error);
    }

    private int percent() {
        if (state == ScanState.DONE) {
            return 100;
        }
        return stage.getStartPercent() + (int) (stage.getWeightPercent() * stageFraction());
    }

    private double stageFraction() {
        return switch (stage) {
            case SWEEP -> ratio(addressesScanned, addressesTotal);
            case PORTS -> ratio(portsScanned, devices.size());
            case NAMES -> 0;
            case DETAILS -> ratio(detailsDone, detailsTotal);
        };
    }

    private double ratio(int done, int total) {
        return total == 0 ? 0 : Math.min(1, (double) done / total);
    }

    private List<DiscoveredDevice> sortedDevices() {
        return devices.values().stream()
                .sorted(Comparator.comparingInt(device -> Integer.parseInt(device.ip().substring(device.ip().lastIndexOf('.') + 1))))
                .toList();
    }
}
