package com.example.DynamicCode.service.file;


import com.example.DynamicCode.databaseservice.code.CompiledCodeService;
import com.example.DynamicCode.databaseservice.code.SourceCodeService;
import com.example.DynamicCode.databaseservice.file.CompileFolderInTheDiskService;
import com.example.DynamicCode.databaseservice.file.SourceFolderInTheDiskService;
import com.example.DynamicCode.fileutil.FileService;
import com.example.DynamicCode.mapper.file.MapperFile;
import com.example.DynamicCode.model.dto.file.CodeFileDto;
import com.example.DynamicCode.model.entity.code.CompiledCode;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.model.entity.file.CompiledFolderInTheDisk;
import com.example.DynamicCode.model.entity.file.SourceFolderInTheDisk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Component
public class SavingDataInSystemService {

    private final CompiledCodeService compiledCodeService;
    private final MapperFile mapperFile;
    private final FileService fileService;
    private final SourceFolderInTheDiskService sourceFolderInTheDiskService;
    private final SourceCodeService sourceCodeService;
    private final CompileFolderInTheDiskService compileFolderInTheDiskService; // Wstrzykujemy nowy serwis

    @Transactional
    public String saveSourceCodeIntheSystem(List<SourceCode> sourceCodes) {
        if (sourceCodes == null || sourceCodes.isEmpty()) {
            throw new IllegalArgumentException("Lista plików do zapisu jest pusta!");
        }

        log.info("Rozpoczynam proces zapisu systemu dla {} plików źródłowych.", sourceCodes.size());

        try {
            String uniquePath = fileService.generateUniquePath();

            fileService.saveFilesToDisk(sourceCodes, uniquePath);

            log.info("Zapisuję pliki źródłowe do bazy danych...");
            sourceCodeService.saveAllFilesToDb(sourceCodes);

            log.info("Zapisuję informację o folderze [{}] do bazy danych...", uniquePath);

            SourceFolderInTheDisk folder = mapperFile.toFolderInTheDisk(sourceCodes, uniquePath);

            sourceFolderInTheDiskService.saveToDb(folder);

            log.info("Proces zapisu zakończony pełnym sukcesem!");

            return uniquePath;

        } catch (Exception e) {
            log.error("Błąd krytyczny podczas zapisu kodu w systemie. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się zapisać kodu w systemie: " + e.getMessage(), e);
        }
    }

    public String SaveCompiledCodeIntheSystem(List<SourceCode> compiledCodes) {
        // Tutaj implementacja logiki zapisu skompilowanych plików na dysku
        // Możesz użyć klasy FileService do faktycznego zapisu
        // Na przykład:
        // filesSave.saveFilesToDisk(compiledCodes);
        return "Pomyślnie zapisano skompilowane pliki w systemie!";
    }

    @Transactional
    public void handleCompilationResult(Long idMainClass, List<SourceCode> sourceFiles) {

        SourceFolderInTheDisk sourceFolder = sourceFolderInTheDiskService.getFoldersFromSourceCodeId(idMainClass);

        if (sourceFolder == null || sourceFolder.getPath() == null) {
            log.error("Nie znaleziono ścieżki do folderu dla idMainClass: {}", idMainClass);
            return;
        }

        String folderPath = sourceFolder.getPath();

        List<CodeFileDto> rawBinaryFiles = fileService.readFilesFromDisk(folderPath);

        if (rawBinaryFiles.isEmpty()) {
            log.warn("Brak plików binarnych na dysku do przetworzenia.");
            return;
        }

        // 3. Mapowanie i zapis skompilowanego kodu
        List<CompiledCode> compiledCodes = mapperFile.toCompileCodeList(sourceFiles, rawBinaryFiles);

        if (!compiledCodes.isEmpty()) {
            // Zapisujemy pliki kodu do bazy danych
            compiledCodeService.saveAllFilesToDb(compiledCodes);

            // UŻYCIE MAPPERA: Przekształcamy folder źródłowy i listę skompilowaną w nową encję
            CompiledFolderInTheDisk compiledFolder = mapperFile.toCompiledFolderInTheDisk(sourceFolder, compiledCodes);

            // Zapisujemy nowy folder skompilowany przez dedykowany serwis
            String resultMessage = compileFolderInTheDiskService.saveToDb(compiledFolder);
            log.info("Status zapisu folderu skompilowanego: {}", resultMessage);
        }
    }

    }

