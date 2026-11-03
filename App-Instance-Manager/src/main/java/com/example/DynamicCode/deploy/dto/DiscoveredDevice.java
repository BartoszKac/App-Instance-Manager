package com.example.DynamicCode.deploy.dto;

public record DiscoveredDevice(String ip, String hostname, String macAddress, String vendor, boolean sshOpen) {
}
