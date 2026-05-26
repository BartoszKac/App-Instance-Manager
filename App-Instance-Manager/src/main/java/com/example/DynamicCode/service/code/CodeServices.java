package com.example.DynamicCode.service.code;

import com.example.DynamicCode.databaseservice.code.SourceCodeService;
import com.example.DynamicCode.file.FilesSave;
import com.example.DynamicCode.model.entity.code.SourceCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeServices {

    private final SourceCodeService sourceCodeService;
    private final FilesSave filesSave;

    @Transactional
    public String saveCodeToDbAndDisk(List<SourceCode> sourceCodes) {
        if (sourceCodes == null || sourceCodes.isEmpty()) {
            log.warn("[CODE-SERVICES] Próba zapisu pustej listy plików.");
            throw new IllegalArgumentException("Lista plików nie może być pusta!");
        }
        try {
            sourceCodeService.saveAllFilesToDb(sourceCodes);
            log.info("[CODE-SERVICES] Krok 2: Zapis plików na dysku...");
            filesSave.saveFilesToDisk(sourceCodes);
            log.info("[CODE-SERVICES] Proces zapisu do bazy i na dysk zakończony SUKCESEM.");
            return "Pomyślnie zapisano pliki w bazie danych oraz na dysku lokalnym!";

        } catch (Exception e) {
            log.error("[CODE-SERVICES] KRYTYCZNY BŁĄD podczas operacji zapisu: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się zrealizować pełnego zapisu: " + e.getMessage(), e);
        }
    }
}