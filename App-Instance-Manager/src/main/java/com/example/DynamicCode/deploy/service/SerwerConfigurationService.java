package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.ServerCredentialsRequest;
import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.OperationSystem;
import com.example.DynamicCode.deploy.exception.InvalidServerCredentialsException;
import com.example.DynamicCode.shared.event.ServerCredentialsSavedEvent;
import com.example.DynamicCode.shared.event.ServerRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SerwerConfigurationService {

    private static final Map<String, OperationSystem> OS_BY_NAME_FRAGMENT = Map.of(
            "linux", OperationSystem.LINUX,
            "darwin", OperationSystem.MAC,
            "mac", OperationSystem.MAC,
            "windows", OperationSystem.WINDOWS);

    private final RemoteSerwerService remoteSerwerService;
    private final SshSessionFactory sshSessionFactory;
    private final ApplicationEventPublisher eventPublisher;

    public RemoteSerwerConfiguration registerAndFetchDetails(RemoteSerwerConfiguration config) {
        log.info("Rozpoczynam automatyczne pobieranie parametrów dla serwera: {}", config.getIp());
        config.setOperationSystem(detectOperationSystem(config));
        RemoteSerwerConfiguration saved = remoteSerwerService.updateConfig(config);
        publishCredentialsSaved(saved);
        return saved;
    }

    public void registerConfigurations(List<RemoteSerwerConfiguration> configs) {
        log.info("Inicjowanie procesu masowej rejestracji {} konfiguracji serwerów.", configs.size());
        remoteSerwerService.saveAllConfigsToDb(configs);
        configs.forEach(this::publishCredentialsSaved);
    }

    public RemoteSerwerConfiguration getConfiguration(Long idConfiguration) {
        log.info("Żądanie pobrania konfiguracji serwera o ID: {}", idConfiguration);
        return remoteSerwerService.getConfigById(idConfiguration);
    }

    public List<RemoteSerwerConfiguration> getAllConfigurations() {
        log.info("Żądanie pobrania wszystkich konfiguracji serwerów z bazy.");
        return remoteSerwerService.getAllConfigs();
    }

    public RemoteSerwerConfiguration updateConfiguration(RemoteSerwerConfiguration updatedConfig) {
        log.info("Inicjowanie aktualizacji konfiguracji serwera o ID: {}", updatedConfig.getIdConfiguration());
        RemoteSerwerConfiguration saved = remoteSerwerService.updateConfig(updatedConfig);
        publishCredentialsSaved(saved);
        return saved;
    }

    public void updateCredentials(Long idConfiguration, ServerCredentialsRequest request) {
        if (isBlank(request.user()) || request.password() == null || request.password().isEmpty()) {
            throw new InvalidServerCredentialsException("Podaj użytkownika i hasło SSH.");
        }
        RemoteSerwerConfiguration config = remoteSerwerService.getConfigById(idConfiguration);
        config.setUser(request.user().trim());
        config.setPass(request.password());
        updateConfiguration(config);
    }

    public void removeConfiguration(Long idConfiguration) {
        log.info("Żądanie usunięcia konfiguracji serwera o ID: {}", idConfiguration);
        remoteSerwerService.deleteConfigById(idConfiguration);
        eventPublisher.publishEvent(new ServerRemovedEvent(idConfiguration));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private OperationSystem detectOperationSystem(RemoteSerwerConfiguration config) {
        try {
            String osName = sshSessionFactory.executeCommand(config.getIp(), config.getUser(), config.getPass(), "uname");
            OperationSystem detected = resolveOperationSystem(osName);
            log.info("Pomyślnie rozpoznano system operacyjny: {} dla serwera {}", detected, config.getIp());
            return detected;
        } catch (Exception e) {
            log.warn("Nie udało się pobrać szczegółów przez SSH. Fallback na LINUX. Powód: {}", e.getMessage());
            return OperationSystem.LINUX;
        }
    }

    private OperationSystem resolveOperationSystem(String osName) {
        String normalized = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
        return OS_BY_NAME_FRAGMENT.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(OperationSystem.LINUX);
    }

    private void publishCredentialsSaved(RemoteSerwerConfiguration config) {
        boolean hasPassword = config.getPass() != null && !config.getPass().isEmpty();
        if (hasPassword && config.getIdConfiguration() != null) {
            eventPublisher.publishEvent(new ServerCredentialsSavedEvent(config.getIdConfiguration()));
        }
    }
}
