package com.example.DynamicCode.strategy.sshfiiletransfer;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.databaseservice.code.CompiledCodeService;
import com.example.DynamicCode.model.entity.code.CompiledCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Strategia wysyłania skompilowanych plików (tabela compiled_codes).
 * Zawartość plików pochodzi bezpośrednio z pola `code` w bazie danych.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompiledCodeUploadStrategy implements FileUploadStrategy {

    private final CompiledCodeService compiledCodeService;

    @Override
    public Map<String, String> resolveFiles(Long mainClassId) {
        log.info("CompiledCodeUploadStrategy: Pobieram skompilowane pliki z DB dla mainClassId={}", mainClassId);
        List<CompiledCode> files = compiledCodeService.getAllFilesFromMainClass(mainClassId);
        return files.stream()
                .collect(Collectors.toMap(
                        CompiledCode::getName,
                        CompiledCode::getCode
                ));
    }

    @Override
    public UploadStrategyType getType() {
        return UploadStrategyType.COMPILED_CODE;
    }
}