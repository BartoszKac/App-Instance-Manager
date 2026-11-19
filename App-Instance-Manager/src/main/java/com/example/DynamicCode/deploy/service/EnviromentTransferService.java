package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.TransferResult;
import com.example.DynamicCode.deploy.dto.TransferTask;
import com.example.DynamicCode.deploy.entity.RemoteProgramConfiguration;
import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import com.example.DynamicCode.deploy.exception.RemoteWriteException;
import com.example.DynamicCode.deploy.exception.SshConnectionException;
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

    public List<TransferResult> initiateUpload(List<TransferTask> transferTaskList) {
        if (transferTaskList == null) {
            return List.of();
        }
        return transferTaskList.stream().map(this::processSingleTask).toList();
    }

    public void transferEnvironment(Long mainClassId, Long idConfiguration, UploadStrategyType uploadStrategyType) {
        RemoteSerwerConfiguration server = remoteSerwerService.getConfigById(idConfiguration);
        String remotePath = remoteDirectoryService.getRemoteDirectoryPath(server.getOperationSystem());
        RemoteTarget target = new RemoteTarget(server.getIp(), server.getUser(), server.getPass(), remotePath);
        executeSshUpload(target, uploadStrategyType, mainClassId);
        saveProgramConfiguration(new TransferTask(mainClassId, idConfiguration, uploadStrategyType), remotePath);
    }

    private TransferResult processSingleTask(TransferTask task) {
        try {
            transferEnvironment(task.mainClassId(), task.configurationId(), task.uploadStrategyType());
            return new TransferResult(task.mainClassId(), task.configurationId(), true, TransferFailureMessages.SENT);
        } catch (RuntimeException e) {
            log.warn("Transfer of id {} with strategy {} to server {} failed",
                    task.mainClassId(), task.uploadStrategyType(), task.configurationId(), e);
            return new TransferResult(task.mainClassId(), task.configurationId(), false, TransferFailureMessages.messageFor(e));
        }
    }

    private void saveProgramConfiguration(TransferTask task, String remotePath) {
        RemoteProgramConfiguration configuration = RemoteProgramConfiguration.builder()
                .idSerwer(task.configurationId())
                .uploadStrategyType(task.uploadStrategyType())
                .idCode(task.mainClassId())
                .pathInServer(remotePath)
                .build();
        remoteProgramService.saveAllConfigurationsToDb(List.of(configuration));
    }

    private void executeSshUpload(RemoteTarget target, UploadStrategyType uploadStrategyType, Long mainClassId) {
        try {
            sshFileTransferService.uploadWithStrategy(target, uploadStrategyType, mainClassId);
        } catch (JSchException e) {
            throw new SshConnectionException(e);
        } catch (SftpException e) {
            throw new RemoteWriteException(e);
        }
    }
}
