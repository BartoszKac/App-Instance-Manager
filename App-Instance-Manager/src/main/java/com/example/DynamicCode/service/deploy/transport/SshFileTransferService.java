package com.example.DynamicCode.service.deploy.transport;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.factory.deploy.sshfiiletransfer.FileUploadStrategyFactory;
import com.example.DynamicCode.service.deploy.connection.SshSessionFactory;

import com.example.DynamicCode.strategy.sshfiiletransfer.FileUploadStrategy;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshFileTransferService {

    private static final int CONNECTION_TIMEOUT_MS = 10000;

    private final SshSessionFactory sshSessionFactory;
    private final FileUploadStrategyFactory strategyFactory;

    public void uploadWithStrategy(String remoteIp,
                                   String user,
                                   String password,
                                   String remoteDirectory,
                                   UploadStrategyType strategyType,
                                   Long mainClassId) throws JSchException, SftpException {


        log.info("SshFileTransferService: Rozpoczynam upload ze strategią={}, mainClassId={}, cel={}@{}:{}",
                strategyType, mainClassId, user, remoteIp, remoteDirectory);

        FileUploadStrategy strategy = strategyFactory.getStrategy(strategyType);

        Map<String, String> files = strategy.resolveFiles(mainClassId);

        if (files == null) {
            System.out.println("[SOUT] UWAGA: Mapa 'files' jest NULL!");
            return;
        }

        if (files.isEmpty()) {
            log.warn("SshFileTransferService: Strategia {} nie zwróciła żadnych plików dla mainClassId={}. Upload pominięty.",
                    strategyType, mainClassId);
            return;
        }

        log.info("SshFileTransferService: Pobrano {} plik(ów) z DB, rozpoczynam transfer SFTP...", files.size());

        Session session = sshSessionFactory.createSession(remoteIp, user, password);
        ChannelSftp sftpChannel = null;
        try {
            session.connect(CONNECTION_TIMEOUT_MS);

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            int index = 1;
            for (Map.Entry<String, String> entry : files.entrySet()) {
                String fileName = entry.getKey();
                String fileContent = entry.getValue();
                String remotePath = remoteDirectory + fileName;


                if (fileContent == null) {
                    continue;
                }

                InputStream contentStream = new ByteArrayInputStream(
                        fileContent.getBytes(StandardCharsets.UTF_8)
                );

                sftpChannel.put(contentStream, remotePath);
                index++;
            }

            log.info("SshFileTransferService: Transfer zakończony pomyślnie. Wysłano {} plik(ów).", files.size());

        } catch (Exception e) {

            e.printStackTrace();
            throw e; // Rzucamy dalej, żeby zachować oryginalne zachowanie metody
        } finally {
            if (sftpChannel != null) {
                if (sftpChannel.isConnected()) {
                    sftpChannel.exit();
                }
            }
            if (session != null) {
                if (session.isConnected()) {
                    session.disconnect();
                }
            }
        }
    }
}