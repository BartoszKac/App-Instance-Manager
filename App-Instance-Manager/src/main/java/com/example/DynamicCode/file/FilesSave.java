package com.example.DynamicCode.file;

import com.example.DynamicCode.constants.code.CompileConstats;
import com.example.DynamicCode.model.dto.file.CodeFileDto;
import com.example.DynamicCode.model.entity.code.SourceCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class FilesSave {



    List<String> lines = new ArrayList<>();

    public void writeString( String content) {
        lines.add(content);
    }
    public void deleteString(String content) {
        lines.remove(content);
    }

        @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public void saveFilesToDisk(List<SourceCode> sourceCodes) {

        String uniquePath = getUniquePathFromRequests(sourceCodes);
        System.out.println("Unikalna ścieżka dla plików: " + uniquePath);

        File directory = new File(CompileConstats.WORKING_DIR + "/" + uniquePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        for (SourceCode fileRequest : sourceCodes) {
            try {
                Path filePath = Paths.get(CompileConstats.WORKING_DIR + "/" +uniquePath, fileRequest.getName());

                Files.writeString(filePath, fileRequest.getCode());

                System.out.println("Zapisano plik na dysku: " + filePath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Nie udało się zapisać pliku: " + fileRequest.getName() + " na dysku!");
                e.printStackTrace();
            }
        }
    }

    public List<CodeFileDto> readFilesFromDisk(Long projectIdandMainClass) {
        List<CodeFileDto> compiledFiles = new ArrayList<>();
        File workingDir = new File(CompileConstats.WORKING_DIR + "/" + projectIdandMainClass);

        if (!workingDir.exists() || !workingDir.isDirectory()) {
            log.warn("Katalog roboczy nie istnieje lub nie jest folderem: {}", CompileConstats.WORKING_DIR);
            return compiledFiles;
        }

        File[] classFiles = workingDir.listFiles((dir, name) -> name.endsWith(".class"));

        if (classFiles == null || classFiles.length == 0) {
            log.warn("Nie znaleziono żadnych plików .class w katalogu: {}", CompileConstats.WORKING_DIR);
            return compiledFiles;
        }

        for (File file : classFiles) {
            try {
                byte[] fileBytes = Files.readAllBytes(file.toPath());
                String base64Code = Base64.getEncoder().encodeToString(fileBytes);

                compiledFiles.add(new CodeFileDto(file.getName(), base64Code));
                log.info("Pomyślnie odczytano z dysku plik binarny: {}", file.getName());
            } catch (Exception e) {
                log.error("Błąd podczas odczytu pliku z dysku: {}", file.getName(), e);
            }
        }

        return compiledFiles;
    }


    private String getUniquePathFromRequests(List<SourceCode> sourceCodes) {
        if (sourceCodes == null || sourceCodes.isEmpty()) {
            throw new IllegalArgumentException("Lista sourceCodes nie może być pusta ani null");
        }
        return String.valueOf(sourceCodes.get(0).getIdManClass());
    }
    }

