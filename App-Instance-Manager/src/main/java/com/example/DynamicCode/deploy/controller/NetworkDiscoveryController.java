package com.example.DynamicCode.deploy.controller;

import com.example.DynamicCode.deploy.dto.DeviceDetailsRequest;
import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.NetworkScanRequest;
import com.example.DynamicCode.deploy.dto.ScanJobResponse;
import com.example.DynamicCode.deploy.exception.InvalidScanTargetException;
import com.example.DynamicCode.deploy.exception.InvalidSubnetException;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import com.example.DynamicCode.deploy.exception.NetworkScanInProgressException;
import com.example.DynamicCode.deploy.service.LocalSubnetService;
import com.example.DynamicCode.deploy.service.NetworkScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/deploy/network")
@RequiredArgsConstructor
public class NetworkDiscoveryController {

    private final LocalSubnetService localSubnetService;
    private final NetworkScanService networkScanService;

    @GetMapping("/subnets")
    public List<String> listSubnets() {
        return localSubnetService.listSubnets();
    }

    @PostMapping("/scan")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ScanJobResponse startScan(@RequestBody NetworkScanRequest request) {
        return networkScanService.startScan(request.cidr());
    }

    @GetMapping("/scan/current")
    public ResponseEntity<ScanJobResponse> currentScan() {
        return networkScanService.currentScan()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/device-details")
    public DeviceDetailsResponse deviceDetails(@RequestBody DeviceDetailsRequest request) {
        return networkScanService.details(request.ip());
    }

    @ExceptionHandler({InvalidSubnetException.class, InvalidScanTargetException.class})
    public ProblemDetail handleInvalidTarget(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NetworkScanInProgressException.class)
    public ProblemDetail handleScanInProgress(NetworkScanInProgressException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(NetworkScanFailedException.class)
    public ProblemDetail handleScanFailed(NetworkScanFailedException e) {
        log.warn("Network scan failed", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
}
