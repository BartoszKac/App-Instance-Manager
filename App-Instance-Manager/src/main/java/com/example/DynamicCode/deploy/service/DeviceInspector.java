package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredService;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceInspector {

    private static final Duration COMMAND_TIMEOUT = Duration.ofSeconds(100);
    private static final List<Integer> PREFERRED_HOST_SCRIPT_PORTS = List.of(445, 139);

    private final NmapRunner nmapRunner;
    private final NmapDetailsParser nmapDetailsParser;

    public List<Supplier<DeviceDetailsResponse>> inspections(String ip, List<Integer> ports) {
        List<Supplier<DeviceDetailsResponse>> inspections = new ArrayList<>();
        ports.forEach(port -> inspections.add(() -> inspectPort(ip, port)));
        if (!ports.isEmpty()) {
            inspections.add(() -> inspectHost(ip, hostScriptPort(ports)));
        }
        return inspections;
    }

    private DeviceDetailsResponse inspectPort(String ip, int port) {
        DeviceDetailsResponse parsed = parseSafely(ip, NmapCommands.portDetails(ip, port));
        if (!parsed.services().isEmpty()) {
            return parsed;
        }
        DiscoveredService unrecognized = new DiscoveredService(port, null, null, null, null, null, null, List.of(), List.of());
        return parsed.merge(new DeviceDetailsResponse(ip, null, null, null, null, null, List.of(unrecognized), List.of()));
    }

    private DeviceDetailsResponse inspectHost(String ip, int port) {
        DeviceDetailsResponse parsed = parseSafely(ip, NmapCommands.hostDetails(ip, port));
        return new DeviceDetailsResponse(ip, parsed.hostname(), parsed.macAddress(), parsed.osName(), parsed.osFamily(),
                null, List.of(), parsed.hostScripts());
    }

    private DeviceDetailsResponse parseSafely(String ip, List<String> command) {
        try {
            return nmapDetailsParser.parse(ip, nmapRunner.run(command, COMMAND_TIMEOUT));
        } catch (NetworkScanFailedException e) {
            log.warn("Inspection of {} failed", ip, e);
            return DeviceDetailsResponse.empty(ip);
        }
    }

    private int hostScriptPort(List<Integer> ports) {
        return PREFERRED_HOST_SCRIPT_PORTS.stream().filter(ports::contains).findFirst().orElse(ports.get(0));
    }
}
