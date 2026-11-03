package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.code.service.LanguageProcess;
import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.NetworkScanResponse;
import com.example.DynamicCode.deploy.exception.InvalidSubnetException;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import com.example.DynamicCode.deploy.exception.NetworkScanInProgressException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkScanService {

    private static final Duration SCAN_TIMEOUT = Duration.ofMinutes(4);
    private static final String PROBED_PORTS = "22,80,135,139,443,445,3389,8080,9100,62078";
    private static final String SCAN_FAILED_MESSAGE =
            "Skan się nie udał. Sprawdź, czy nmap jest zainstalowany i dostępny w PATH.";

    private final LanguageProcess languageProcess;
    private final LocalSubnetService localSubnetService;
    private final NmapXmlParser nmapXmlParser;
    private final ReentrantLock scanLock = new ReentrantLock();

    public NetworkScanResponse scan(String cidr) {
        if (localSubnetService.listSubnets().stream().noneMatch(subnet -> subnet.equals(cidr))) {
            throw new InvalidSubnetException();
        }
        if (!scanLock.tryLock()) {
            throw new NetworkScanInProgressException();
        }
        try {
            return toResponse(cidr, nmapXmlParser.parse(runNmap(cidr)));
        } finally {
            scanLock.unlock();
        }
    }

    private String runNmap(String cidr) {
        List<String> command = List.of("nmap", "-Pn", "-sT", "-p", PROBED_PORTS, "--open",
                "-T4", "--max-retries", "1", "-oX", "-", cidr);
        try {
            return languageProcess.captureOutput(command, SCAN_TIMEOUT);
        } catch (IOException e) {
            throw new NetworkScanFailedException(SCAN_FAILED_MESSAGE, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkScanFailedException(SCAN_FAILED_MESSAGE, e);
        }
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
