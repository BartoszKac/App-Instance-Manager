package com.example.DynamicCode.code.service;

import com.example.DynamicCode.code.entity.SourceCode;
import com.example.DynamicCode.code.enums.CompileConstants;
import com.example.DynamicCode.code.enums.LanguageType;
import com.example.DynamicCode.code.exception.FileStorageException;
import com.example.DynamicCode.code.exception.InvalidFileNameException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileServiceTest {

    @TempDir
    Path workingDir;

    private final FileService fileService = new FileService();
    private String originalWorkingDir;

    @BeforeEach
    void setUp() {
        originalWorkingDir = CompileConstants.WORKING_DIR;
        CompileConstants.WORKING_DIR = workingDir.toString();
    }

    @AfterEach
    void tearDown() {
        CompileConstants.WORKING_DIR = originalWorkingDir;
    }

    @Test
    void shouldWriteFilesInsideProjectDirectory() throws IOException {
        fileService.saveFilesToDisk(List.of(source("Main.java", "class Main {}")), "project-1");

        assertThat(Files.readString(workingDir.resolve("project-1").resolve("Main.java"))).isEqualTo("class Main {}");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"../evil.sh", "..", ".", "a/b.java", "a\\b.java", "/etc/passwd", "C:\\x.java", ".hidden", "ma in.java"})
    void shouldRejectUnsafeFileNames(String fileName) {
        List<SourceCode> files = List.of(source(fileName, "x"));

        assertThatThrownBy(() -> fileService.validateFileNames(files)).isInstanceOf(InvalidFileNameException.class);
        assertThatThrownBy(() -> fileService.saveFilesToDisk(files, "project-1")).isInstanceOf(InvalidFileNameException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Main.java", "hello_world.py", "test-2.cpp", "run.sh", "data.txt"})
    void shouldAcceptSafeFileNames(String fileName) {
        fileService.validateFileNames(List.of(source(fileName, "x")));
    }

    @Test
    void shouldRejectProjectPathEscapingWorkingDirectory() {
        List<SourceCode> files = List.of(source("Main.java", "x"));

        assertThatThrownBy(() -> fileService.saveFilesToDisk(files, "../outside")).isInstanceOf(InvalidFileNameException.class);
        assertThat(workingDir.getParent().resolve("outside")).doesNotExist();
    }

    @Test
    void shouldThrowWhenFileCannotBeWritten() throws IOException {
        Files.createDirectories(workingDir.resolve("project-1").resolve("Main.java"));

        assertThatThrownBy(() -> fileService.saveFilesToDisk(List.of(source("Main.java", "x")), "project-1"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Main.java");
    }

    @Test
    void shouldReadOnlyClassFiles() throws IOException {
        Path project = Files.createDirectories(workingDir.resolve("project-1"));
        Files.write(project.resolve("Main.class"), new byte[]{1, 2});
        Files.writeString(project.resolve("Main.java"), "x");

        assertThat(fileService.readFilesFromDisk("project-1")).singleElement()
                .satisfies(file -> assertThat(file.getNameFile()).isEqualTo("Main.class"));
    }

    @Test
    void shouldReturnEmptyListWhenProjectDirectoryIsMissing() {
        assertThat(fileService.readFilesFromDisk("missing")).isEmpty();
    }

    private SourceCode source(String name, String code) {
        return new SourceCode(null, name, code, LanguageType.JAVA, 1L);
    }
}
