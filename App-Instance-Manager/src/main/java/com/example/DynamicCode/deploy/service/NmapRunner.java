package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.code.service.LanguageProcess;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NmapRunner {

    private static final String SCAN_FAILED_MESSAGE =
            "Skan się nie udał. Sprawdź, czy nmap jest zainstalowany i dostępny w PATH.";

    private final LanguageProcess languageProcess;

    public String run(List<String> command, Duration timeout) {
        try {
            return languageProcess.captureOutput(command, timeout);
        } catch (IOException e) {
            throw new NetworkScanFailedException(SCAN_FAILED_MESSAGE, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkScanFailedException(SCAN_FAILED_MESSAGE, e);
        }
    }
}
