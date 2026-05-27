package com.example.DynamicCode.strategy.sshfiiletransfer;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.databaseservice.procces.RemoteExecutableService;
import com.example.DynamicCode.model.entity.procces.RemoteExecutable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Strategia wysyłania plików wykonywalnych procesów (tabela remote_executable).
 * Zawartość plików pochodzi bezpośrednio z pola `contents` w bazie danych.
 *
 * UWAGA: RemoteExecutableService nie filtruje po mainClassId — parametr ignorowany.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoteExecutableUploadStrategy implements FileUploadStrategy {

    private final RemoteExecutableService remoteExecutableService;

    @Override
    public Map<String, String> resolveFiles(Long mainClassId) {
        log.info("RemoteExecutableUploadStrategy: Pobieram pliki wykonywalne z DB (mainClassId={} ignorowane)", mainClassId);
        List<RemoteExecutable> executables = remoteExecutableService.getAllExecutables();
        return executables.stream()
                .collect(Collectors.toMap(
                        RemoteExecutable::getName,
                        RemoteExecutable::getContents
                ));
    }

    @Override
    public UploadStrategyType getType() {
        return UploadStrategyType.REMOTE_EXECUTABLE;
    }
}