package com.example.DynamicCode.deploy.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScanStage {
    SWEEP(0, 20),
    PORTS(20, 25),
    NAMES(45, 5),
    DETAILS(50, 50);

    private final int startPercent;
    private final int weightPercent;
}
