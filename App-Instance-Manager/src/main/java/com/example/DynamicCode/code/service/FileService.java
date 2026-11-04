package com.example.DynamicCode.code.service;

import com.example.DynamicCode.code.dto.CodeFileDto;
import com.example.DynamicCode.code.entity.SourceCode;
import com.example.DynamicCode.code.enums.CompileConstants;
import com.example.DynamicCode.code.exception.FileStorageException;
import com.example.DynamicCode.code.exception.InvalidFileNameException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FileService {

    private static final Pattern SAFE_FILE_NAME = Pattern.compile("^[A-Za-z0-9_\\-][A-Za-z0-9_.\\-]{0,254}$");

    private final List<String> lines = new ArrayList<>();

    public void writeString(String content) {
        lines.add(content);
    }

    public void deleteString(String content) {
        lines.remove(content);
    }

    @Override
    public String toString() {
        return String.join("\n", lines) + (lines.isEmpty() ? "" : "\n");
    }

    public String generateUniquePath() {
        return UUID.randomUUID().toString();
    }

    public void validateFileNames(List<SourceCode> sourceCodes) {
        sourceCodes.forEach(sourceCode -> validateFileName(sourceCode.getName()));
    }

    public void saveFilesToDisk(List<SourceCode> sourceCodes, String uniquePath) {
        Path projectDir = resolveInside(workingDir(), uniquePath);
        createDirectory(projectDir);
        log.info("Zapisuję {} plików do katalogu {}", sourceCodes.size(), projectDir);
        sourceCodes.forEach(sourceCode -> writeFile(projectDir, sourceCode));
    }

    public List<CodeFileDto> readFilesFromDisk(String uniquePath) {
        File projectDir = resolveInside(workingDir(), uniquePath).toFile();
        File[] classFiles = projectDir.listFiles((dir, name) -> name.endsWith(".class"));
        if (classFiles == null || classFiles.length == 0) {
            log.warn("Brak plików .class w katalogu {}", projectDir);
            return new ArrayList<>();
        }
        List<CodeFileDto> compiledFiles = new ArrayList<>();
        for (File file : classFiles) {
            compiledFiles.add(new CodeFileDto(file.getName(), readBytes(file.toPath())));
        }
        return compiledFiles;
    }

    private void validateFileName(String fileName) {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            throw new InvalidFileNameException(fileName);
        }
    }

    private void writeFile(Path projectDir, SourceCode sourceCode) {
        validateFileName(sourceCode.getName());
        Path filePath = resolveInside(projectDir, sourceCode.getName());
        try {
            Files.writeString(filePath, sourceCode.getCode() == null ? "" : sourceCode.getCode());
            log.debug("Zapisano plik {}", filePath);
        } catch (IOException e) {
            throw new FileStorageException("Nie udało się zapisać pliku: " + sourceCode.getName(), e);
        }
    }

    private void createDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new FileStorageException("Nie udało się utworzyć katalogu projektu: " + directory.getFileName(), e);
        }
    }

    private byte[] readBytes(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new FileStorageException("Nie udało się odczytać pliku: " + file.getFileName(), e);
        }
    }

    private Path workingDir() {
        return Paths.get(CompileConstants.WORKING_DIR).toAbsolutePath().normalize();
    }

    private Path resolveInside(Path base, String child) {
        Path resolved = base.resolve(child).normalize();
        if (!resolved.startsWith(base) || resolved.equals(base)) {
            throw new InvalidFileNameException(child);
        }
        return resolved;
    }
}
