package com.example.DynamicCode.service.file;

import com.example.DynamicCode.constants.code.CompileConstats;
import com.example.DynamicCode.constants.code.LanguageType;
import com.example.DynamicCode.databaseservice.code.CompiledCodeService;
import com.example.DynamicCode.databaseservice.file.CompileFolderInTheDiskService;
import com.example.DynamicCode.databaseservice.file.SourceFolderInTheDiskService;
import com.example.DynamicCode.model.entity.code.CompiledCode;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.model.entity.file.CompiledFolderInTheDisk;
import com.example.DynamicCode.model.entity.file.SourceFolderInTheDisk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Gwarantuje automatyczny rollback bazy po każdym teście
class SavingDataInSystemServiceTest {

    @Autowired
    private SavingDataInSystemService savingDataInSystemService;

    @Autowired
    private SourceFolderInTheDiskService sourceFolderInTheDiskService;

    @Autowired
    private CompileFolderInTheDiskService compileFolderInTheDiskService;

    @Autowired
    private CompiledCodeService compiledCodeService;

    // Bezpieczny, wirtualny folder tworzony przez JUnit i automatycznie sprzątany po testach
    @TempDir
    Path sharedTempDir;

    @BeforeEach
    void setUp() {
        // Przekierowujemy zapis plików na nasz katalog tymczasowy
        CompileConstats.WORKING_DIR = sharedTempDir.toAbsolutePath().toString();
    }

    @Test
    @DisplayName("Powinien poprawnie zapisać kod źródłowy w systemie oraz utworzyć folder na dysku")
    void shouldSuccessfullySaveSourceCodeInSystem() {
        // GIVEN
        Long sampleMainClassId = 777L;
        SourceCode sourceFile = new SourceCode();
        sourceFile.setName("Main.java");
        sourceFile.setCode("public class Main { }");
        sourceFile.setIdManClass(sampleMainClassId);
        sourceFile.setLanguage(LanguageType.JAVA);

        List<SourceCode> sourceCodes = List.of(sourceFile);

        // WHEN
        String generatedUuidPath = savingDataInSystemService.saveSourceCodeIntheSystem(sourceCodes);

        // THEN
        // 1. Weryfikacja unikalnej ścieżki (UUID)
        assertNotNull(generatedUuidPath);
        assertFalse(generatedUuidPath.isEmpty());

        // 2. Weryfikacja fizycznego pliku na dysku tymczasowym
        Path expectedFilePath = sharedTempDir.resolve(generatedUuidPath).resolve("Main.java");
        assertTrue(Files.exists(expectedFilePath), "Plik źródłowy powinien zostać fizycznie utworzony na dysku!");

        // 3. Weryfikacja zapisu metadanych folderu w bazie danych
        SourceFolderInTheDisk savedFolder = sourceFolderInTheDiskService.getFoldersFromSourceCodeId(sampleMainClassId);
        assertNotNull(savedFolder, "Folder źródłowy powinien zostać zapisany w bazie danych!");
        assertEquals(generatedUuidPath, savedFolder.getPath());
        assertEquals(sampleMainClassId, savedFolder.getSourceCodeId());
    }

    @Test
    @DisplayName("Powinien poprawnie przetworzyć pliki .class po kompilacji i zapisać je oraz folder skompilowany w bazie danych")
    void shouldSuccessfullyHandleCompilationResultAndSaveToDb() throws IOException {
        // GIVEN
        Long sampleMainClassId = 888L;
        String mockFolderUuid = "mock-compilation-folder-uuid";

        // 1. Symulujemy, że w bazie istnieje już powiązany folder źródłowy (zgodnie z krokiem 1 logiki biznesowej)
        SourceFolderInTheDisk existingSourceFolder = new SourceFolderInTheDisk();
        existingSourceFolder.setPath(mockFolderUuid);
        existingSourceFolder.setSourceCodeId(sampleMainClassId);
        existingSourceFolder.setName("Katalog projektu " + sampleMainClassId);
        sourceFolderInTheDiskService.saveToDb(existingSourceFolder);

        // 2. Tworzymy na naszym wirtualnym dysku strukturę plików symulującą udaną kompilację
        Path projectFolderOnDisk = sharedTempDir.resolve(mockFolderUuid);
        Files.createDirectories(projectFolderOnDisk);
        Path compiledClassFile = projectFolderOnDisk.resolve("Main.class");
        // Zapisujemy przykładowy fake bytecode (np. nagłówek pliku klasy Java CAFEBABE)
        Files.write(compiledClassFile, new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});

        // 3. Przygotowujemy listę plików źródłowych wejściowych do mappera
        SourceCode sourceFile = new SourceCode();
        sourceFile.setName("Main.java");
        sourceFile.setIdManClass(sampleMainClassId);
        sourceFile.setLanguage(LanguageType.JAVA);
        List<SourceCode> sourceFiles = List.of(sourceFile);

        // WHEN
        savingDataInSystemService.handleCompilationResult(sampleMainClassId, sourceFiles);

        // THEN
        // 1. Sprawdzamy, czy pliki binarne (.class) trafiły do bazy danych
        List<CompiledCode> savedCompiledCodes = compiledCodeService.getAllFilesFromMainClass(sampleMainClassId);
        assertNotNull(savedCompiledCodes);
        assertFalse(savedCompiledCodes.isEmpty(), "Skompilowane pliki powinny zostać zapisane w bazie!");
        assertEquals("Main.class", savedCompiledCodes.get(0).getName());

        // 2. Sprawdzamy, czy poprawnie utworzył się i zapisał dedykowany folder skompilowany
        // UWAGA: Zmień nazwę metody pobierania poniżej na taką, jaką faktycznie posiadasz w CompileFolderInTheDiskService
        CompiledFolderInTheDisk savedCompiledFolder = compileFolderInTheDiskService.getFoldersFromCompiledCodeId(sampleMainClassId);

        assertNotNull(savedCompiledFolder, "Folder skompilowany powinien zostać zarejestrowany w bazie danych!");
        assertEquals(mockFolderUuid, savedCompiledFolder.getPath());
    }

    @Test
    @DisplayName("Powinien wyrzucić wyjątek IllegalArgumentException, gdy lista plików do zapisu jest pusta lub null")
    void shouldThrowExceptionWhenSourceCodesListIsInvalid() {
        // WHEN & THEN
        IllegalArgumentException exceptionNull = assertThrows(IllegalArgumentException.class, () ->
                savingDataInSystemService.saveSourceCodeIntheSystem(null)
        );
        assertEquals("Lista plików do zapisu jest pusta!", exceptionNull.getMessage());

        IllegalArgumentException exceptionEmpty = assertThrows(IllegalArgumentException.class, () ->
                savingDataInSystemService.saveSourceCodeIntheSystem(List.of())
        );
        assertEquals("Lista plików do zapisu jest pusta!", exceptionEmpty.getMessage());
    }
}