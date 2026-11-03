package com.example.DynamicCode.deploy.dto;

import com.example.DynamicCode.deploy.enums.OperationSystem;

import java.util.List;

public record DeviceDetailsResponse(
        String ip,
        String hostname,
        String osName,
        OperationSystem osFamily,
        String deviceType,
        List<DiscoveredService> services) {
}
