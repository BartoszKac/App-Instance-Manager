package com.example.DynamicCode.databaseservice.code;

import com.example.DynamicCode.databaseservice.DataBaseProvider;
import com.example.DynamicCode.model.entity.code.CompiledCode; // Zmiana na poprawną encję
import com.example.DynamicCode.repository.code.CompiledCodeRepository; // Zakładam taką nazwę repozytorium dla CompiledCode
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompiledCodeService implements DataBaseProvider<CompiledCode> {

    private final CompiledCodeRepository compiledCodeRepository;

    @Transactional
    public String saveAllFilesToDb(List<CompiledCode> codesFromRequest) {
        try {
            log.info("CompiledCodeStorage: Rozpoczynam zapis {} skompilowanych plików do bazy...", codesFromRequest.size());
            compiledCodeRepository.saveAll(codesFromRequest);
            log.info("CompiledCodeStorage: Pliki zostały pomyślnie zapisane!");
            return "All compiled files saved to DB successfully!";
        } catch (Exception e) {
            log.error("CompiledCodeStorage: Błąd podczas zapisu plików do bazy. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving compiled files to DB: " + e.getMessage(), e);
        }
    }

    @Transactional
    public CompiledCode updateFile(CompiledCode updatedCode) {
        try {
            log.info("CompiledCodeStorage: Aktualizuję skompilowany plik o ID: {}", updatedCode.getIdCode());
            return compiledCodeRepository.save(updatedCode);
        } catch (Exception e) {
            log.error("CompiledCodeStorage: Błąd podczas aktualizacji pliku o ID: {}. Powód: {}", updatedCode.getIdCode(), e.getMessage(), e);
            throw new RuntimeException("Nie udało się zaktualizować skompilowanego pliku", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompiledCode> getAllFilesFromMainClass(Long idMainClass) {
        try {
            log.info("CompiledCodeStorage: Pobieram WSZYSTKIE skompilowane pliki dla idMainClass: {}", idMainClass);
            return compiledCodeRepository.findByIdManClass(idMainClass);
        } catch (Exception e) {
            log.error("CompiledCodeStorage: Błąd podczas pobierania wszystkich skompilowanych plików dla idMainClass: {}. Powód: ", idMainClass, e);
            return Collections.emptyList();
        }
    }

    @Transactional
    public void deleteAllFilesByMainClass(Long idMainClass) {
        try {
            log.info("CompiledCodeStorage: Usuwam wszystkie skompilowane pliki dla idMainClass: {}", idMainClass);
            compiledCodeRepository.deleteByIdManClass(idMainClass);
            log.info("CompiledCodeStorage: Pomyślnie usunięto pliki dla idMainClass: {}", idMainClass);
        } catch (Exception e) {
            log.error("CompiledCodeStorage: Błąd podczas usuwania plików dla idMainClass: {}. Powód: {}", idMainClass, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć skompilowanych plików dla projektu", e);
        }
    }

    @Transactional
    public void deleteFileById(Long idCode) {
        try {
            log.info("CompiledCodeStorage: Usuwam pojedynczy skompilowany plik o ID: {}", idCode);
            compiledCodeRepository.deleteById(idCode);
            log.info("CompiledCodeStorage: Pomyślnie usunięto plik o ID: {}", idCode);
        } catch (Exception e) {
            log.error("CompiledCodeStorage: Błąd podczas usuwania pliku o ID: {}. Powód: {}", idCode, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć konkretnego skompilowanego pliku", e);
        }
    }

    @Transactional
    public String deleteAllFilesFromDb() {
        try {
            log.warn("CompiledCodeStorage: UWAGA: Rozpoczynam usuwanie WSZYSTKICH skompilowanych rekordów z bazy danych!");
            compiledCodeRepository.deleteAll();
            log.info("CompiledCodeStorage: Baza danych została całkowicie wyczyszczona.");
            return "All compiled data cleared successfully!";
        } catch (Exception e) {
            log.error("CompiledCodeStorage: Błąd podczas czyszczenia całej bazy danych. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się wyczyścić bazy danych", e);
        }
    }

    @Transactional(readOnly = true)
    public CompiledCode getFileById(Long idCode) {
        try {
            log.info("CompiledCodeStorage: Pobieram skompilowany plik o idCode: {}", idCode);
            return compiledCodeRepository.findById(idCode)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono skompilowanego pliku o podanym ID: " + idCode));
        } catch (IllegalArgumentException e) {
            log.warn("CompiledCodeStorage: Brak rekordu w bazie: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("CompiledCodeStorage: Błąd podczas pobierania pliku o idCode: {}. Powód: ", idCode, e);
            throw new RuntimeException("Nie udało się pobrać skompilowanego pliku z bazy danych", e);
        }
    }
}