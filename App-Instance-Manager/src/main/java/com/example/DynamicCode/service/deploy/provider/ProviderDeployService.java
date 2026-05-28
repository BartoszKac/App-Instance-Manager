package com.example.DynamicCode.service.deploy.provider;

import com.example.DynamicCode.model.dto.deploy.TransferTask;
import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.service.deploy.serwer.SerwerConfigurationService;
import com.example.DynamicCode.service.deploy.transport.EnviromentTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderDeployService {

    private final SerwerConfigurationService serwerConfigurationService;
    private final EnviromentTransferService enviromentTransferService;

    // --- SEKCJA ZARZĄDZANIA SERWERAMI (KONFIGURACJA) ---

    /**
     * Rejestruje nowy serwer w bazie i automatycznie sprawdza jego OS przez SSH.
     * Idealne pod endpoint POST z front-endu podczas dodawania serwera.
     */
    public RemoteSerwerConfiguration registerNewServerAndFetchDetails(RemoteSerwerConfiguration config) {
        log.info("[Provider-Deploy] Rejestracja i automatyczna inspekcja nowego serwera: {}", config.getIp());
        return serwerConfigurationService.registerAndFetchDetails(config);
    }

    /**
     * Masowa rejestracja maszyn (gdyby front-end wysyłał gotową listę konfiguracji).
     */
    public void registerBulkServerConfigurations(List<RemoteSerwerConfiguration> configs) {
        log.info("[Provider-Deploy] Masowa rejestracja {} konfiguracji serwerów.", configs.size());
        serwerConfigurationService.registerConfigurations(configs);
    }

    /**
     * Pobiera szczegóły konfiguracji danego serwera po jego ID.
     */
    public RemoteSerwerConfiguration getServerConfiguration(Long idConfiguration) {
        log.info("[Provider-Deploy] Pobieranie danych serwera o ID: {}", idConfiguration);
        return serwerConfigurationService.getConfiguration(idConfiguration);
    }

    /**
     * Pobiera wszystkie zarejestrowane serwery.
     * Używane przez front-end do wylistowania dostępnych maszyn w tabeli/drop-downie.
     */
    public List<RemoteSerwerConfiguration> getAllServerConfigurations() {
        log.info("[Provider-Deploy] Pobieranie wszystkich konfiguracji serwerów z bazy danych.");
        return serwerConfigurationService.getAllConfigurations(); // Upewnij się, że masz tę metodę w serwisie niżej!
    }

    /**
     * Aktualizuje parametry serwera (np. zmiana hasła, IP czy użytkownika).
     */
    public RemoteSerwerConfiguration updateServerConfiguration(RemoteSerwerConfiguration updatedConfig) {
        log.info("[Provider-Deploy] Aktualizacja konfiguracji serwera o ID: {}", updatedConfig.getIdConfiguration());
        return serwerConfigurationService.updateConfiguration(updatedConfig);
    }

    /**
     * Usuwa konfigurację wybranego serwera z systemu.
     */
    public void removeServerConfiguration(Long idConfiguration) {
        log.warn("[Provider-Deploy] Usuwanie konfiguracji serwera o ID: {}", idConfiguration);
        serwerConfigurationService.removeConfiguration(idConfiguration);
    }

    // --- SEKCJA TRANSFERU I DEPLOYMENTU ŚRODOWISK ---

    /**
     * Przyjmuje listę zadań wdrożeniowych i uruchamia proces transferu plików na serwery.
     * Front-end wywoła to, kiedy użytkownik kliknie np. "Wdróż wybrane projekty".
     */
    public void executeEnvironmentUploads(List<TransferTask> transferTaskList) {
        log.info("[Provider-Deploy] Inicjalizacja masowego transferu środowisk. Liczba zadań: {}", transferTaskList.size());
        enviromentTransferService.initiateUpload(transferTaskList);
    }

    /**
     * Pozwala na uruchomienie pojedynczego, natychmiastowego transferu dla konkretnego kodu i serwera.
     */
    public void executeSingleEnvironmentTransfer(Long mainClassId, Long idConfiguration, com.example.DynamicCode.constants.deploy.UploadStrategyType uploadStrategyType) {
        log.info("[Provider-Deploy] Ręczne wywołanie transferu dla klasy: {}, konfiguracji serwera: {}", mainClassId, idConfiguration);
        enviromentTransferService.transferEnvironment(mainClassId, idConfiguration, uploadStrategyType);
    }
}