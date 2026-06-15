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
import java.util.Base64;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshFileTransferService {

    private static final int CONNECTION_TIMEOUT_MS = 10000;

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            ".java", ".class", ".kt", ".groovy",
            ".properties", ".yml", ".yaml", ".xml", ".json",
            ".sh", ".bat", ".jar", ".war"
    );

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

        if (files == null || files.isEmpty()) {
            log.warn("SshFileTransferService: Brak plików do przesłania dla mainClassId={} i strategii {}.", mainClassId, strategyType);
            return;
        }

        log.info("SshFileTransferService: Pobrano {} plik(ów) z DB, rozpoczynam transfer SFTP...", files.size());

        Session session = sshSessionFactory.createSession(remoteIp, user, password);
        ChannelSftp sftpChannel = null;

        try {
            session.connect(CONNECTION_TIMEOUT_MS);
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            for (Map.Entry<String, String> entry : files.entrySet()) {
                processAndUploadFile(sftpChannel, remoteDirectory, entry.getKey(), entry.getValue());
            }

            log.info("SshFileTransferService: Transfer zakończony pomyślnie. Wysłano {} plik(ów).", files.size());

        } catch (Exception e) {
            log.error("Wystąpił błąd podczas transferu plików przez SFTP", e);
            throw e;
        } finally {
            closeSftpChannel(sftpChannel);
            closeSession(session);
        }
    }

    private void processAndUploadFile(ChannelSftp sftpChannel, String remoteDirectory, String fileName, String fileContent) throws SftpException {
        if (fileContent == null) {
            log.warn("Pominięto plik {}, ponieważ jego zawartość jest NULL.", fileName);
            return;
        }

        String lowerCaseFileName = fileName.toLowerCase();
        boolean isSupported = SUPPORTED_EXTENSIONS.stream().anyMatch(lowerCaseFileName::endsWith);
        if (!isSupported) {
            log.warn("Wykryto plik o nieznanym rozszerzeniu: {}. Próba przesłania mimo to...", fileName);
        }

        String normalizedDir = remoteDirectory.endsWith("/") ? remoteDirectory : remoteDirectory + "/";
        String remotePath = normalizedDir + fileName;

        byte[] rawBytes = resolveFileBytes(fileName, fileContent);

        uploadToSftp(sftpChannel, remotePath, rawBytes);
    }

    private byte[] resolveFileBytes(String fileName, String fileContent) {
        if (fileContent == null) {
            return new byte[0];
        }

        String lowerCaseFileName = fileName.toLowerCase();

        if (lowerCaseFileName.endsWith(".class") || lowerCaseFileName.endsWith(".jar")) {
            String textForBase64 = fileContent.trim();
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(textForBase64);
                log.info("Pomyślnie zdekodowano plik binarny z Base64: {}", fileName);
                return decodedBytes;
            } catch (IllegalArgumentException e) {
                log.warn("Plik {} nie jest w formacie Base64. Przesyłam surowe bajty ISO_8859_1.", fileName);
            }
        }

        return fileContent.getBytes(StandardCharsets.ISO_8859_1);
    }

    private void uploadToSftp(ChannelSftp sftpChannel, String remotePath, byte[] bytes) throws SftpException {
        try (InputStream contentStream = new ByteArrayInputStream(bytes)) {
            sftpChannel.put(contentStream, remotePath);
            log.info("Pomyślnie przesłano plik na serwer zdalny: {}", remotePath);
        } catch (Exception e) {
            log.error("Błąd zapisu strumienia dla ścieżki: {}", remotePath, e);
            if (e instanceof SftpException) {
                throw (SftpException) e;
            }
            throw new RuntimeException(e);
        }
    }

    private void closeSftpChannel(ChannelSftp sftpChannel) {
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.exit();
        }
    }

    private void closeSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}