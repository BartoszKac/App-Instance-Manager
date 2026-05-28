package com.example.DynamicCode.databaseservice.code;

import com.example.DynamicCode.databaseservice.DataBaseProvider;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.repository.code.CodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SourceCodeService implements DataBaseProvider<SourceCode> {

    @Autowired
    private CodeRepository codeRepository;


    @Transactional
    public String saveAllFilesToDb(List<SourceCode> codesFromRequest) {
        try {
            log.info("Rozpoczynam zapis {} plików do bazy...", codesFromRequest.size());
            codeRepository.saveAll(codesFromRequest);
            log.info("Pliki zostały pomyślnie zapisane!");
            return "All files saved to DB successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas zapisu plików do bazy. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving files to DB: " + e.getMessage(), e);
        }
    }

    @Transactional
    public SourceCode updateFile(SourceCode updatedCode) {
        try {
            log.info("Aktualizuję plik o ID: {}", updatedCode.getId());
            return codeRepository.save(updatedCode);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji pliku o ID: {}. Powód: {}", updatedCode.getId(), e.getMessage(), e);
            throw new RuntimeException("Nie udało się zaktualizować pliku", e);
        }
    }


    @Transactional(readOnly = true)
    @Override
    public List<SourceCode> getAllFilesFromMainClass(Long idMainClass) {
        try {
            log.info("Pobieram WSZYSTKIE pliki dla idMainClass: {}", idMainClass);
            return  codeRepository.findByIdManClass(idMainClass);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania wszystkich plików dla idMainClass: {}. Powód: ", idMainClass, e);
            return Collections.emptyList();
        }
    }


    @Transactional
    public void deleteAllFilesByMainClass(Long idMainClass) {
        try {
            log.info("Usuwam wszystkie pliki (skompilowane i nieskompilowane) dla idMainClass: {}", idMainClass);
            codeRepository.deleteByIdManClass(idMainClass);
            log.info("Pomyślnie usunięto pliki dla idMainClass: {}", idMainClass);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania plików dla idMainClass: {}. Powód: {}", idMainClass, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć plików dla projektu", e);
        }
    }

    @Transactional
    public void deleteFileById(Long idCode) {
        try {
            log.info("Usuwam pojedynczy plik o ID: {}", idCode);
            codeRepository.deleteById(idCode);
            log.info("Pomyślnie usunięto plik o ID: {}", idCode);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania pliku o ID: {}. Powód: {}", idCode, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć konkretnego pliku", e);
        }
    }

    @Transactional
    public String deleteAllFilesFromDb() {
        try {
            log.warn("UWAGA: Rozpoczynam usuwanie WSZYSTKICH rekordów z bazy danych!");
            codeRepository.deleteAll();
            log.info("Baza danych została całkowicie wyczyszczona.");
            return "All data cleared successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas czyszczenia całej bazy danych. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się wyczyścić bazy danych", e);
        }
    }

    @Transactional(readOnly = true)
    public SourceCode getFileById(Long idCode) {
        try {
            log.info("Pobieram plik o idCode: {}", idCode);
            return codeRepository.findById(idCode)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono pliku o podanym ID: " + idCode));

        } catch (IllegalArgumentException e) {
            log.warn("Brak rekordu w bazie: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania pliku o idCode: {}. Powód: ", idCode, e);
            throw new RuntimeException("Nie udało się pobrać pliku z bazy danych", e);
        }
    }

    /**
     * Pobiera absolutnie wszystkie pliki SourceCode zapisane w bazie danych.
     * Przydatne do ogólnego przeglądu bazy lub testów.
     */
    @Transactional(readOnly = true)
    public List<SourceCode> getAllFilesFromMainClass() {
        log.info("[SERWIS] Pobieram absolutnie wszystkie pliki SourceCode z bazy danych.");
        try {
            List<SourceCode> allFiles = codeRepository.findAll(); // Wywołanie wbudowanej metody JpaRepository
            log.info("[SERWIS] Pomyślnie pobrano wszystkie pliki. Łączna liczba: {}", allFiles.size());
            return allFiles;
        } catch (Exception e) {
            log.error("[SERWIS] Błąd podczas pobierania wszystkich plików z bazy: ", e);
            return Collections.emptyList();
        }
    }
}