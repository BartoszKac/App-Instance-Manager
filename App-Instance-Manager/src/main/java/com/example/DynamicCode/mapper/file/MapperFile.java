package com.example.DynamicCode.mapper.file;

import com.example.DynamicCode.model.entity.code.CompiledCode;
import com.example.DynamicCode.model.dto.file.CodeFileDto;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.model.entity.file.CompiledFolderInTheDisk;
import com.example.DynamicCode.model.entity.file.SourceFolderInTheDisk;
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

        // POPRAWKA: Zmiana typu mapy na <String, byte[]> oraz użycie poprawnych getterów z DTO
        Map<String, byte[]> binaryCodeMap = binaryFiles.stream()
                .collect(Collectors.toMap(
                        CodeFileDto::getNameFile,
                        CodeFileDto::getFileContent,
                        (existing, replacement) -> replacement
                ));

        return sourceFiles.stream()
                .map(source -> mapToCompiledCode(source, binaryCodeMap))
                .collect(Collectors.toList());
    }

    // POPRAWKA: Typ mapy w argumencie zmieniony na Map<String, byte[]> - teraz pasuje idealnie
    private CompiledCode mapToCompiledCode(SourceCode source, Map<String, byte[]> binaryCodeMap) {
        String expectedBinaryName = source.getName().replace(source.getLanguage().getExtension(), ".class");

        // Pobieramy tablicę bajtów. Jeśli jej nima w mapie, zwracamy pustą tablicę
        byte[] bytecode = binaryCodeMap.getOrDefault(expectedBinaryName, new byte[0]);

        if (bytecode.length == 0) {
            log.warn("Nie znaleziono kodu binarnego dla oczekiwanego pliku: {}", expectedBinaryName);
        }

        CompiledCode compiled = new CompiledCode();
        compiled.setIdCode(source.getId());
        compiled.setName(expectedBinaryName);
        compiled.setCode(bytecode); // Zapisujemy czyste bajty bezpośrednio do encji
        compiled.setIdManClass(source.getIdManClass());
        compiled.setLanguage((source.getLanguage()));

        return compiled;
    }

    public SourceFolderInTheDisk toFolderInTheDisk(List<SourceCode> sourceCodes, String uniquePath) {
        if (sourceCodes == null || sourceCodes.isEmpty()) {
            throw new IllegalArgumentException("Lista plików źródłowych nie może być pusta podczas mapowania folderu!");
        }

        Long idMainClass = sourceCodes.get(0).getIdManClass();

        SourceFolderInTheDisk folder = new SourceFolderInTheDisk();
        folder.setPath(uniquePath);
        folder.setName("Katalog projektu " + idMainClass);
        folder.setSourceCodeId(idMainClass);

        return folder;
    }

    /**
     * Nowa metoda mapująca strukturę folderu źródłowego oraz wyniki kompilacji
     * na encję folderu skompilowanego na dysku.
     */
    public CompiledFolderInTheDisk toCompiledFolderInTheDisk(SourceFolderInTheDisk sourceFolder, List<CompiledCode> compiledCodes) {
        if (sourceFolder == null) {
            throw new IllegalArgumentException("Folder źródłowy nie może być nullem podczas mapowania folderu skompilowanego!");
        }
        if (compiledCodes == null || compiledCodes.isEmpty()) {
            throw new IllegalArgumentException("Lista skompilowanych plików nie może być pusta!");
        }

        log.info("Mapowanie folderu źródłowego '{}' na folder skompilowany.", sourceFolder.getName());

        // Wyciągamy ID skompilowanego kodu z pierwszego elementu listy
        Long compiledCodeId = compiledCodes.get(0).getIdManClass();   // DOBRZE - to idMainClass = 888

        CompiledFolderInTheDisk compiledFolder = new CompiledFolderInTheDisk();
        compiledFolder.setName(sourceFolder.getName());
        compiledFolder.setPath(sourceFolder.getPath());
        compiledFolder.setCompiledCodeId(compiledCodeId);

        return compiledFolder;
    }
}