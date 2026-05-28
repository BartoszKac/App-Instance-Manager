package com.example.DynamicCode.controller.test;
import com.example.DynamicCode.databaseservice.deploy.RemoteSerwerService;
import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.service.deploy.serwer.SerwerConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
@Profile("dev")
public class SerwerConfigurationController {

    private final SerwerConfigurationService serwerConfigurationService;
    private final RemoteSerwerService remoteSerwerService;

    /**
     * Endpoint do rejestracji serwera z automatycznym SSH (OS detection).
     * POST http://localhost:8080/api/servers/register
     */
    @PostMapping("/register")
    public ResponseEntity<RemoteSerwerConfiguration> registerServer(@RequestBody RemoteSerwerConfiguration config) {
        log.info("API: Odebrano żądanie rejestracji serwera dla IP: {}", config.getIp());
        RemoteSerwerConfiguration savedConfig = serwerConfigurationService.registerAndFetchDetails(config);
        return ResponseEntity.ok(savedConfig);
    }

    /**
     * Endpoint do pobierania wszystkich konfiguracji z bazy danych.
     * GET http://localhost:8080/api/servers
     */
    @GetMapping
    public ResponseEntity<List<RemoteSerwerConfiguration>> getAllServers() {
        log.info("API: Żądanie pobrania wszystkich konfiguracji serwerów.");
        List<RemoteSerwerConfiguration> servers = remoteSerwerService.getAllConfigs();
        return ResponseEntity.ok(servers);
    }
}