package com.example.DynamicCode.deploy.dto;

import java.util.List;

public record DiscoveredDevice(
        String ip,
        String hostname,
        String macAddress,
        String vendor,
        boolean sshOpen,
        List<Integer> openPorts,
        String deviceType) {

    private static final int SSH_PORT = 22;

    public DiscoveredDevice withOpenPorts(List<Integer> ports) {
        return new DiscoveredDevice(ip, hostname, macAddress, vendor, ports.contains(SSH_PORT), ports, deviceType);
    }

    public DiscoveredDevice withIdentity(String name, String type) {
        return new DiscoveredDevice(ip, name != null ? name : hostname, macAddress, vendor, sshOpen, openPorts, type);
    }
}
