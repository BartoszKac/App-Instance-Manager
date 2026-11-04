package com.example.DynamicCode.deploy.dto;

import com.example.DynamicCode.deploy.enums.OperationSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DeviceDetailsResponse(
        String ip,
        String hostname,
        String macAddress,
        String osName,
        OperationSystem osFamily,
        String deviceType,
        List<DiscoveredService> services,
        List<ScriptResult> hostScripts) {

    public static DeviceDetailsResponse empty(String ip) {
        return new DeviceDetailsResponse(ip, null, null, null, null, null, List.of(), List.of());
    }

    public DeviceDetailsResponse merge(DeviceDetailsResponse other) {
        boolean otherOsMoreSpecific = other.osName != null && (osName == null || other.osName.length() > osName.length());
        return new DeviceDetailsResponse(
                ip,
                hostname != null ? hostname : other.hostname,
                macAddress != null ? macAddress : other.macAddress,
                otherOsMoreSpecific ? other.osName : osName,
                otherOsMoreSpecific ? other.osFamily : osFamily,
                deviceType != null ? deviceType : other.deviceType,
                mergedServices(other),
                mergedScripts(other));
    }

    private List<DiscoveredService> mergedServices(DeviceDetailsResponse other) {
        Map<Integer, DiscoveredService> byPort = new LinkedHashMap<>();
        services.forEach(service -> byPort.put(service.port(), service));
        other.services.forEach(service -> byPort.putIfAbsent(service.port(), service));
        List<DiscoveredService> merged = new ArrayList<>(byPort.values());
        merged.sort(Comparator.comparingInt(DiscoveredService::port));
        return merged;
    }

    private List<ScriptResult> mergedScripts(DeviceDetailsResponse other) {
        Map<String, ScriptResult> byId = new LinkedHashMap<>();
        hostScripts.forEach(script -> byId.put(script.id(), script));
        other.hostScripts.forEach(script -> byId.putIfAbsent(script.id(), script));
        return List.copyOf(byId.values());
    }
}
