package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.NetworkScanResponse;
import com.example.DynamicCode.deploy.exception.InvalidScanTargetException;
import com.example.DynamicCode.deploy.exception.InvalidSubnetException;
import com.example.DynamicCode.deploy.exception.NetworkScanInProgressException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkScanService {

    private static final Duration SWEEP_TIMEOUT = Duration.ofMinutes(1);
    private static final Duration PORTS_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration DETAILS_TIMEOUT = Duration.ofMinutes(2);
    private static final int MAX_PARALLEL_DETAILS = 3;

    private final NmapRunner nmapRunner;
    private final LocalSubnetService localSubnetService;
    private final NmapXmlParser nmapXmlParser;
    private final NmapDetailsParser nmapDetailsParser;
    private final ReentrantLock scanLock = new ReentrantLock();
    private final Semaphore detailPermits = new Semaphore(MAX_PARALLEL_DETAILS);

    public NetworkScanResponse scan(String cidr) {
        if (localSubnetService.listSubnets().stream().noneMatch(subnet -> subnet.equals(cidr))) {
            throw new InvalidSubnetException();
        }
        if (!scanLock.tryLock()) {
            throw new NetworkScanInProgressException();
        }
        try {
            return toResponse(cidr, withOpenPorts(sweep(cidr)));
        } finally {
            scanLock.unlock();
        }
    }

    public DeviceDetailsResponse details(String ip) {
        if (!localSubnetService.isAllowedHost(ip)) {
            throw new InvalidScanTargetException();
        }
        if (!detailPermits.tryAcquire()) {
            throw new NetworkScanInProgressException();
        }
        try {
            return nmapDetailsParser.parse(ip, nmapRunner.run(NmapCommands.details(ip), DETAILS_TIMEOUT));
        } finally {
            detailPermits.release();
        }
    }

    private List<DiscoveredDevice> sweep(String cidr) {
        return nmapXmlParser.parseLiveHosts(nmapRunner.run(NmapCommands.sweep(cidr), SWEEP_TIMEOUT));
    }

    private List<DiscoveredDevice> withOpenPorts(List<DiscoveredDevice> devices) {
        if (devices.isEmpty()) {
            return devices;
        }
        List<String> hosts = devices.stream().map(DiscoveredDevice::ip).toList();
        String xml = nmapRunner.run(NmapCommands.openPorts(hosts), PORTS_TIMEOUT);
        Map<String, List<Integer>> openPorts = nmapXmlParser.parseOpenPorts(xml);
        return devices.stream()
                .map(device -> device.withOpenPorts(openPorts.getOrDefault(device.ip(), List.of())))
                .toList();
    }

    private NetworkScanResponse toResponse(String cidr, List<DiscoveredDevice> devices) {
        log.info("Network scan of {} found {} devices", cidr, devices.size());
        List<DiscoveredDevice> sorted = devices.stream()
                .sorted(Comparator.comparingInt(device -> lastOctet(device.ip())))
                .toList();
        return new NetworkScanResponse(cidr, sorted.size(), sorted);
    }

    private int lastOctet(String ip) {
        return Integer.parseInt(ip.substring(ip.lastIndexOf('.') + 1));
    }
}
