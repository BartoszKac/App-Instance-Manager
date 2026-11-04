package com.example.DynamicCode.deploy.strategy;
import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import com.example.DynamicCode.code.service.SourceCodeService;
import com.example.DynamicCode.code.entity.SourceCode;
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