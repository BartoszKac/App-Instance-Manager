package com.example.DynamicCode.databaseservice.procces;

import com.example.DynamicCode.databaseservice.DataBaseProvider;
import com.example.DynamicCode.model.entity.procces.RemoteExecutable;
import com.example.DynamicCode.repository.procces.RemoteExecutableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteExecutableService implements DataBaseProvider<RemoteExecutable> {



    private final RemoteExecutableRepository remoteExecutableRepository;

    @Transactional
    public String saveAllExecutablesToDb(List<RemoteExecutable> executablesFromRequest) {
        try {
            log.info("RemoteExecutableStorage: Rozpoczynam zapis {} zdalnych plików wykonywalnych do bazy...", executablesFromRequest.size());
            remoteExecutableRepository.saveAll(executablesFromRequest);
            log.info("RemoteExecutableStorage: Pliki wykonywalne zostały pomyślnie zapisane!");
            return "All remote executables saved to DB successfully!";
        } catch (Exception e) {
            log.error("RemoteExecutableStorage: Błąd podczas zapisu plików wykonywalnych do bazy. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving remote executables to DB: " + e.getMessage(), e);
        }
    }

    @Transactional
    public RemoteExecutable updateExecutable(RemoteExecutable updatedExecutable) {
        try {
            log.info("RemoteExecutableStorage: Aktualizuję zdalny plik wykonywalny o ID: {}", updatedExecutable.getId());
            return remoteExecutableRepository.save(updatedExecutable);
        } catch (Exception e) {
            log.error("RemoteExecutableStorage: Błąd podczas aktualizacji pliku o ID: {}. Powód: {}", updatedExecutable.getId(), e.getMessage(), e);
            throw new RuntimeException("Nie udało się zaktualizować zdalnego pliku wykonywalnego", e);
        }
    }

    @Transactional(readOnly = true)
    public List<RemoteExecutable> getAllExecutables() {
        try {
            log.info("RemoteExecutableStorage: Pobieram listę wszystkich zdalnych plików wykonywalnych");
            return remoteExecutableRepository.findAll();
        } catch (Exception e) {
            log.error("RemoteExecutableStorage: Błąd podczas pobierania wszystkich plików wykonywalnych. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się pobrać listy plików wykonywalnych", e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<RemoteExecutable> getAllFilesFromMainClass(Long id) {
        try {
            log.info("RemoteExecutableStorage: Pobieram plik o ID: {}", id);

            return remoteExecutableRepository.findById(id)
                    .map(List::of)
                    .orElse(Collections.emptyList());

        } catch (Exception e) {
            log.error("RemoteExecutableStorage: Błąd podczas pobierania pliku o ID: {}.", id, e);
            throw new RuntimeException("Błąd bazy danych przy pobieraniu pliku", e);
        }
    }

    @Transactional
    public void deleteExecutableById(Long id) {
        try {
            log.info("RemoteExecutableStorage: Usuwam zdalny plik wykonywalny o ID: {}", id);
            remoteExecutableRepository.deleteById(id);
            log.info("RemoteExecutableStorage: Pomyślnie usunięto plik o ID: {}", id);
        } catch (Exception e) {
            log.error("RemoteExecutableStorage: Błąd podczas usuwania pliku o ID: {}. Powód: {}", id, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć wskazanego pliku wykonywalnego", e);
        }
    }

    @Transactional
    public String deleteAllExecutablesFromDb() {
        try {
            log.warn("RemoteExecutableStorage: UWAGA: Rozpoczynam czyszczenie tabeli remote_executable!");
            remoteExecutableRepository.deleteAll();
            log.info("RemoteExecutableStorage: Tabela została całkowicie wyczyszczona.");
            return "All remote executables data cleared successfully!";
        } catch (Exception e) {
            log.error("RemoteExecutableStorage: Błąd podczas czyszczenia tabeli. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się wyczyścić danych plików wykonywalnych", e);
        }
    }
}