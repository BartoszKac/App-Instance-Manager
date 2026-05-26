package com.example.DynamicCode.databaservice.deploy;


import com.example.DynamicCode.databaseservice.deploy.RemoteSerwerService;
import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.repository.deploy.RemoteSerwerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoteSerwerServiceTest {

    @Mock
    private RemoteSerwerRepository deployConfigRepository;

    @InjectMocks
    private RemoteSerwerService remoteSerwerService;

    @Test
    void shouldSaveAllConfigsToDbSuccessfully() {
        // GIVEN
        List<RemoteSerwerConfiguration> configs = Arrays.asList(
                new RemoteSerwerConfiguration(1L, "Server-1", "192.168.1.1", "admin", "pass1"),
                new RemoteSerwerConfiguration(2L, "Server-2", "192.168.1.2", "root", "pass2")
        );
        when(deployConfigRepository.saveAll(configs)).thenReturn(configs);

        // WHEN
        String result = remoteSerwerService.saveAllConfigsToDb(configs);

        // THEN
        assertThat(result).isEqualTo("All deploy configurations saved to DB successfully!");
        verify(deployConfigRepository, times(1)).saveAll(configs);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenSaveAllConfigsFails() {
        // GIVEN
        List<RemoteSerwerConfiguration> configs = Arrays.asList(new RemoteSerwerConfiguration());
        when(deployConfigRepository.saveAll(any())).thenThrow(new RuntimeException("Database error"));

        // WHEN & THEN
        assertThatThrownBy(() -> remoteSerwerService.saveAllConfigsToDb(configs))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error saving deploy configurations to DB");
    }

    @Test
    void shouldUpdateConfigSuccessfully() {
        // GIVEN
        RemoteSerwerConfiguration config = new RemoteSerwerConfiguration(1L, "Server-1", "192.168.1.1", "admin", "new-pass");
        when(deployConfigRepository.save(config)).thenReturn(config);

        // WHEN
        RemoteSerwerConfiguration result = remoteSerwerService.updateConfig(config);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getPass()).isEqualTo("new-pass");
        verify(deployConfigRepository, times(1)).save(config);
    }

    @Test
    void shouldGetConfigByName() {
        // GIVEN
        String serverName = "Prod-Server";
        RemoteSerwerConfiguration expectedConfig = new RemoteSerwerConfiguration(10L, serverName, "10.0.0.1", "user", "secret");
        when(deployConfigRepository.findByName(serverName)).thenReturn(expectedConfig);

        // WHEN
        RemoteSerwerConfiguration result = remoteSerwerService.getConfigsByName(serverName);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(serverName);
        verify(deployConfigRepository, times(1)).findByName(serverName);
    }

    @Test
    void shouldDeleteConfigByName() {
        // GIVEN
        String serverName = "Dev-Server";
        doNothing().when(deployConfigRepository).deleteByName(serverName);

        // WHEN
        remoteSerwerService.deleteConfigsByName(serverName);

        // THEN
        verify(deployConfigRepository, times(1)).deleteByName(serverName);
    }

    @Test
    void shouldDeleteConfigById() {
        // GIVEN
        Long configId = 5L;
        doNothing().when(deployConfigRepository).deleteById(configId);

        // WHEN
        remoteSerwerService.deleteConfigById(configId);

        // THEN
        verify(deployConfigRepository, times(1)).deleteById(configId);
    }

    @Test
    void shouldDeleteAllConfigsFromDb() {
        // GIVEN
        doNothing().when(deployConfigRepository).deleteAll();

        // WHEN
        String result = remoteSerwerService.deleteAllConfigsFromDb();

        // THEN
        assertThat(result).isEqualTo("All deploy configurations cleared successfully!");
        verify(deployConfigRepository, times(1)).deleteAll();
    }

    @Test
    void shouldGetConfigByIdWhenConfigExists() {
        // GIVEN
        Long configId = 1L;
        RemoteSerwerConfiguration config = new RemoteSerwerConfiguration(configId, "Test", "127.0.0.1", "test", "test");
        when(deployConfigRepository.findById(configId)).thenReturn(Optional.of(config));

        // WHEN
        RemoteSerwerConfiguration result = remoteSerwerService.getConfigById(configId);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getIdConfiguration()).isEqualTo(configId);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenConfigByIdDoesNotExist() {
        // GIVEN
        Long nonExistingId = 999L;
        when(deployConfigRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThatThrownBy(() -> remoteSerwerService.getConfigById(nonExistingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nie znaleziono konfiguracji o podanym ID");
    }
}