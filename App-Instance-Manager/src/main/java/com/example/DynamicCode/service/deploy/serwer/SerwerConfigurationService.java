package com.example.DynamicCode.service.deploy.serwer;

import com.example.DynamicCode.constants.deploy.OperationSystem;
import com.example.DynamicCode.databaseservice.deploy.RemoteSerwerService;
import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.service.deploy.connection.SshSessionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SerwerConfigurationService {

    private final RemoteSerwerService remoteSerwerService;
    private final SshSessionFactory sshSessionFactory;

    /**
     * Rejestruje serwer w bazie danych oraz automatycznie pobiera z niego szczegóły (OS) przez SSH.
     */
    public RemoteSerwerConfiguration registerAndFetchDetails(RemoteSerwerConfiguration config) {
        log.info("Rozpoczynam automatyczne pobieranie parametrów dla serwera: {}", config.getIp());

        try {
            String osName = sshSessionFactory.executeCommand(
                    config.getIp(),
                    config.getUser(),
                    config.getPass(),
                    "uname"
            );

            OperationSystem detectedOs = resolveOperationSystem(osName);
            config.setOperationSystem(detectedOs);

            log.info("Pomyślnie rozpoznano system operacyjny: {} dla serwera {}", detectedOs, config.getIp());
        } catch (Exception e) {
            log.warn("Nie udało się pobrać szczegółów przez SSH. Fallback na LINUX. Powód: {}", e.getMessage());
            config.setOperationSystem(OperationSystem.LINUX);
        }

        return remoteSerwerService.updateConfig(config);
    }

    /**
     * Tradycyjna, masowa rejestracja konfiguracji bezpośrednio do bazy.
     */
    public void registerConfigurations(List<RemoteSerwerConfiguration> configs) {
        log.info("Inicjowanie procesu masowej rejestracji {} konfiguracji serwerów.", configs.size());
        remoteSerwerService.saveAllConfigsToDb(configs);
    }

    /**
     * Pobiera konfigurację konkretnego serwera na podstawie ID.
     */
    public RemoteSerwerConfiguration getConfiguration(Long idConfiguration) {
        log.info("Żądanie pobrania konfiguracji serwera o ID: {}", idConfiguration);
        return remoteSerwerService.getConfigById(idConfiguration);
    }

    /**
     * Pobiera absolutnie wszystkie zarejestrowane serwery z bazy danych.
     * Metoda używana do przekazania danych dalej do Providera i Kontrolera.
     */
    public List<RemoteSerwerConfiguration> getAllConfigurations() {
        log.info("Żądanie pobrania wszystkich konfiguracji serwerów z bazy.");
        // Zakładam, że remoteSerwerService posiada metodę do pobrania całej listy (np. opartą o findAll())
        return remoteSerwerService.getAllConfigs();
    }

    /**
     * Aktualizuje konfigurację istniejącego serwera.
     */
    public RemoteSerwerConfiguration updateConfiguration(RemoteSerwerConfiguration updatedConfig) {
        log.info("Inicjowanie aktualizacji konfiguracji serwera o ID: {}", updatedConfig.getIdConfiguration());
        return remoteSerwerService.updateConfig(updatedConfig);
    }

    /**
     * Usuwa konfigurację serwera z systemu.
     */
    public void removeConfiguration(Long idConfiguration) {
        log.info("Żądanie usunięcia konfiguracji serwera o ID: {}", idConfiguration);
        remoteSerwerService.deleteConfigById(idConfiguration);
    }

    /**
     * Prywatna metoda tłumacząca odpowiedź z terminala SSH na obiekt Enum systemu.
     */
    private OperationSystem resolveOperationSystem(String osName) {
        if (osName == null || osName.isEmpty()) {
            return OperationSystem.LINUX;
        }
        String normalized = osName.toLowerCase();
        if (normalized.contains("linux")) return OperationSystem.LINUX;
        if (normalized.contains("darwin") || normalized.contains("mac")) return OperationSystem.MAC;
        if (normalized.contains("windows")) return OperationSystem.WINDOWS;

        return OperationSystem.LINUX;
    }
}