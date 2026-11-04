package com.example.DynamicCode.code.service;


import com.example.DynamicCode.code.enums.LanguageType;
import com.example.DynamicCode.code.service.CompiledCodeService;
import com.example.DynamicCode.code.entity.CompiledCode;
import com.example.DynamicCode.code.repository.CompiledCodeRepository;
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
class CompiledCodeServiceTest {

    @Mock
    private CompiledCodeRepository compiledCodeRepository;

    @InjectMocks
    private CompiledCodeService compiledCodeService;

    @Test
    void shouldSaveAllFilesSuccessfully() {
        // GIVEN
        List<CompiledCode> codes = Arrays.asList(
                new CompiledCode(1L, "Main.class", "bytecode123".getBytes(), LanguageType.JAVA, 10L),
                new CompiledCode(2L, "Utils.class", "bytecode456".getBytes(), LanguageType.JAVA, 10L)
        );
        when(compiledCodeRepository.saveAll(codes)).thenReturn(codes);

        // WHEN
        String result = compiledCodeService.saveAllFilesToDb(codes);

        // THEN
        assertThat(result).isEqualTo("All compiled files saved to DB successfully!");
        verify(compiledCodeRepository, times(1)).saveAll(codes);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenSaveAllFilesFails() {
        // GIVEN
        when(compiledCodeRepository.saveAll(any())).thenThrow(new RuntimeException("Database connection error"));

        // WHEN & THEN
        assertThatThrownBy(() -> compiledCodeService.saveAllFilesToDb(Collections.emptyList()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error saving compiled files to DB");
    }

    @Test
    void shouldUpdateFileSuccessfully() {
        // GIVEN
        CompiledCode originalCode = new CompiledCode(1L, "App.class", "oldBytecode".getBytes(), LanguageType.JAVA, 20L);
        when(compiledCodeRepository.save(originalCode)).thenReturn(originalCode);

        // WHEN
        CompiledCode updatedResult = compiledCodeService.updateFile(originalCode);

        // THEN
        assertThat(updatedResult).isNotNull();
        assertThat(updatedResult.getIdCode()).isEqualTo(1L);
        verify(compiledCodeRepository, times(1)).save(originalCode);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenUpdateFileFails() {
        // GIVEN
        CompiledCode code = new CompiledCode(1L, "App.class", "bytecode".getBytes(), LanguageType.JAVA, 20L);
        when(compiledCodeRepository.save(any())).thenThrow(new RuntimeException("Optimistic locking fail"));

        // WHEN & THEN
        assertThatThrownBy(() -> compiledCodeService.updateFile(code))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zaktualizować skompilowanego pliku");
    }

    @Test
    void shouldGetAllFilesFromMainClass() {
        // GIVEN
        Long idMainClass = 99L;
        List<CompiledCode> expectedList = Arrays.asList(
                new CompiledCode(5L, "Module.class", "bytecode".getBytes(), LanguageType.JAVA, idMainClass)
        );
        when(compiledCodeRepository.findByIdManClass(idMainClass)).thenReturn(expectedList);

        // WHEN
        List<CompiledCode> result = compiledCodeService.getAllFilesFromMainClass(idMainClass);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdManClass()).isEqualTo(idMainClass);
        verify(compiledCodeRepository, times(1)).findByIdManClass(idMainClass);
    }

    @Test
    void shouldReturnEmptyListWhenGetAllFilesFromMainClassFails() {
        // GIVEN
        Long idMainClass = 99L;
        when(compiledCodeRepository.findByIdManClass(idMainClass)).thenThrow(new RuntimeException("Query timeout"));

        // WHEN
        List<CompiledCode> result = compiledCodeService.getAllFilesFromMainClass(idMainClass);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteAllFilesByMainClass() {
        // GIVEN
        Long idMainClass = 50L;
        doNothing().when(compiledCodeRepository).deleteByIdManClass(idMainClass);

        // WHEN
        compiledCodeService.deleteAllFilesByMainClass(idMainClass);

        // THEN
        verify(compiledCodeRepository, times(1)).deleteByIdManClass(idMainClass);
    }

    @Test
    void shouldDeleteFileById() {
        // GIVEN
        Long idCode = 77L;
        doNothing().when(compiledCodeRepository).deleteById(idCode);

        // WHEN
        compiledCodeService.deleteFileById(idCode);

        // THEN
        verify(compiledCodeRepository, times(1)).deleteById(idCode);
    }

    @Test
    void shouldDeleteAllFilesFromDb() {
        // GIVEN
        doNothing().when(compiledCodeRepository).deleteAll();

        // WHEN
        String result = compiledCodeService.deleteAllFilesFromDb();

        // THEN
        assertThat(result).isEqualTo("All compiled data cleared successfully!");
        verify(compiledCodeRepository, times(1)).deleteAll();
    }

    @Test
    void shouldGetFileByIdWhenFileExists() {
        // GIVEN
        Long idCode = 100L;
        CompiledCode code = new CompiledCode(idCode, "Run.class", "bytecode".getBytes(), LanguageType.JAVA, 1L);
        when(compiledCodeRepository.findById(idCode)).thenReturn(Optional.of(code));

        // WHEN
        CompiledCode result = compiledCodeService.getFileById(idCode);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getIdCode()).isEqualTo(idCode);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFileDoesNotExist() {
        // GIVEN
        Long nonExistingId = 404L;
        when(compiledCodeRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> compiledCodeService.getFileById(nonExistingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nie znaleziono skompilowanego pliku o podanym ID");
    }
}