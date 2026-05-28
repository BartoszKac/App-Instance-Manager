package com.example.DynamicCode.strategy.sshfiiletransfer;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.databaseservice.procces.RemoteExecutableService;
import com.example.DynamicCode.model.entity.procces.RemoteExecutable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Strategia wysyłania plików wykonywalnych procesów (tabela remote_executable).
 * Zawartość plików pochodzi bezpośrednio z pola `contents` w bazie danych.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteExecutableUploadStrategy implements FileUploadStrategy {

    private final RemoteExecutableService remoteExecutableService;

    @Override
    public Map<String, String> resolveFiles(Long mainClassId) {
        log.info("RemoteExecutableUploadStrategy: Pobieram pliki wykonywalne z DB dla id: {}", mainClassId);
        List<RemoteExecutable> executables = remoteExecutableService.getAllFilesFromMainClass(mainClassId);

        return executables.stream()
                .collect(Collectors.toMap(
                        RemoteExecutable::getName,
                        executable -> {
                            if (executable.getContents() == null) {
                                return "";
                            }
                            // ISO_8859_1 zachowuje układ bajtów 1:1, co pozwala przesyłać też pliki .exe jako String
                            return new String(executable.getContents(), StandardCharsets.ISO_8859_1);
                        }
                ));
    }

    @Override
    public UploadStrategyType getType() {
        return UploadStrategyType.REMOTE_EXECUTABLE;
    }
}