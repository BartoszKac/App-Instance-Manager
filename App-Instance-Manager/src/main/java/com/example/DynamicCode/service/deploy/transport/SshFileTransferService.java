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

        System.out.println("[SOUT] --- START METODY uploadWithStrategy ---");
        System.out.println("[SOUT] Parametry: IP=" + remoteIp + ", User=" + user + ", Dir=" + remoteDirectory + ", Strategy=" + strategyType + ", MainClassId=" + mainClassId);

        log.info("SshFileTransferService: Rozpoczynam upload ze strategią={}, mainClassId={}, cel={}@{}:{}",
                strategyType, mainClassId, user, remoteIp, remoteDirectory);

        System.out.println("[SOUT] Pobieram strategie z fabryki...");
        FileUploadStrategy strategy = strategyFactory.getStrategy(strategyType);
        System.out.println("[SOUT] Strategia pobrana: " + (strategy != null ? strategy.getClass().getSimpleName() : "NULL!"));

        System.out.println("[SOUT] Pobieram pliki ze strategii dla ID: " + mainClassId);
        Map<String, String> files = strategy.resolveFiles(mainClassId);

        if (files == null) {
            System.out.println("[SOUT] UWAGA: Mapa 'files' jest NULL!");
            return;
        }
        System.out.println("[SOUT] Pobrano mapę plików. Rozmiar: " + files.size());

        if (files.isEmpty()) {
            System.out.println("[SOUT] Mapa plików jest pusta. Przerywam działanie.");
            log.warn("SshFileTransferService: Strategia {} nie zwróciła żadnych plików dla mainClassId={}. Upload pominięty.",
                    strategyType, mainClassId);
            return;
        }

        log.info("SshFileTransferService: Pobrano {} plik(ów) z DB, rozpoczynam transfer SFTP...", files.size());

        System.out.println("[SOUT] Tworzę sesję SSH...");
        Session session = sshSessionFactory.createSession(remoteIp, user, password);
        ChannelSftp sftpChannel = null;
        try {
            System.out.println("[SOUT] Łączę z sesją SSH (timeout: " + CONNECTION_TIMEOUT_MS + "ms)...");
            session.connect(CONNECTION_TIMEOUT_MS);
            System.out.println("[SOUT] Sesja SSH połączona. Otwieram kanał SFTP...");

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            System.out.println("[SOUT] Łączę z kanałem SFTP...");
            sftpChannel.connect();
            System.out.println("[SOUT] Kanał SFTP połączony pomyślnie.");

            int index = 1;
            for (Map.Entry<String, String> entry : files.entrySet()) {
                String fileName = entry.getKey();
                String fileContent = entry.getValue();
                String remotePath = remoteDirectory + fileName;

                System.out.println("[SOUT] Plik #" + index + " -> Nazwa: " + fileName + ", Ścieżka docelowa: " + remotePath);
                System.out.println("[SOUT] Rozmiar zawartości (znaki): " + (fileContent != null ? fileContent.length() : "NULL!"));

                if (fileContent == null) {
                    System.out.println("[SOUT] Pomijam plik " + fileName + " bo zawartość to NULL.");
                    continue;
                }

                InputStream contentStream = new ByteArrayInputStream(
                        fileContent.getBytes(StandardCharsets.UTF_8)
                );

                log.info("SshFileTransferService: Wysyłam plik z DB -> {}", remotePath);
                System.out.println("[SOUT] Wykonuję sftpChannel.put()...");
                sftpChannel.put(contentStream, remotePath);
                System.out.println("[SOUT] Plik #" + index + " wysłany pomyślnie.");
                index++;
            }

            log.info("SshFileTransferService: Transfer zakończony pomyślnie. Wysłano {} plik(ów).", files.size());
            System.out.println("[SOUT] Wszystkie pliki zostały przetworzone w bloku try.");

        } catch (Exception e) {
            System.out.println("[SOUT] !!! ZŁAPANO WYJĄTEK W BLOKU TRY !!!");
            System.out.println("[SOUT] Typ wyjątku: " + e.getClass().getName());
            System.out.println("[SOUT] Wiadomość: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rzucamy dalej, żeby zachować oryginalne zachowanie metody
        } finally {
            System.out.println("[SOUT] Wchodzę do bloku finally. Czyszczenie połączeń...");
            if (sftpChannel != null) {
                System.out.println("[SOUT] Stan kanału SFTP: connected=" + sftpChannel.isConnected());
                if (sftpChannel.isConnected()) {
                    System.out.println("[SOUT] Zamykam kanał SFTP...");
                    sftpChannel.exit();
                }
            }
            if (session != null) {
                System.out.println("[SOUT] Stan sesji SSH: connected=" + session.isConnected());
                if (session.isConnected()) {
                    System.out.println("[SOUT] Zamykam sesję SSH...");
                    session.disconnect();
                }
            }
            System.out.println("[SOUT] --- KONIEC METODY uploadWithStrategy ---");
        }
    }
}