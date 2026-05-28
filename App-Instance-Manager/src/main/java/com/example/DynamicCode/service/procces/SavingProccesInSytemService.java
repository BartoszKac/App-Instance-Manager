package com.example.DynamicCode.service.procces;

import com.example.DynamicCode.databaseservice.procces.RemoteExecutableService;
import com.example.DynamicCode.model.entity.procces.RemoteExecutable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavingProccesInSytemService {

    private final RemoteExecutableService remoteExecutableService;

    public String save(RemoteExecutable executable) {
        log.info("Proces: Wywołanie zapisu dla pliku: {}", executable.getName());
        return remoteExecutableService.saveAllExecutablesToDb(List.of(executable));
    }

    public String saveAll(List<RemoteExecutable> executables) {
        log.info("Proces: Wywołanie zapisu masowego dla {} plików", executables.size());
        return remoteExecutableService.saveAllExecutablesToDb(executables);
    }

    public List<RemoteExecutable> getById(Long id) {
        log.info("Proces: Pobieranie pliku o ID: {}", id);
        return remoteExecutableService.getAllFilesFromMainClass(id);
    }

    public List<RemoteExecutable> getAll() {
        log.info("Proces: Pobieranie wszystkich plików");
        return remoteExecutableService.getAllExecutables();
    }

    public String update(Long id, RemoteExecutable executable) {
        log.info("Proces: Aktualizacja pliku o ID: {}", id);
        return remoteExecutableService.saveAllExecutablesToDb(List.of(executable) );
    }

    public void deleteById(Long id) {
        log.info("Proces: Usuwanie pliku o ID: {}", id);
        remoteExecutableService.deleteExecutableById(id);
    }

    public String deleteAll() {
        log.warn("Proces: Wywołanie czyszczenia całej bazy danych!");
        return remoteExecutableService.deleteAllExecutablesFromDb();
    }
}