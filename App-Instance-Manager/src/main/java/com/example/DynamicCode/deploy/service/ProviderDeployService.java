package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeployableFilesResponse;
import com.example.DynamicCode.deploy.dto.TransferResult;
import com.example.DynamicCode.deploy.dto.TransferTask;
import com.example.DynamicCode.deploy.entity.RemoteProgramConfiguration;
import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderDeployService {

    private final ExecutionService executionService;
    private final SerwerConfigurationService serwerConfigurationService;
    private final EnviromentTransferService enviromentTransferService;
    private final RemoteProgramService remoteProgramService;
    private final DeployableFilesService deployableFilesService;

    public RemoteSerwerConfiguration registerNewServerAndFetchDetails(RemoteSerwerConfiguration config) {
        log.info("Registering server {}", config.getIp());
        return serwerConfigurationService.registerAndFetchDetails(config);
    }

    public void registerBulkServerConfigurations(List<RemoteSerwerConfiguration> configs) {
        log.info("Registering {} servers in bulk", configs.size());
        serwerConfigurationService.registerConfigurations(configs);
    }

    public RemoteSerwerConfiguration getServerConfiguration(Long idConfiguration) {
        return serwerConfigurationService.getConfiguration(idConfiguration);
    }

    public List<RemoteSerwerConfiguration> getAllServerConfigurations() {
        return serwerConfigurationService.getAllConfigurations();
    }

    public RemoteSerwerConfiguration updateServerConfiguration(RemoteSerwerConfiguration updatedConfig) {
        log.info("Updating server {}", updatedConfig.getIdConfiguration());
        return serwerConfigurationService.updateConfiguration(updatedConfig);
    }

    public void removeServerConfiguration(Long idConfiguration) {
        log.info("Removing server {}", idConfiguration);
        serwerConfigurationService.removeConfiguration(idConfiguration);
    }

    public List<TransferResult> executeEnvironmentUploads(List<TransferTask> transferTaskList) {
        log.info("Starting {} transfer task(s)", transferTaskList.size());
        return enviromentTransferService.initiateUpload(transferTaskList);
    }

    public void executeSingleEnvironmentTransfer(Long mainClassId, Long idConfiguration, UploadStrategyType uploadStrategyType) {
        log.info("Starting single transfer of id {} to server {}", mainClassId, idConfiguration);
        enviromentTransferService.transferEnvironment(mainClassId, idConfiguration, uploadStrategyType);
    }

    public DeployableFilesResponse getDeployableFiles() {
        return deployableFilesService.getDeployableFiles();
    }

    public RemoteProgramConfiguration getProgramConfiguration(Long idConfiguration) {
        return remoteProgramService.getConfigurationById(idConfiguration);
    }

    public List<RemoteProgramConfiguration> getAllConfigurations() {
        return remoteProgramService.getAllConfigurations();
    }

    public String executeProgramOnRemoteServer(String command, Long idConfiguration) {
        log.info("Executing a command for program configuration {}", idConfiguration);
        return executionService.executeCommandOnRemoteServer(command, idConfiguration);
    }
}
