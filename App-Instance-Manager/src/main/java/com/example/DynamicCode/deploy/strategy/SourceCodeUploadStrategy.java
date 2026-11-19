package com.example.DynamicCode.deploy.strategy;

import com.example.DynamicCode.code.entity.SourceCode;
import com.example.DynamicCode.code.service.SourceCodeService;
import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SourceCodeUploadStrategy implements FileUploadStrategy {

    private final SourceCodeService sourceCodeService;

    @Override
    public Map<String, byte[]> resolveFiles(Long mainClassId) {
        return sourceCodeService.getAllFilesFromMainClass(mainClassId).stream()
                .filter(file -> Objects.nonNull(file.getCode()))
                .collect(Collectors.toMap(
                        SourceCode::getName,
                        file -> file.getCode().getBytes(StandardCharsets.UTF_8),
                        (first, second) -> second));
    }

    @Override
    public UploadStrategyType getType() {
        return UploadStrategyType.SOURCE_CODE;
    }
}
