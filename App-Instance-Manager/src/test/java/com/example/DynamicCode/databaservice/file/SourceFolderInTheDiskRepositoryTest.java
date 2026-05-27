package com.example.DynamicCode.databaservice.file;

import com.example.DynamicCode.databaseservice.file.SourceFolderInTheDiskService;
import com.example.DynamicCode.model.entity.file.SourceFolderInTheDisk;
import com.example.DynamicCode.repository.file.SourceFolderInTheDiskRepository;
import org.junit.jupiter.api.BeforeEach;
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
class SourceFolderInTheDiskServiceTest {

    @Mock
    private SourceFolderInTheDiskRepository sourceFolderInTheDiskRepository;

    @InjectMocks
    private SourceFolderInTheDiskService sourceFolderInTheDiskService;

    private SourceFolderInTheDisk sampleFolder;

    @BeforeEach
    void setUp() {
        sampleFolder = new SourceFolderInTheDisk(1L, "src", "/project/src", 10L);
    }

    // --- TESTY DLA: saveToDb ---

    @Test
    void shouldSaveToDbSuccessfully() {
        // GIVEN
        when(sourceFolderInTheDiskRepository.save(any(SourceFolderInTheDisk.class))).thenReturn(sampleFolder);

        // WHEN
        String result = sourceFolderInTheDiskService.saveToDb(sampleFolder);

        // THEN
        assertThat(result).isEqualTo("All folders saved to DB successfully!");
        verify(sourceFolderInTheDiskRepository, times(1)).save(sampleFolder);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenSaveToDbFails() {
        // GIVEN
        when(sourceFolderInTheDiskRepository.save(any(SourceFolderInTheDisk.class)))
                .thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThatThrownBy(() -> sourceFolderInTheDiskService.saveToDb(sampleFolder))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error saving folders to DB: Database error");
    }

    // --- TESTY DLA: updateFolder ---

    @Test
    void shouldUpdateFolderSuccessfully() {
        // GIVEN
        when(sourceFolderInTheDiskRepository.save(any(SourceFolderInTheDisk.class))).thenReturn(sampleFolder);

        // WHEN
        SourceFolderInTheDisk result = sourceFolderInTheDiskService.updateFolder(sampleFolder);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(sourceFolderInTheDiskRepository, times(1)).save(sampleFolder);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenUpdateFolderFails() {
        // GIVEN
        when(sourceFolderInTheDiskRepository.save(any(SourceFolderInTheDisk.class)))
                .thenThrow(new RuntimeException("Update failed"));

        // WHEN & THEN
        assertThatThrownBy(() -> sourceFolderInTheDiskService.updateFolder(sampleFolder))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się zaktualizować folderu");
    }

    // --- TESTY DLA: getFoldersFromSourceCodeId ---

    @Test
    void shouldGetFoldersFromSourceCodeIdSuccessfully() {
        // GIVEN
        Long sourceCodeId = 10L;
        when(sourceFolderInTheDiskRepository.findBySourceCodeId(sourceCodeId)).thenReturn(sampleFolder);

        // WHEN
        SourceFolderInTheDisk result = sourceFolderInTheDiskService.getFoldersFromSourceCodeId(sourceCodeId);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getSourceCodeId()).isEqualTo(sourceCodeId);
        verify(sourceFolderInTheDiskRepository, times(1)).findBySourceCodeId(sourceCodeId);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenGetFoldersFromSourceCodeIdFails() {
        // GIVEN
        Long sourceCodeId = 10L;
        when(sourceFolderInTheDiskRepository.findBySourceCodeId(sourceCodeId))
                .thenThrow(new RuntimeException("Connection error"));

        // WHEN & THEN
        assertThatThrownBy(() -> sourceFolderInTheDiskService.getFoldersFromSourceCodeId(sourceCodeId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Nie udało się pobrać folderu");
    }

    // --- TESTY DLA: deleteFoldersBySourceCodeId ---

    @Test
    void shouldDeleteFoldersBySourceCodeIdSuccessfully() {
        // GIVEN
        Long sourceCodeId = 10L;
        doNothing().when(sourceFolderInTheDiskRepository).deleteBySourceCodeId(sourceCodeId);

        // WHEN
        sourceFolderInTheDiskService.deleteFoldersBySourceCodeId(sourceCodeId);

        // THEN
        verify(sourceFolderInTheDiskRepository, times(1)).deleteBySourceCodeId(sourceCodeId);
    }

    // --- TESTY DLA: deleteFolderById ---

    @Test
    void shouldDeleteFolderByIdSuccessfully() {
        // GIVEN
        Long idFolder = 1L;
        doNothing().when(sourceFolderInTheDiskRepository).deleteById(idFolder);

        // WHEN
        sourceFolderInTheDiskService.deleteFolderById(idFolder);

        // THEN
        verify(sourceFolderInTheDiskRepository, times(1)).deleteById(idFolder);
    }

    // --- TESTY DLA: deleteAllFoldersFromDb ---

    @Test
    void shouldDeleteAllFoldersFromDbSuccessfully() {
        // GIVEN
        doNothing().when(sourceFolderInTheDiskRepository).deleteAll();

        // WHEN
        String result = sourceFolderInTheDiskService.deleteAllFoldersFromDb();

        // THEN
        assertThat(result).isEqualTo("All folders cleared successfully!");
        verify(sourceFolderInTheDiskRepository, times(1)).deleteAll();
    }

    // --- TESTY DLA: getFolderById ---

    @Test
    void shouldGetFolderByIdSuccessfully() {
        // GIVEN
        Long idFolder = 1L;
        when(sourceFolderInTheDiskRepository.findById(idFolder)).thenReturn(Optional.of(sampleFolder));

        // WHEN
        SourceFolderInTheDisk result = sourceFolderInTheDiskService.getFolderById(idFolder);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(idFolder);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFolderNotFoundById() {
        // GIVEN
        Long idFolder = 999L;
        when(sourceFolderInTheDiskRepository.findById(idFolder)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> sourceFolderInTheDiskService.getFolderById(idFolder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nie znaleziono folderu o podanym ID: " + idFolder);
    }

    // --- TESTY DLA: getAllFolders ---

    @Test
    void shouldReturnAllFolders() {
        // GIVEN
        List<SourceFolderInTheDisk> mockList = Arrays.asList(
                sampleFolder,
                new SourceFolderInTheDisk(2L, "out", "/project/out", 10L)
        );
        when(sourceFolderInTheDiskRepository.findAll()).thenReturn(mockList);

        // WHEN
        List<SourceFolderInTheDisk> result = sourceFolderInTheDiskService.getAllFolders();

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).extracting(SourceFolderInTheDisk::getName).containsExactly("src", "out");
    }

    @Test
    void shouldReturnEmptyListWhenGetAllFoldersFails() {
        // GIVEN
        when(sourceFolderInTheDiskRepository.findAll()).thenThrow(new RuntimeException("DB crash"));

        // WHEN
        List<SourceFolderInTheDisk> result = sourceFolderInTheDiskService.getAllFolders();

        // THEN
        assertThat(result).isEmpty();
    }

    // --- TESTY DLA: getProjectPathBySourceCodeId ---

    @Test
    void shouldGetProjectPathBySourceCodeIdSuccessfully() {
        // GIVEN
        Long sourceCodeId = 10L;
        when(sourceFolderInTheDiskRepository.findBySourceCodeId(sourceCodeId)).thenReturn(sampleFolder);

        // WHEN
        String result = sourceFolderInTheDiskService.getProjectPathBySourceCodeId(sourceCodeId);

        // THEN
        assertThat(result).isEqualTo("/project/src");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenProjectPathFolderIsNull() {
        // GIVEN
        Long sourceCodeId = 10L;
        when(sourceFolderInTheDiskRepository.findBySourceCodeId(sourceCodeId)).thenReturn(null);

        // WHEN & THEN
        assertThatThrownBy(() -> sourceFolderInTheDiskService.getProjectPathBySourceCodeId(sourceCodeId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Brak folderu powiązanego z projektem o ID: " + sourceCodeId);
    }
}