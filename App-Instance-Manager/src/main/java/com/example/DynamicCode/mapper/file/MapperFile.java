package com.example.DynamicCode.mapper.file;

import com.example.DynamicCode.model.entity.code.CompiledCode;
import com.example.DynamicCode.model.dto.file.CodeFileDto;
import com.example.DynamicCode.model.entity.code.SourceCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MapperFile {

    public List<CompiledCode> toCompileCodeList(List<SourceCode> sourceFiles, List<CodeFileDto> binaryFiles) {
        log.info("Mapowanie plików źródłowych na skompilowane. Liczba źródeł: {}, Liczba binariów: {}",
                sourceFiles.size(), binaryFiles.size());

        Map<String, String> binaryCodeMap = binaryFiles.stream()
                .collect(Collectors.toMap(
                        CodeFileDto::getNameFile,
                        CodeFileDto::getBytecode, // Lombok z pola 'Bytecode' robi 'getBytecode()'
                        (existing, replacement) -> replacement
                ));

        return sourceFiles.stream()
                .map(source -> mapToCompiledCode(source, binaryCodeMap))
                .collect(Collectors.toList());
    }

    private CompiledCode mapToCompiledCode(SourceCode source, Map<String, String> binaryCodeMap) {
        String expectedBinaryName = source.getName().replace(source.getLanguage().getExtension(), ".class");
        String bytecode = binaryCodeMap.getOrDefault(expectedBinaryName, "");

        if (bytecode.isBlank()) {
            log.warn("Nie znaleziono kodu binarnego dla oczekiwanego pliku: {}", expectedBinaryName);
        }

        CompiledCode compiled = new CompiledCode();
        compiled.setIdCode(source.getId());
        compiled.setName(expectedBinaryName);
        compiled.setCode(bytecode);
        compiled.setIdManClass(source.getIdManClass());
        compiled.setLanguage((source.getLanguage()));

        return compiled;
    }


}