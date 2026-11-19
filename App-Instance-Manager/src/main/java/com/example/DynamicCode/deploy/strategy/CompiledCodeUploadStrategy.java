package com.example.DynamicCode.deploy.strategy;

import com.example.DynamicCode.code.entity.CompiledCode;
import com.example.DynamicCode.code.service.CompiledCodeService;
import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompiledCodeUploadStrategy implements FileUploadStrategy {

    private final CompiledCodeService compiledCodeService;

    @Override
    public Map<String, byte[]> resolveFiles(Long mainClassId) {
        return compiledCodeService.getAllFilesFromMainClass(mainClassId).stream()
                .filter(file -> Objects.nonNull(file.getCode()))
                .collect(Collectors.toMap(CompiledCode::getName, CompiledCode::getCode, (first, second) -> second));
    }

    @Override
    public UploadStrategyType getType() {
        return UploadStrategyType.COMPILED_CODE;
    }
}
