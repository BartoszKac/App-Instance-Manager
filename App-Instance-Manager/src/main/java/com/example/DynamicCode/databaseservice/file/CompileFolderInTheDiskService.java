package com.example.DynamicCode.databaseservice.file;


import com.example.DynamicCode.model.entity.file.CompiledFolderInTheDisk;
import com.example.DynamicCode.repository.file.CompileFolderInTheDiskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CompileFolderInTheDiskService {

    @Autowired
    private CompileFolderInTheDiskRepository compileFolderInTheDiskRepository;

    @Transactional
    public String saveToDb(CompiledFolderInTheDisk folderFromRequest) {
        try {
            compileFolderInTheDiskRepository.save(folderFromRequest);
            log.info("Skompilowane foldery zostały pomyślnie zapisane!");
            return "All compiled folders saved to DB successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas zapisu skompilowanych folderów do bazy. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving compiled folders to DB: " + e.getMessage(), e);
        }
    }

    @Transactional
    public CompiledFolderInTheDisk updateFolder(CompiledFolderInTheDisk updatedFolder) {
        try {
            log.info("Aktualizuję skompilowany folder o ID: {}", updatedFolder.getId());
            return compileFolderInTheDiskRepository.save(updatedFolder);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji skompilowanego folderu o ID: {}. Powód: {}", updatedFolder.getId(), e.getMessage(), e);
            throw new RuntimeException("Nie udało się zaktualizować skompilowanego folderu", e);
        }
    }

    @Transactional(readOnly = true)
    public CompiledFolderInTheDisk getFoldersFromCompiledCodeId(Long compiledCodeId) {
        try {
            log.info("Pobieram skompilowany folder dla compiledCodeId: {}", compiledCodeId);
            return compileFolderInTheDiskRepository.findByCompiledCodeId(compiledCodeId)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono skompilowanego folderu dla compiledCodeId: " + compiledCodeId));
        } catch (IllegalArgumentException e) {
            log.warn("Brak rekordu w bazie: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania skompilowanego folderu dla compiledCodeId: {}. Powód: ", compiledCodeId, e);
            throw new RuntimeException("Nie udało się pobrać skompilowanego folderu", e);
        }
    }

    @Transactional
    public void deleteFoldersByCompiledCodeId(Long compiledCodeId) {
        try {
            log.info("Usuwam wszystkie skompilowane foldery dla compiledCodeId: {}", compiledCodeId);
            compileFolderInTheDiskRepository.deleteByCompiledCodeId(compiledCodeId);
            log.info("Pomyślnie usunięto skompilowane foldery dla compiledCodeId: {}", compiledCodeId);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania skompilowanych folderów dla compiledCodeId: {}. Powód: {}", compiledCodeId, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć skompilowanych folderów dla projektu", e);
        }
    }

    @Transactional
    public void deleteFolderById(Long idFolder) {
        try {
            log.info("Usuwam pojedynczy skompilowany folder o ID: {}", idFolder);
            compileFolderInTheDiskRepository.deleteById(idFolder);
            log.info("Pomyślnie usunięto skompilowany folder o ID: {}", idFolder);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania skompilowanego folderu o ID: {}. Powód: {}", idFolder, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć konkretnego skompilowanego folderu", e);
        }
    }

    @Transactional
    public String deleteAllFoldersFromDb() {
        try {
            log.warn("UWAGA: Rozpoczynam usuwanie WSZYSTKICH skompilowanych folderów z bazy danych!");
            compileFolderInTheDiskRepository.deleteAll();
            log.info("Baza danych ze skompilowanymi folderami została całkowicie wyczyszczona.");
            return "All compiled folders cleared successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas czyszczenia bazy danych ze skompilowanych folderów. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się wyczyścić bazy danych ze skompilowanych folderów", e);
        }
    }

    @Transactional(readOnly = true)
    public CompiledFolderInTheDisk getFolderById(Long idFolder) {
        try {
            log.info("Pobieram skompilowany folder o idFolder: {}", idFolder);
            return compileFolderInTheDiskRepository.findById(idFolder)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono skompilowanego folderu o podanym ID: " + idFolder));
        } catch (IllegalArgumentException e) {
            log.warn("Brak rekordu w bazie: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania skompilowanego folderu o idFolder: {}. Powód: ", idFolder, e);
            throw new RuntimeException("Nie udało się pobrać skompilowanego folderu z bazy danych", e);
        }
    }

    @Transactional(readOnly = true)
    public List<CompiledFolderInTheDisk> getAllFolders() {
        log.info("[SERWIS] Pobieram absolutnie wszystkie foldery (CompiledFolderInTheDisk) z bazy danych.");
        try {
            List<CompiledFolderInTheDisk> allFolders = compileFolderInTheDiskRepository.findAll();
            log.info("[SERWIS] Pomyślnie pobrano wszystkie skompilowane foldery. Łączna liczba: {}", allFolders.size());
            return allFolders;
        } catch (Exception e) {
            log.error("[SERWIS] Błąd podczas pobierania wszystkich skompilowanych folderów z bazy: ", e);
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public String getProjectPathByCompiledCodeId(Long compiledCodeId) {
        try {
            log.info("Pobieram ścieżkę projektu (path_in_project) dla compiledCodeId: {}", compiledCodeId);

            CompiledFolderInTheDisk folder = compileFolderInTheDiskRepository.findByCompiledCodeId(compiledCodeId)
                    .orElseThrow(() -> new IllegalArgumentException("Brak skompilowanego folderu powiązanego z projektem o ID: " + compiledCodeId));

            String projectPath = folder.getPath();
            log.info("Znaleziona ścieżka dla compiledCodeId {}: {}", compiledCodeId, projectPath);
            return projectPath;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania ścieżki dla compiledCodeId: {}. Powód: {}", compiledCodeId, e.getMessage(), e);
            throw new RuntimeException("Nie udało się pobrać ścieżki projektu z bazy danych", e);
        }
    }
}