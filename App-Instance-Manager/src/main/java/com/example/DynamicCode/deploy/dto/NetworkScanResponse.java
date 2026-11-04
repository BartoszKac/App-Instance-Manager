package com.example.DynamicCode.deploy.dto;

import java.util.List;

public record NetworkScanResponse(String cidr, int deviceCount, List<DiscoveredDevice> devices) {
}
