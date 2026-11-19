package com.example.DynamicCode.process.service;

import com.example.DynamicCode.process.entity.RemoteExecutable;
import com.example.DynamicCode.process.exception.InvalidExecutableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavingProcessInSystemService {

    static final int MAX_DESCRIPTION_LENGTH = 200;

    private final RemoteExecutableService remoteExecutableService;
    private final Clock clock;

    public String save(RemoteExecutable executable) {
        return saveAll(List.of(executable));
    }

    public String saveAll(List<RemoteExecutable> executables) {
        executables.forEach(this::prepare);
        log.info("Saving {} uploaded file(s)", executables.size());
        return remoteExecutableService.saveAllExecutablesToDb(executables);
    }

    public List<RemoteExecutable> getById(Long id) {
        return remoteExecutableService.getAllFilesFromMainClass(id);
    }

    public List<RemoteExecutable> getAll() {
        return remoteExecutableService.getAllExecutables();
    }

    public String update(Long id, RemoteExecutable executable) {
        executable.setId(id);
        return saveAll(List.of(executable));
    }

    public void deleteById(Long id) {
        remoteExecutableService.deleteExecutableById(id);
    }

    public String deleteAll() {
        log.warn("Deleting all uploaded files");
        return remoteExecutableService.deleteAllExecutablesFromDb();
    }

    private void prepare(RemoteExecutable executable) {
        validate(executable);
        executable.setSizeBytes(executable.getContents() == null ? null : (long) executable.getContents().length);
        executable.setUploadedAt(storedUploadTime(executable.getId()));
    }

    private Instant storedUploadTime(Long id) {
        if (id == null) {
            return Instant.now(clock);
        }
        return remoteExecutableService.findExecutable(id).map(RemoteExecutable::getUploadedAt).orElseGet(() -> Instant.now(clock));
    }

    private void validate(RemoteExecutable executable) {
        if (!ExecutableFileNameRule.isSafe(executable.getName())) {
            throw new InvalidExecutableException("Niedozwolona nazwa pliku. Usuń z nazwy ukośniki, znaki sterujące i początkowy myślnik.");
        }
        String description = executable.getDescription();
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidExecutableException("Opis może mieć najwyżej " + MAX_DESCRIPTION_LENGTH + " znaków.");
        }
    }
}
