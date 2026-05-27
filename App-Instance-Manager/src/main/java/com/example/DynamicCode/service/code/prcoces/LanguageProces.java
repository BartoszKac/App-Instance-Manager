package com.example.DynamicCode.service.code.prcoces;

import com.example.DynamicCode.notification.FrontendNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageProces {

    private final FrontendNotificationService frontendNotificationService;

    /**
     * Uruchamia proces w dokładnie wskazanym katalogu (absolute path)
     */
    /**
     * Uruchamia proces w katalogu 'app/{folderName}' względem katalogu głównego aplikacji
     */
    public int runProcessInDirectory(List<String> command, String prefix, String folderName) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);

        // Budujemy ścieżkę wskazując na katalog "app", a w nim nasz konkretny folder (UUID)
        File projectDir = new File("app", folderName);

        log.info("Uruchamiam proces w wyliczonym katalogu: {}", projectDir.getAbsolutePath());

        // Sprawdzamy na wszelki wypadek, czy folder fizycznie istnieje
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("Katalog roboczy nie istnieje: {}", projectDir.getAbsolutePath());
            throw new IOException("Katalog projektu nie istnieje na dysku: " + projectDir.getAbsolutePath());
        }

        pb.directory(projectDir);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("PROCES LOG: {}", line);
                frontendNotificationService.sendToFrontend(prefix + (prefix.isEmpty() ? "" : " ") + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("Proces zakończony błędem. Kod: {}", exitCode);
        }
        return exitCode;
    }
}