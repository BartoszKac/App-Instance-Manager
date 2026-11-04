package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.ScanJobResponse;
import com.example.DynamicCode.deploy.enums.ScanStage;
import com.example.DynamicCode.deploy.exception.InvalidScanTargetException;
import com.example.DynamicCode.deploy.exception.InvalidSubnetException;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import com.example.DynamicCode.deploy.exception.NetworkScanInProgressException;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkScanService {

    private static final Duration SWEEP_TIMEOUT = Duration.ofMinutes(1);
    private static final Duration PORTS_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration NAMES_TIMEOUT = Duration.ofSeconds(30);
    private static final int SWEEP_CHUNK_SIZE = 16;
    private static final int DNS_PORT = 53;
    private static final int MAX_PARALLEL_PREVIEWS = 3;

    private final NmapRunner nmapRunner;
    private final LocalSubnetService localSubnetService;
    private final NmapXmlParser nmapXmlParser;
    private final DeviceInspector deviceInspector;
    private final Semaphore previewPermits = new Semaphore(MAX_PARALLEL_PREVIEWS);
    private final ExecutorService jobExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService workers = Executors.newFixedThreadPool(6);
    private ScanJob currentJob;

    public synchronized ScanJobResponse startScan(String cidr) {
        if (localSubnetService.listSubnets().stream().noneMatch(subnet -> subnet.equals(cidr))) {
            throw new InvalidSubnetException();
        }
        if (currentJob != null && currentJob.isRunning()) {
            throw new NetworkScanInProgressException();
        }
        ScanJob job = new ScanJob(cidr, LocalSubnetService.hostsOf(cidr).size());
        currentJob = job;
        jobExecutor.submit(() -> run(job));
        return job.snapshot();
    }

    public synchronized Optional<ScanJobResponse> currentScan() {
        return Optional.ofNullable(currentJob).map(ScanJob::snapshot);
    }

    public DeviceDetailsResponse details(String ip) {
        if (!localSubnetService.isAllowedHost(ip)) {
            throw new InvalidScanTargetException();
        }
        if (!previewPermits.tryAcquire()) {
            throw new NetworkScanInProgressException();
        }
        try {
            return preview(ip);
        } finally {
            previewPermits.release();
        }
    }

    @PreDestroy
    void shutdown() {
        jobExecutor.shutdownNow();
        workers.shutdownNow();
    }

    private DeviceDetailsResponse preview(String ip) {
        List<Integer> ports = scanPorts(ip);
        List<DeviceDetailsResponse> parts = new ArrayList<>();
        inParallel(deviceInspector.inspections(ip, ports), inspection -> {
            DeviceDetailsResponse part = inspection.get();
            synchronized (parts) {
                parts.add(part);
            }
        });
        DeviceDetailsResponse merged = parts.stream().reduce(DeviceDetailsResponse.empty(ip), DeviceDetailsResponse::merge);
        return new DeviceDetailsResponse(ip, merged.hostname(), merged.macAddress(), merged.osName(), merged.osFamily(),
                DeviceClassifier.deviceType(ports, merged.hostname()), merged.services(), merged.hostScripts());
    }

    private void run(ScanJob job) {
        try {
            String cidr = job.snapshot().cidr();
            sweep(job, LocalSubnetService.hostsOf(cidr));
            scanAllPorts(job);
            resolveIdentities(job);
            inspectAll(job);
            job.complete();
            log.info("Network scan of {} found {} devices", cidr, job.snapshot().deviceCount());
        } catch (RuntimeException e) {
            log.warn("Network scan failed", e);
            job.fail(e instanceof NetworkScanFailedException ? e.getMessage() : "Skan się nie udał.");
        }
    }

    private void sweep(ScanJob job, List<String> hosts) {
        inParallel(chunks(hosts), chunk -> {
            String xml = nmapRunner.run(NmapCommands.sweep(chunk), SWEEP_TIMEOUT);
            job.addSweepResult(chunk.size(), nmapXmlParser.parseLiveHosts(xml));
        });
    }

    private void scanAllPorts(ScanJob job) {
        List<DiscoveredDevice> devices = job.enterStage(ScanStage.PORTS);
        inParallel(devices, device -> job.setPorts(device.ip(), tolerantPorts(device.ip())));
    }

    private List<Integer> tolerantPorts(String ip) {
        try {
            return scanPorts(ip);
        } catch (NetworkScanFailedException e) {
            log.warn("Port scan of {} failed", ip, e);
            return List.of();
        }
    }

    private List<Integer> scanPorts(String ip) {
        return nmapXmlParser.parseOpenPorts(nmapRunner.run(NmapCommands.openPorts(ip), PORTS_TIMEOUT));
    }

    private void resolveIdentities(ScanJob job) {
        List<DiscoveredDevice> devices = job.enterStage(ScanStage.NAMES);
        Map<String, String> names = resolveNames(devices);
        devices.forEach(device -> {
            String name = names.get(device.ip());
            String known = name != null ? name : device.hostname();
            job.update(device.ip(), current -> current.withIdentity(name, DeviceClassifier.deviceType(current.openPorts(), known)));
        });
    }

    private Map<String, String> resolveNames(List<DiscoveredDevice> devices) {
        List<String> dnsServers = devices.stream()
                .filter(device -> device.openPorts().contains(DNS_PORT))
                .map(DiscoveredDevice::ip)
                .toList();
        if (dnsServers.isEmpty()) {
            return Map.of();
        }
        try {
            List<String> hosts = devices.stream().map(DiscoveredDevice::ip).toList();
            return nmapXmlParser.parseHostnames(nmapRunner.run(NmapCommands.names(hosts, dnsServers), NAMES_TIMEOUT));
        } catch (NetworkScanFailedException e) {
            log.warn("Reverse DNS lookup failed", e);
            return Map.of();
        }
    }

    private void inspectAll(ScanJob job) {
        List<Runnable> tasks = new ArrayList<>();
        for (DiscoveredDevice device : job.enterStage(ScanStage.DETAILS)) {
            for (Supplier<DeviceDetailsResponse> inspection : deviceInspector.inspections(device.ip(), device.openPorts())) {
                tasks.add(() -> job.addDetails(device.ip(), inspection.get()));
            }
        }
        job.startDetails(tasks.size());
        inParallel(tasks, Runnable::run);
    }

    private <T> void inParallel(List<T> items, Consumer<T> action) {
        List<CompletableFuture<Void>> tasks = items.stream()
                .map(item -> CompletableFuture.runAsync(() -> action.accept(item), workers))
                .toList();
        try {
            CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();
        } catch (CompletionException e) {
            throw e.getCause() instanceof RuntimeException cause ? cause : e;
        }
    }

    private List<List<String>> chunks(List<String> hosts) {
        List<List<String>> chunks = new ArrayList<>();
        for (int start = 0; start < hosts.size(); start += SWEEP_CHUNK_SIZE) {
            chunks.add(hosts.subList(start, Math.min(start + SWEEP_CHUNK_SIZE, hosts.size())));
        }
        return chunks;
    }
}
