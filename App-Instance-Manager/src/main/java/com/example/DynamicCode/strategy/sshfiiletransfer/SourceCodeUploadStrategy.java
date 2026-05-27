package com.example.DynamicCode.strategy.sshfiiletransfer;
import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.databaseservice.code.SourceCodeService;
import com.example.DynamicCode.model.entity.code.SourceCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Strategia wysyłania nieskompilowanych plików źródłowych (tabela source_codes).
 * Zawartość plików pochodzi bezpośrednio z pola `code` w bazie danych.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SourceCodeUploadStrategy implements FileUploadStrategy {

    private final SourceCodeService sourceCodeService;

    @Override
    public Map<String, String> resolveFiles(Long mainClassId) {
        log.info("SourceCodeUploadStrategy: Pobieram pliki źródłowe z DB dla mainClassId={}", mainClassId);
        List<SourceCode> files = sourceCodeService.getAllFilesFromMainClass(mainClassId);
        return files.stream()
                .collect(Collectors.toMap(
                        SourceCode::getName,
                        SourceCode::getCode
                ));
    }

    @Override
    public UploadStrategyType getType() {
        return UploadStrategyType.SOURCE_CODE;
    }
}