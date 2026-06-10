package com.example.DynamicCode.service.deploy.transport;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.databaseservice.deploy.RemoteProgramService;
import com.example.DynamicCode.databaseservice.deploy.RemoteSerwerService;
import com.example.DynamicCode.model.dto.deploy.TransferTask;
import com.example.DynamicCode.model.entity.deploy.RemoteProgramConfiguration;
import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.service.deploy.storage.RemoteDirectoryService;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnviromentTransferService {

    private final SshFileTransferService sshFileTransferService;
    private final RemoteDirectoryService remoteDirectoryService;
    private final RemoteSerwerService remoteSerwerService;
    private final RemoteProgramService remoteProgramService;

    public void initiateUpload(List<TransferTask> transferTaskList) {
        if (transferTaskList == null || transferTaskList.isEmpty()) {
            log.warn("Lista zadań transferu jest pusta.");
            return;
        }

        log.info("Rozpoczynam przetwarzanie {} zadań transferu...", transferTaskList.size());

        for (TransferTask task : transferTaskList) {
            processSingleTask(task);
        }

        log.info("Zakończono przetwarzanie listy transferów.");
    }

    private void processSingleTask(TransferTask task) {
        try {
            log.info("Uruchamiam transfer dla klasy: {}, konfiguracji: {}, strategii: {}",
                    task.mainClassId(), task.configurationId(), task.uploadStrategyType());

            transferEnvironment(task.mainClassId(), task.configurationId(), task.uploadStrategyType());

            log.info("Transfer dla klasy {} zakończony sukcesem.", task.mainClassId());
        } catch (Exception e) {
            log.error("Błąd podczas transferu dla konfiguracji o ID: {}. Powód: {}",
                    task.configurationId(), e.getMessage(), e);
        }
    }

    public void transferEnvironment(Long mainClassId, Long idConfiguration, UploadStrategyType uploadStrategyType) {
        RemoteSerwerConfiguration remoteSerwerConfiguration = remoteSerwerService.getConfigById(idConfiguration);

        String remotePath = remoteDirectoryService.getRemoteDirectoryPath(remoteSerwerConfiguration.getOperationSystem());

        saveProgramConfiguration(mainClassId, idConfiguration,  uploadStrategyType, remotePath);

        executeSshUpload(mainClassId, remoteSerwerConfiguration, uploadStrategyType, remotePath);
    }

    private void saveProgramConfiguration(Long mainClassId, Long idConfiguration,  UploadStrategyType uploadStrategyType, String remotePath) {
        RemoteProgramConfiguration remoteProgramConfiguration = RemoteProgramConfiguration.builder()
                .idSerwer(idConfiguration)
                .uploadStrategyType(uploadStrategyType)
                .idCode(mainClassId)
                .pathInServer(remotePath)
                .build();

        remoteProgramService.saveAllConfigurationsToDb(List.of(remoteProgramConfiguration));
    }

    private void executeSshUpload(Long mainClassId, RemoteSerwerConfiguration remoteSerwerConfiguration, UploadStrategyType uploadStrategyType, String remotePath) {
        try {
            sshFileTransferService.uploadWithStrategy(
                    remoteSerwerConfiguration.getIp(),
                    remoteSerwerConfiguration.getUser(),
                    remoteSerwerConfiguration.getPass(),
                    remotePath,
                    uploadStrategyType,
                    mainClassId
            );
        } catch (JSchException e) {
            throw new RuntimeException("Błąd protokołu SSH podczas transferu", e);
        } catch (SftpException e) {
            throw new RuntimeException("Błąd protokołu SFTP podczas przesyłania pliku", e);
        }
    }
}