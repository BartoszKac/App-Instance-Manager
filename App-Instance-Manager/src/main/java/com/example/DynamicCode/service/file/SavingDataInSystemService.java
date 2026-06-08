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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavingDataInSystemService {

    private final CompiledCodeService compiledCodeService;
    private final MapperFile mapperFile;
    private final FileService fileService;
    private final SourceFolderInTheDiskService sourceFolderInTheDiskService;
    private final SourceCodeService sourceCodeService;
    private final CompileFolderInTheDiskService compileFolderInTheDiskService;

    @Transactional
    public String saveSourceCodeIntheSystem(List<SourceCode> sourceCodes) {
        validateSourceCodes(sourceCodes);
        log.info("Rozpoczynam proces zapisu systemu dla {} plików źródłowych.", sourceCodes.size());

        try {
            String uniquePath = resolveUniquePath(sourceCodes.get(0).getIdManClass());

            fileService.saveFilesToDisk(sourceCodes, uniquePath);

            saveDataToDatabase(sourceCodes, uniquePath);

            return uniquePath;

        } catch (Exception e) {
            log.error("Błąd krytyczny podczas zapisu kodu w systemie. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się zapisać kodu w systemie: " + e.getMessage(), e);
        }
    }

    private void validateSourceCodes(List<SourceCode> sourceCodes) {
        if (sourceCodes == null || sourceCodes.isEmpty()) {
            throw new IllegalArgumentException("Lista plików do zapisu jest pusta!");
        }
    }

    private String resolveUniquePath(Long idMainClass) {
        if (!CollectionUtils.isEmpty(sourceCodeService.getAllFilesFromMainClass(idMainClass))) {
            log.warn("Istnieją już pliki dla idMainClass: {}. Zastępuję istniejące dane nowymi plikami.", idMainClass);

            // Bezpiecznik: jeśli baza była uszkodzona i nie ma folderu, wygeneruj nową ścieżkę zamiast strzelać nullem
            SourceFolderInTheDisk existingFolder = sourceFolderInTheDiskService.getFoldersFromSourceCodeId(idMainClass);
            if (existingFolder != null) {
                return existingFolder.getPath();
            }
            log.error("Niespójność danych: Kod istnieje, ale brak folderu w DB dla idMainClass: {}. Generuję nową ścieżkę.", idMainClass);
        }
        return fileService.generateUniquePath();
    }

    private void saveDataToDatabase(List<SourceCode> sourceCodes, String uniquePath) {
        Long idMainClass = sourceCodes.get(0).getIdManClass();

        // 1. NAJPIERW sprawdzamy stan bazy (zanim dodamy nowe pliki)
        boolean folderExists = !CollectionUtils.isEmpty(sourceCodeService.getAllFilesFromMainClass(idMainClass));

        // 2. Zapisujemy pliki do bazy danych
        log.info("Zapisuję pliki źródłowe do bazy danych...");
        sourceCodeService.saveAllFilesToDb(sourceCodes);

        // 3. Jeśli to był UPDATE, przerywamy i nie dotykamy tabeli z folderami
        if (folderExists) {
            log.info("Aktualizacja kodu w istniejącym folderie. Pomijam ponowny zapis folderu na dysku do bazy danych.");
            log.info("Proces aktualizacji zakończony pełnym sukcesem!");
            return;
        }

        // 4. Jeśli to NOWY ZAPIS, dodajemy informację o folderze
        log.info("Nowy kod - zapisuję informację o nowym folderze [{}] do bazy danych...", uniquePath);
        SourceFolderInTheDisk folder = mapperFile.toFolderInTheDisk(sourceCodes, uniquePath);

        if (folder != null) {
            sourceFolderInTheDiskService.saveToDb(folder);
            log.info("Proces zapisu nowego kodu zakończony pełnym sukcesem!");
        }
    }

    @Transactional
    public void handleCompilationResult(Long idMainClass) {
        log.info("Rozpoczynam przetwarzanie wyniku kompilacji dla idMainClass: {}", idMainClass);

        SourceFolderInTheDisk sourceFolder = sourceFolderInTheDiskService.getFoldersFromSourceCodeId(idMainClass);
        if (sourceFolder == null || sourceFolder.getPath() == null) {
            log.error("Nie znaleziono ścieżki do folderu źródłowego dla idMainClass: {}", idMainClass);
            throw new IllegalStateException("Brak folderu źródłowego dla idMainClass: " + idMainClass);
        }

        List<SourceCode> sourceFiles = sourceCodeService.getAllFilesFromMainClass(idMainClass);
        if (CollectionUtils.isEmpty(sourceFiles)) {
            log.error("Nie znaleziono plików źródłowych w bazie dla idMainClass: {}", idMainClass);
            return;
        }

        boolean hasAlreadyCompiledCodes = !CollectionUtils.isEmpty(compiledCodeService.getAllFilesFromMainClass(idMainClass));

        String folderPath = sourceFolder.getPath();
        List<CodeFileDto> rawBinaryFiles = fileService.readFilesFromDisk(folderPath);
        if (CollectionUtils.isEmpty(rawBinaryFiles)) {
            log.warn("Brak plików binarnych na dysku do przetworzenia w ścieżce: {}", folderPath);
            return;
        }

        List<CompiledCode> compiledCodes = mapperFile.toCompileCodeList(sourceFiles, rawBinaryFiles);
        if (CollectionUtils.isEmpty(compiledCodes)) {
            log.warn("Mapowanie skompilowanych plików zwróciło pustą listę.");
            return;
        }

        compiledCodeService.saveAllFilesToDb(compiledCodes);
        log.info("Zapisano {} skompilowanych plików do bazy danych.", compiledCodes.size());

        if (hasAlreadyCompiledCodes) {
            log.info("Kompilacja dla idMainClass: {} była już wcześniej procesowana. Pomijam ponowny zapis folderu.", idMainClass);
        } else {
            log.info("Pierwsza kompilacja dla idMainClass: {}. Zapisuję folder skompilowany do bazy.", idMainClass);
            CompiledFolderInTheDisk compiledFolder = mapperFile.toCompiledFolderInTheDisk(sourceFolder, compiledCodes);
            if (compiledFolder != null) {
                String resultMessage = compileFolderInTheDiskService.saveToDb(compiledFolder);
                log.info("Status zapisu nowego folderu skompilowanego: {}", resultMessage);
            }
        }

        log.info("Przetwarzanie wyniku kompilacji dla idMainClass: {} zakończone sukcesem.", idMainClass);
    }
}