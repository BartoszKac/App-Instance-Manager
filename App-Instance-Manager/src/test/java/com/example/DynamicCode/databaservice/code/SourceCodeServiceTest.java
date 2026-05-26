package com.example.DynamicCode.databaservice.code;

import com.example.DynamicCode.constants.code.LanguageType;
import com.example.DynamicCode.databaseservice.code.SourceCodeService;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.repository.code.CodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SourceCodeServiceTest {

    @Mock
    private CodeRepository codeRepository;

    @InjectMocks
    private SourceCodeService sourceCodeService;

    @Test
    void shouldSaveAllFilesSuccessfully() {
        // GIVEN
        List<SourceCode> files = Arrays.asList(
                new SourceCode(1L, "Main.java", "public class Main {}", LanguageType.JAVA, 100L),
                new SourceCode(2L, "Utils.java", "public class Utils {}", LanguageType.JAVA, 100L)
        );
        when(codeRepository.saveAll(files)).thenReturn(files);

        // WHEN
        String result = sourceCodeService.saveAllFilesToDb(files);

        // THEN
        assertThat(result).isEqualTo("All files saved to DB successfully!");
        verify(codeRepository, times(1)).saveAll(files);
    }

    @Test
    void shouldThrowExceptionWhenSaveAllFilesFails() {
        // GIVEN
        when(codeRepository.saveAll(any())).thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThatThrownBy(() -> sourceCodeService.saveAllFilesToDb(Collections.emptyList()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error saving files to DB");
    }

    @Test
    void shouldUpdateFileSuccessfully() {
        // GIVEN
        SourceCode file = new SourceCode(1L, "Script.py", "print('Hello')", LanguageType.PYTHON, 200L);
        when(codeRepository.save(file)).thenReturn(file);

        // WHEN
        SourceCode result = sourceCodeService.updateFile(file);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Script.py");
        verify(codeRepository, times(1)).save(file);
    }

    @Test
    void shouldGetAllFilesFromMainClass() {
        // GIVEN
        Long idMainClass = 100L;
        List<SourceCode> expectedFiles = Arrays.asList(
                new SourceCode(1L, "Main.java", "code", LanguageType.JAVA, idMainClass)
        );
        when(codeRepository.findByIdManClass(idMainClass)).thenReturn(expectedFiles);

        // WHEN
        List<SourceCode> result = sourceCodeService.getAllFilesFromMainClass(idMainClass);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(codeRepository, times(1)).findByIdManClass(idMainClass);
    }

    @Test
    void shouldDeleteAllFilesByMainClass() {
        // GIVEN
        Long idMainClass = 300L;
        doNothing().when(codeRepository).deleteByIdManClass(idMainClass);

        // WHEN
        sourceCodeService.deleteAllFilesByMainClass(idMainClass);

        // THEN
        verify(codeRepository, times(1)).deleteByIdManClass(idMainClass);
    }

    @Test
    void shouldDeleteFileById() {
        // GIVEN
        Long idCode = 15L;
        doNothing().when(codeRepository).deleteById(idCode);

        // WHEN
        sourceCodeService.deleteFileById(idCode);

        // THEN
        verify(codeRepository, times(1)).deleteById(idCode);
    }

    @Test
    void shouldDeleteAllFilesFromDb() {
        // GIVEN
        doNothing().when(codeRepository).deleteAll();

        // WHEN
        String result = sourceCodeService.deleteAllFilesFromDb();

        // THEN
        assertThat(result).isEqualTo("All data cleared successfully!");
        verify(codeRepository, times(1)).deleteAll();
    }

    @Test
    void shouldGetFileByIdWhenExists() {
        // GIVEN
        Long idCode = 1L;
        SourceCode file = new SourceCode(idCode, "Test.sh", "echo 1", LanguageType.BASH, 50L);
        when(codeRepository.findById(idCode)).thenReturn(Optional.of(file));

        // WHEN
        SourceCode result = sourceCodeService.getFileById(idCode);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(idCode);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFileDoesNotExist() {
        // GIVEN
        Long nonExistingId = 999L;
        when(codeRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> sourceCodeService.getFileById(nonExistingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nie znaleziono pliku o podanym ID");
    }

    @Test
    void shouldGetAllFilesFromDb() {
        // GIVEN
        List<SourceCode> allFiles = Arrays.asList(new SourceCode(), new SourceCode());
        when(codeRepository.findAll()).thenReturn(allFiles);

        // WHEN
        List<SourceCode> result = sourceCodeService.getAllFilesFromMainClass();

        // THEN
        assertThat(result).hasSize(2);
        verify(codeRepository, times(1)).findAll();
    }
}
