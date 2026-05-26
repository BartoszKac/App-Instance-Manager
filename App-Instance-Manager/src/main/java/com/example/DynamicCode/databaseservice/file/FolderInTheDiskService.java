package com.example.DynamicCode.databaseservice.file;

import com.example.DynamicCode.model.entity.file.FolderInTheDisk;
import com.example.DynamicCode.repository.file.FolderInTheDiskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class FolderInTheDiskService {

    @Autowired
    private FolderInTheDiskRepository folderInTheDiskRepository;

    @Transactional
    public String saveAllFoldersToDb(List<FolderInTheDisk> foldersFromRequest) {
        try {
            log.info("Rozpoczynam zapis {} folderów do bazy...", foldersFromRequest.size());
            folderInTheDiskRepository.saveAll(foldersFromRequest);
            log.info("Foldery zostały pomyślnie zapisane!");
            return "All folders saved to DB successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas zapisu folderów do bazy. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving folders to DB: " + e.getMessage(), e);
        }
    }

    @Transactional
    public FolderInTheDisk updateFolder(FolderInTheDisk updatedFolder) {
        try {
            log.info("Aktualizuję folder o ID: {}", updatedFolder.getIdFolder());
            return folderInTheDiskRepository.save(updatedFolder);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji folderu o ID: {}. Powód: {}", updatedFolder.getIdFolder(), e.getMessage(), e);
            throw new RuntimeException("Nie udało się zaktualizować folderu", e);
        }
    }

    @Transactional(readOnly = true)
    public List<FolderInTheDisk> getAllFoldersFromMainClass(Long IdMainClass) {
        try {
            log.info("Pobieram WSZYSTKIE foldery dla IdMainClass: {}", IdMainClass);
            return folderInTheDiskRepository.findByIdMainClass(IdMainClass); // Metoda w repo musi się nazywać: findByIdMainClass(Long IdMainClass)
        } catch (Exception e) {
            log.error("Błąd podczas pobierania wszystkich folderów dla IdMainClass: {}. Powód: ", IdMainClass, e);
            return Collections.emptyList();
        }
    }

    @Transactional
    public void deleteAllFoldersByMainClass(Long IdMainClass) {
        try {
            log.info("Usuwam wszystkie foldery dla IdMainClass: {}", IdMainClass);
            folderInTheDiskRepository.deleteByIdMainClass(IdMainClass); // Metoda w repo: deleteByIdMainClass(Long IdMainClass)
            log.info("Pomyślnie usunięto foldery dla IdMainClass: {}", IdMainClass);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania folderów dla IdMainClass: {}. Powód: {}", IdMainClass, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć folderów dla projektu", e);
        }
    }

    @Transactional
    public void deleteFolderById(Long IdFolder) {
        try {
            log.info("Usuwam pojedynczy folder o ID: {}", IdFolder);
            folderInTheDiskRepository.deleteById(IdFolder);
            log.info("Pomyślnie usunięto folder o ID: {}", IdFolder);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania folderu o ID: {}. Powód: {}", IdFolder, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć konkretnego folderu", e);
        }
    }

    @Transactional
    public String deleteAllFoldersFromDb() {
        try {
            log.warn("UWAGA: Rozpoczynam usuwanie WSZYSTKICH folderów z bazy danych!");
            folderInTheDiskRepository.deleteAll();
            log.info("Baza danych z folderami została całkowicie wyczyszczona.");
            return "All folders cleared successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas czyszczenia bazy danych z folderów. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się wyczyścić bazy danych z folderów", e);
        }
    }

    @Transactional(readOnly = true)
    public FolderInTheDisk getFolderById(Long IdFolder) {
        try {
            log.info("Pobieram folder o IdFolder: {}", IdFolder);
            return folderInTheDiskRepository.findById(IdFolder)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono folderu o podanym ID: " + IdFolder));

        } catch (IllegalArgumentException e) {
            log.warn("Brak rekordu w bazie: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania folderu o IdFolder: {}. Powód: ", IdFolder, e);
            throw new RuntimeException("Nie udało się pobrać folderu z bazy danych", e);
        }
    }

    @Transactional(readOnly = true)
    public List<FolderInTheDisk> getAllFolders() {
        log.info("[SERWIS] Pobieram absolutnie wszystkie foldery (FolderInTheDisk) z bazy danych.");
        try {
            List<FolderInTheDisk> allFolders = folderInTheDiskRepository.findAll();
            log.info("[SERWIS] Pomyślnie pobrano wszystkie foldery. Łączna liczba: {}", allFolders.size());
            return allFolders;
        } catch (Exception e) {
            log.error("[SERWIS] Błąd podczas pobierania wszystkich folderów z bazy: ", e);
            return Collections.emptyList();
        }
    }
}