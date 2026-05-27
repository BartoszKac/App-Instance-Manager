package com.example.DynamicCode.databaseservice.file;

import com.example.DynamicCode.model.entity.file.SourceFolderInTheDisk;
import com.example.DynamicCode.repository.file.SourceFolderInTheDiskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SourceFolderInTheDiskService {

    @Autowired
    private SourceFolderInTheDiskRepository sourceFolderInTheDiskRepository;

    @Transactional
    public String saveToDb(SourceFolderInTheDisk foldersFromRequest) {
        try {
            sourceFolderInTheDiskRepository.save(foldersFromRequest);
            log.info("Foldery zostały pomyślnie zapisane!");
            return "All folders saved to DB successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas zapisu folderów do bazy. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving folders to DB: " + e.getMessage(), e);
        }
    }

    @Transactional
    public SourceFolderInTheDisk updateFolder(SourceFolderInTheDisk updatedFolder) {
        try {
            log.info("Aktualizuję folder o ID: {}", updatedFolder.getId());
            return sourceFolderInTheDiskRepository.save(updatedFolder);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji folderu o ID: {}. Powód: {}", updatedFolder.getId(), e.getMessage(), e);
            throw new RuntimeException("Nie udało się zaktualizować folderu", e);
        }
    }

    @Transactional(readOnly = true)
    public SourceFolderInTheDisk getFoldersFromSourceCodeId(Long sourceCodeId) {
        try {
            log.info("Pobieram folder dla sourceCodeId: {}", sourceCodeId);
            // Zmiana na nową metodę z repozytorium
            return sourceFolderInTheDiskRepository.findBySourceCodeId(sourceCodeId);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania folderu dla sourceCodeId: {}. Powód: ", sourceCodeId, e);
            throw new RuntimeException("Nie udało się pobrać folderu", e);
        }
    }

    @Transactional
    public void deleteFoldersBySourceCodeId(Long sourceCodeId) {
        try {
            log.info("Usuwam wszystkie foldery dla sourceCodeId: {}", sourceCodeId);
            // Zmiana na nową metodę z repozytorium
            sourceFolderInTheDiskRepository.deleteBySourceCodeId(sourceCodeId);
            log.info("Pomyślnie usunięto foldery dla sourceCodeId: {}", sourceCodeId);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania folderów dla sourceCodeId: {}. Powód: {}", sourceCodeId, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć folderów dla projektu", e);
        }
    }

    @Transactional
    public void deleteFolderById(Long idFolder) {
        try {
            log.info("Usuwam pojedynczy folder o ID: {}", idFolder);
            sourceFolderInTheDiskRepository.deleteById(idFolder);
            log.info("Pomyślnie usunięto folder o ID: {}", idFolder);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania folderu o ID: {}. Powód: {}", idFolder, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć konkretnego folderu", e);
        }
    }

    @Transactional
    public String deleteAllFoldersFromDb() {
        try {
            log.warn("UWAGA: Rozpoczynam usuwanie WSZYSTKICH folderów z bazy danych!");
            sourceFolderInTheDiskRepository.deleteAll();
            log.info("Baza danych z folderami została całkowicie wyczyszczona.");
            return "All folders cleared successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas czyszczenia bazy danych z folderów. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się wyczyścić bazy danych z folderów", e);
        }
    }

    @Transactional(readOnly = true)
    public SourceFolderInTheDisk getFolderById(Long idFolder) {
        try {
            log.info("Pobieram folder o idFolder: {}", idFolder);
            return sourceFolderInTheDiskRepository.findById(idFolder)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono folderu o podanym ID: " + idFolder));
        } catch (IllegalArgumentException e) {
            log.warn("Brak rekordu w bazie: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania folderu o idFolder: {}. Powód: ", idFolder, e);
            throw new RuntimeException("Nie udało się pobrać folderu z bazy danych", e);
        }
    }

    @Transactional(readOnly = true)
    public List<SourceFolderInTheDisk> getAllFolders() {
        log.info("[SERWIS] Pobieram absolutnie wszystkie foldery (SourceFolderInTheDisk) z bazy danych.");
        try {
            List<SourceFolderInTheDisk> allFolders = sourceFolderInTheDiskRepository.findAll();
            log.info("[SERWIS] Pomyślnie pobrano wszystkie foldery. Łączna liczba: {}", allFolders.size());
            return allFolders;
        } catch (Exception e) {
            log.error("[SERWIS] Błąd podczas pobierania wszystkich folderów z bazy: ", e);
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public String getProjectPathBySourceCodeId(Long sourceCodeId) {
        try {
            log.info("Pobieram ścieżkę projektu (path_in_project) dla sourceCodeId: {}", sourceCodeId);

            // Zmiana na nową metodę z repozytorium
            SourceFolderInTheDisk folder = sourceFolderInTheDiskRepository.findBySourceCodeId(sourceCodeId);

            if (folder == null) {
                log.warn("Nie znaleziono folderu w bazie dla sourceCodeId: {}", sourceCodeId);
                throw new IllegalArgumentException("Brak folderu powiązanego z projektem o ID: " + sourceCodeId);
            }

            String projectPath = folder.getPath();
            log.info("Znaleziona ścieżka dla sourceCodeId {}: {}", sourceCodeId, projectPath);
            return projectPath;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania ścieżki dla sourceCodeId: {}. Powód: {}", sourceCodeId, e.getMessage(), e);
            throw new RuntimeException("Nie udało się pobrać ścieżki projektu z bazy danych", e);
        }
    }
}