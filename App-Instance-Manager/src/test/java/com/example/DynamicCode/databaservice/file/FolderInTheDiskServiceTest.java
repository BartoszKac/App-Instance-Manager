package com.example.DynamicCode.databaservice.file;

import com.example.DynamicCode.databaseservice.file.FolderInTheDiskService;
import com.example.DynamicCode.model.entity.file.FolderInTheDisk;
import com.example.DynamicCode.repository.file.FolderInTheDiskRepository;
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
class FolderInTheDiskServiceTest {

    @Mock
    private FolderInTheDiskRepository folderInTheDiskRepository;

    @InjectMocks
    private FolderInTheDiskService folderInTheDiskService;

    @Test
    void shouldSaveAllFoldersSuccessfully() {
        // GIVEN
        List<FolderInTheDisk> folders = Arrays.asList(
                new FolderInTheDisk(1L, "src", "/project/src", false, 10L),
                new FolderInTheDisk(2L, "out", "/project/out", true, 10L)
        );
        when(folderInTheDiskRepository.saveAll(folders)).thenReturn(folders);

        // WHEN
        String result = folderInTheDiskService.saveAllFoldersToDb(folders);

        // THEN
        assertThat(result).isEqualTo("All folders saved to DB successfully!");
        verify(folderInTheDiskRepository, times(1)).saveAll(folders);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenSaveAllFoldersFails() {
        // GIVEN
        when(folderInTheDiskRepository.saveAll(any())).thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThatThrownBy(() -> folderInTheDiskService.saveAllFoldersToDb(Collections.emptyList()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error saving folders to DB");
    }

    @Test
    void shouldUpdateFolderSuccessfully() {
        // GIVEN
        FolderInTheDisk folder = new FolderInTheDisk(1L, "target", "/project/target", true, 20L);
        when(folderInTheDiskRepository.save(folder)).thenReturn(folder);

        // WHEN
        FolderInTheDisk result = folderInTheDiskService.updateFolder(folder);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getIdFolder()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("target");
        verify(folderInTheDiskRepository, times(1)).save(folder);
    }

    @Test
    void shouldGetAllFoldersFromMainClass() {
        // GIVEN
        Long idMainClass = 15L;
        List<FolderInTheDisk> expectedFolders = Arrays.asList(
                new FolderInTheDisk(1L, "src", "/src", false, idMainClass)
        );
        when(folderInTheDiskRepository.findByIdMainClass(idMainClass)).thenReturn(expectedFolders);

        // WHEN
        List<FolderInTheDisk> result = folderInTheDiskService.getAllFoldersFromMainClass(idMainClass);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdMainClass()).isEqualTo(idMainClass);
        verify(folderInTheDiskRepository, times(1)).findByIdMainClass(idMainClass);
    }

    @Test
    void shouldReturnEmptyListWhenGetAllFoldersFromMainClassFails() {
        // GIVEN
        Long idMainClass = 15L;
        when(folderInTheDiskRepository.findByIdMainClass(idMainClass)).thenThrow(new RuntimeException("Timeout"));

        // WHEN
        List<FolderInTheDisk> result = folderInTheDiskService.getAllFoldersFromMainClass(idMainClass);

        // THEN
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteAllFoldersByMainClass() {
        // GIVEN
        Long idMainClass = 99L;
        doNothing().when(folderInTheDiskRepository).deleteByIdMainClass(idMainClass);

        // WHEN
        folderInTheDiskService.deleteAllFoldersByMainClass(idMainClass);

        // THEN
        verify(folderInTheDiskRepository, times(1)).deleteByIdMainClass(idMainClass);
    }

    @Test
    void shouldDeleteFolderById() {
        // GIVEN
        Long idFolder = 5L;
        doNothing().when(folderInTheDiskRepository).deleteById(idFolder);

        // WHEN
        folderInTheDiskService.deleteFolderById(idFolder);

        // THEN
        verify(folderInTheDiskRepository, times(1)).deleteById(idFolder);
    }

    @Test
    void shouldDeleteAllFoldersFromDb() {
        // GIVEN
        doNothing().when(folderInTheDiskRepository).deleteAll();

        // WHEN
        String result = folderInTheDiskService.deleteAllFoldersFromDb();

        // THEN
        assertThat(result).isEqualTo("All folders cleared successfully!");
        verify(folderInTheDiskRepository, times(1)).deleteAll();
    }

    @Test
    void shouldGetFolderByIdWhenFolderExists() {
        // GIVEN
        Long idFolder = 100L;
        FolderInTheDisk folder = new FolderInTheDisk(idFolder, "resources", "/resources", false, 1L);
        when(folderInTheDiskRepository.findById(idFolder)).thenReturn(Optional.of(folder));

        // WHEN
        FolderInTheDisk result = folderInTheDiskService.getFolderById(idFolder);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getIdFolder()).isEqualTo(idFolder);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFolderDoesNotExist() {
        // GIVEN
        Long nonExistingId = 404L;
        when(folderInTheDiskRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> folderInTheDiskService.getFolderById(nonExistingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nie znaleziono folderu o podanym ID");
    }

    @Test
    void shouldGetAllFoldersSuccessfully() {
        // GIVEN
        List<FolderInTheDisk> allFolders = Arrays.asList(new FolderInTheDisk(), new FolderInTheDisk());
        when(folderInTheDiskRepository.findAll()).thenReturn(allFolders);

        // WHEN
        List<FolderInTheDisk> result = folderInTheDiskService.getAllFolders();

        // THEN
        assertThat(result).hasSize(2);
        verify(folderInTheDiskRepository, times(1)).findAll();
    }
}