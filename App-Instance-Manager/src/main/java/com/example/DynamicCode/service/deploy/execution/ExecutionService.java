package com.example.DynamicCode.service.deploy.execution;

import com.example.DynamicCode.databaseservice.deploy.RemoteProgramService;
import com.example.DynamicCode.databaseservice.deploy.RemoteSerwerService;
import com.example.DynamicCode.model.entity.deploy.RemoteProgramConfiguration;
import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.notification.FrontendNotificationService;
import com.example.DynamicCode.service.deploy.connection.SshSessionFactory; // Import Twojej klasy SSH
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionService {

    private final RemoteSerwerService remoteSerwerService;
    private final RemoteProgramService remoteProgramService;
    private final SshSessionFactory sshSessionFactory;
    private final FrontendNotificationService frontendNotificationService;;

    public String executeCommandOnRemoteServer(String command, Long programConfigId) {
        try {
            RemoteProgramConfiguration remoteProgramConfiguration =
                    remoteProgramService.getConfigurationById(programConfigId);

            Long serwerId = remoteProgramConfiguration.getIdSerwer();
            String path = remoteProgramConfiguration.getPathInServer();

            RemoteSerwerConfiguration serwerConfig = remoteSerwerService.getConfigById(serwerId);

            String fullCommand = "cd " + path + " && " + command;
            System.out.println("Pełne polecenie do wykonania: " + fullCommand);

            String result = sshSessionFactory.executeCommand(
                    serwerConfig.getIp(),
                    serwerConfig.getUser(),
                    serwerConfig.getPass(),
                    fullCommand
            );
            frontendNotificationService.sendToFrontend(result);
            return result;

        } catch (Exception e) {
            log.error("Nie udało się wykonać polecenia dla programu o ID: {}", programConfigId, e);
            return "BŁĄD WYKONANIA: " + e.getMessage();
        }
    }
}