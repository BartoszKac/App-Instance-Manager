package com.example.DynamicCode.service.deploy.transport;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.factory.sshfiiletransfer.FileUploadStrategyFactory;
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

    /**
     * Wysyła pliki na zdalny serwer SFTP, pobierając ich zawartość
     * bezpośrednio z bazy danych przez wybraną strategię.
     * Żaden plik nie jest czytany z dysku lokalnego.
     *
     * @param remoteIp        adres IP zdalnego serwera
     * @param user            użytkownik SSH
     * @param password        hasło SSH
     * @param remoteDirectory katalog docelowy na serwerze (np. "/home/user/app/")
     * @param strategyType    typ strategii (SOURCE_CODE, COMPILED_CODE, REMOTE_EXECUTABLE)
     * @param mainClassId     ID projektu — filtr rekordów z DB
     */
    public void uploadWithStrategy(String remoteIp,
                                   String user,
                                   String password,
                                   String remoteDirectory,
                                   UploadStrategyType strategyType,
                                   Long mainClassId) throws JSchException, SftpException {

        log.info("SshFileTransferService: Rozpoczynam upload ze strategią={}, mainClassId={}, cel={}@{}:{}",
                strategyType, mainClassId, user, remoteIp, remoteDirectory);

        // 1. Pobierz strategię z fabryki
        FileUploadStrategy strategy = strategyFactory.getStrategy(strategyType);

        // 2. Strategia zwraca mapę: nazwa -> zawartość (wszystko z DB)
        Map<String, String> files = strategy.resolveFiles(mainClassId);

        if (files.isEmpty()) {
            log.warn("SshFileTransferService: Strategia {} nie zwróciła żadnych plików dla mainClassId={}. Upload pominięty.",
                    strategyType, mainClassId);
            return;
        }

        log.info("SshFileTransferService: Pobrano {} plik(ów) z DB, rozpoczynam transfer SFTP...", files.size());

        // 3. Otwórz sesję SSH i wyślij każdy plik jako strumień bajtów (bez dysku)
        Session session = sshSessionFactory.createSession(remoteIp, user, password);
        ChannelSftp sftpChannel = null;
        try {
            session.connect(CONNECTION_TIMEOUT_MS);
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            for (Map.Entry<String, String> entry : files.entrySet()) {
                String fileName = entry.getKey();
                String fileContent = entry.getValue();
                String remotePath = remoteDirectory + fileName;

                InputStream contentStream = new ByteArrayInputStream(
                        fileContent.getBytes(StandardCharsets.UTF_8)
                );

                log.info("SshFileTransferService: Wysyłam plik z DB -> {}", remotePath);
                sftpChannel.put(contentStream, remotePath);
            }

            log.info("SshFileTransferService: Transfer zakończony pomyślnie. Wysłano {} plik(ów).", files.size());

        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.exit();
            }
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }
}