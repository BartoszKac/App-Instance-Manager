package com.example.DynamicCode.deploy.dto;

import com.example.DynamicCode.deploy.enums.ScanStage;
import com.example.DynamicCode.deploy.enums.ScanState;

import java.util.List;
import java.util.Map;

public record ScanJobResponse(
        String id,
        String cidr,
        ScanState state,
        ScanStage stage,
        int percent,
        int addressesTotal,
        int addressesScanned,
        int portsScanned,
        int detailsTotal,
        int detailsDone,
        int deviceCount,
        List<DiscoveredDevice> devices,
        Map<String, DeviceDetailsResponse> details,
        String error) {
}
