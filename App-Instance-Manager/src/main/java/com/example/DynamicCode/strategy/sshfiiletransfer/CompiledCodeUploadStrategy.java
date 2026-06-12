package com.example.DynamicCode.strategy.sshfiiletransfer;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.databaseservice.code.CompiledCodeService;
import com.example.DynamicCode.model.entity.code.CompiledCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Strategia wysyłania skompilowanych plików (tabela compiled_codes).
 * Bezpiecznie konwertuje dane binarne z bazy danych do formatu Base64 jako String.
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
                        file -> {
                            byte[] byteCode = file.getCode();
                            if (byteCode == null || byteCode.length == 0) {
                                return "";
                            }
                            // Kodujemy bajty do Base64, aby bezpiecznie przesłać je jako String
                            return Base64.getEncoder().encodeToString(byteCode);
                        },
                        (existing, replacement) -> replacement
                ));
    }

    @Override
    public UploadStrategyType getType() {
        return UploadStrategyType.COMPILED_CODE;
    }
}