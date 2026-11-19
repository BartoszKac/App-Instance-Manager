package com.example.DynamicCode.deploy.controller;

import com.example.DynamicCode.deploy.dto.DeployableFilesResponse;
import com.example.DynamicCode.deploy.dto.ExecuteDto;
import com.example.DynamicCode.deploy.dto.TransferResult;
import com.example.DynamicCode.deploy.dto.TransferTask;
import com.example.DynamicCode.deploy.entity.RemoteProgramConfiguration;
import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import com.example.DynamicCode.deploy.service.ProviderDeployService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deploy")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DeployController {

    private final ProviderDeployService providerDeployService;

    @GetMapping("/servers")
    public ResponseEntity<List<RemoteSerwerConfiguration>> getAllServerConfigurations() {
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

    @DeleteMapping("/server/{idConfiguration}")
    public ResponseEntity<Void> removeServerConfig(@PathVariable Long idConfiguration) {
        providerDeployService.removeServerConfiguration(idConfiguration);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deployable-files")
    public DeployableFilesResponse getDeployableFiles() {
        return providerDeployService.getDeployableFiles();
    }

    @GetMapping("/program/{idConfiguration}")
    public ResponseEntity<RemoteProgramConfiguration> getProgramConfiguration(@PathVariable Long idConfiguration) {
        return ResponseEntity.ok(providerDeployService.getProgramConfiguration(idConfiguration));
    }

    @GetMapping("/programs")
    public ResponseEntity<List<RemoteProgramConfiguration>> getAllConfigurations() {
        return ResponseEntity.ok(providerDeployService.getAllConfigurations());
    }

    @PostMapping("/program/execute")
    public ResponseEntity<String> executeProgramOnRemoteServer(@RequestBody ExecuteDto dto) {
        return ResponseEntity.ok(providerDeployService.executeProgramOnRemoteServer(dto.getCommand(), dto.getProgramConfigId()));
    }

    @PostMapping("/transfer/bulk")
    public List<TransferResult> executeBulkTransfer(@RequestBody List<TransferTask> tasks) {
        return providerDeployService.executeEnvironmentUploads(tasks);
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
