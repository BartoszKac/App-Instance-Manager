package com.example.DynamicCode.code.service;

import com.example.DynamicCode.shared.notification.FrontendNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageProcess {

    private static final int MAX_CAPTURED_BYTES = 5 * 1024 * 1024;

    private final FrontendNotificationService frontendNotificationService;

    public int runProcessInDirectory(List<String> command, String prefix, String folderName) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(resolveProjectDirectory(folderName));
        pb.redirectErrorStream(true);

        Process process = pb.start();
        streamOutput(process, prefix);

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.error("Proces zakończony błędem. Kod: {}", exitCode);
        }
        return exitCode;
    }

    public String captureOutput(List<String> command, Duration timeout) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        CompletableFuture<String> output = CompletableFuture.supplyAsync(() -> readBounded(process));
        try {
            return awaitOutput(process, output, timeout);
        } finally {
            process.destroyForcibly();
        }
    }

    private String awaitOutput(Process process, CompletableFuture<String> output, Duration timeout)
            throws IOException, InterruptedException {
        if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
            throw new IOException("Proces przekroczył limit czasu: " + timeout);
        }
        if (process.exitValue() != 0) {
            throw new IOException("Proces zakończony kodem: " + process.exitValue());
        }
        return output.join();
    }

    private String readBounded(Process process) {
        try (InputStream stream = process.getInputStream()) {
            return new String(stream.readNBytes(MAX_CAPTURED_BYTES), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private File resolveProjectDirectory(String folderName) throws IOException {
        File projectDir = new File("app", folderName);
        log.info("Uruchamiam proces w wyliczonym katalogu: {}", projectDir.getAbsolutePath());

        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("Katalog roboczy nie istnieje: {}", projectDir.getAbsolutePath());
            throw new IOException("Katalog projektu nie istnieje na dysku: " + projectDir.getAbsolutePath());
        }
        return projectDir;
    }

    private void streamOutput(Process process, String prefix) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.info("PROCES LOG: {}", line);
                frontendNotificationService.sendToFrontend(prefix + (prefix.isEmpty() ? "" : " ") + line);
            }
        }
    }
}
