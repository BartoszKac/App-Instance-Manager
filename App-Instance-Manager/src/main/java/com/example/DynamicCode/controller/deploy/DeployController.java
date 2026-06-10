package com.example.DynamicCode.controller.deploy;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.model.dto.deploy.ExecuteDto;
import com.example.DynamicCode.model.dto.deploy.TransferTask;
import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.model.entity.deploy.RemoteProgramConfiguration; // Upewnij się, że pakiet jest poprawny
import com.example.DynamicCode.service.deploy.provider.ProviderDeployService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/deploy")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeployController {

    private final ProviderDeployService providerDeployService;

    // --- SERWERY ---

    @GetMapping("/servers")
    public ResponseEntity<List<RemoteSerwerConfiguration>> getAllServerConfigurations() {
        log.info("[Provider-Deploy] Pobieranie wszystkich konfiguracji serwerów z bazy danych.");
        return ResponseEntity.ok(providerDeployService.getAllServerConfigurations());
    }

    @PostMapping("/server")
    public ResponseEntity<RemoteSerwerConfiguration> registerServer(@RequestBody RemoteSerwerConfiguration config) {
        return ResponseEntity.ok(providerDeployService.registerNewServerAndFetchDetails(config));
    }

    @PostMapping("/server/bulk")
    public ResponseEntity<Void> registerServersBulk(@RequestBody List<RemoteSerwerConfiguration> configs) {
        providerDeployService.registerBulkServerConfigurations(configs);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/server/{idConfiguration}")
    public ResponseEntity<RemoteSerwerConfiguration> getServerConfig(@PathVariable Long idConfiguration) {
        return ResponseEntity.ok(providerDeployService.getServerConfiguration(idConfiguration));
    }
//
//    @PutMapping("/server")
//    public ResponseEntity<RemoteSerwerConfiguration> updateServerConfig(@RequestBody RemoteSerwerConfiguration updatedConfig) {
//        return ResponseEntity.ok(providerDeployService.updateServerConfiguration(updatedConfig));
//    }

    @DeleteMapping("/server/{idConfiguration}")
    public ResponseEntity<Void> removeServerConfig(@PathVariable Long idConfiguration) {
        providerDeployService.removeServerConfiguration(idConfiguration);
        return ResponseEntity.noContent().build();
    }

    // --- PROGRAMY ---

    @GetMapping("/program/{mainClassId}")
    public ResponseEntity<RemoteProgramConfiguration> getProgramConfiguration(@PathVariable Long mainClassId) {
        log.info("[API-Deploy] Zapytanie HTTP GET o konfigurację programu dla MainClassId: {}", mainClassId);
        return ResponseEntity.ok(providerDeployService.getProgramConfiguration(mainClassId));
    }
    // 3 ekran
    @GetMapping("/programs")
    public ResponseEntity<List<RemoteProgramConfiguration>> getAllConfigurations() {
        log.info("[API-Deploy] Zapytanie HTTP GET o wszystkie konfiguracje programów.");
        return ResponseEntity.ok(providerDeployService.getAllConfigurations());
    }
    @PostMapping("/program/execute")
    public ResponseEntity<String> executeProgramOnRemoteServer(@RequestBody ExecuteDto dto) {
        String result = providerDeployService.executeProgramOnRemoteServer(dto.getCommand(), dto.getProgramConfigId());
        return ResponseEntity.ok(result);
    }
    /// koniec 3 ekranu

    // --- TRANSFERY / WDROŻENIA ---
    @PostMapping("/transfer/bulk")
    public ResponseEntity<Void> executeBulkTransfer(@RequestBody List<TransferTask> tasks) {
        providerDeployService.executeEnvironmentUploads(tasks);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer/single")
    public ResponseEntity<Void> executeSingleTransfer(
            @RequestParam Long mainClassId,
            @RequestParam Long idConfiguration,
            @RequestParam UploadStrategyType strategy) {
        providerDeployService.executeSingleEnvironmentTransfer(mainClassId, idConfiguration, strategy);
        return ResponseEntity.ok().build();
    }
}