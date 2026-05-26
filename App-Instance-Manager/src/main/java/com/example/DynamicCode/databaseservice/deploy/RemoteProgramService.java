package com.example.DynamicCode.databaseservice.deploy;




import com.example.DynamicCode.model.entity.deploy.RemoteProgramConfiguration;
import com.example.DynamicCode.repository.deploy.RemoteProgramRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class RemoteProgramService {

    @Autowired
    private RemoteProgramRepository repository;

    @Transactional
    public String saveAllConfigurationsToDb(List<RemoteProgramConfiguration> configsFromRequest) {
        try {
            log.info("Rozpoczynam zapis {} konfiguracji programów do bazy...", configsFromRequest.size());
            repository.saveAll(configsFromRequest);
            log.info("Konfiguracje zostały pomyślnie zapisane!");
            return "All configurations saved to DB successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas zapisu konfiguracji do bazy. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving configurations to DB: " + e.getMessage(), e);
        }
    }

    @Transactional
    public RemoteProgramConfiguration updateConfiguration(RemoteProgramConfiguration updatedConfig) {
        try {
            log.info("Aktualizuję konfigurację o ID: {}", updatedConfig.getIdConfiguration());
            return repository.save(updatedConfig);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji konfiguracji o ID: {}. Powód: {}", updatedConfig.getIdConfiguration(), e.getMessage(), e);
            throw new RuntimeException("Nie udało się zaktualizować konfiguracji", e);
        }
    }

    @Transactional(readOnly = true)
    public List<RemoteProgramConfiguration> getAllConfigurationsByIdSerwer(Long idSerwer) {
        try {
            log.info("Pobieram WSZYSTKIE konfiguracje dla idSerwer: {}", idSerwer);
            return repository.findByIdSerwer(idSerwer); // Metoda w repo musi się nazywać: findByIdSerwer(Long idSerwer)
        } catch (Exception e) {
            log.error("Błąd podczas pobierania wszystkich konfiguracji dla idSerwer: {}. Powód: ", idSerwer, e);
            return Collections.emptyList();
        }
    }

    @Transactional
    public void deleteAllConfigurationsByIdSerwer(Long idSerwer) {
        try {
            log.info("Usuwam wszystkie konfiguracje dla idSerwer: {}", idSerwer);
            repository.deleteByIdSerwer(idSerwer); // Metoda w repo: deleteByIdSerwer(Long idSerwer)
            log.info("Pomyślnie usunięto konfiguracje dla idSerwer: {}", idSerwer);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania konfiguracji dla idSerwer: {}. Powód: {}", idSerwer, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć konfiguracji dla serwera", e);
        }
    }

    @Transactional
    public void deleteConfigurationById(Long idConfiguration) {
        try {
            log.info("Usuwam pojedynczą konfigurację o ID: {}", idConfiguration);
            repository.deleteById(idConfiguration);
            log.info("Pomyślnie usunięto konfigurację o ID: {}", idConfiguration);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania konfiguracji o ID: {}. Powód: {}", idConfiguration, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć konkretnej konfiguracji", e);
        }
    }

    @Transactional
    public String deleteAllConfigurationsFromDb() {
        try {
            log.warn("UWAGA: Rozpoczynam usuwanie WSZYSTKICH konfiguracji z bazy danych!");
            repository.deleteAll();
            log.info("Baza danych z konfiguracjami została całkowicie wyczyszczona.");
            return "All configurations cleared successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas czyszczenia bazy danych z konfiguracji. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się wyczyścić bazy danych z konfiguracji", e);
        }
    }

    @Transactional(readOnly = true)
    public RemoteProgramConfiguration getConfigurationById(Long idConfiguration) {
        try {
            log.info("Pobieram konfigurację o ID: {}", idConfiguration);
            return repository.findById(idConfiguration)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono konfiguracji o podanym ID: " + idConfiguration));

        } catch (IllegalArgumentException e) {
            log.warn("Brak rekordu w bazie: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania konfiguracji o ID: {}. Powód: ", idConfiguration, e);
            throw new RuntimeException("Nie udało się pobrać konfiguracji z bazy danych", e);
        }
    }

    @Transactional(readOnly = true)
    public List<RemoteProgramConfiguration> getAllConfigurations() {
        log.info("[SERWIS] Pobieram absolutnie wszystkie konfiguracje z bazy danych.");
        try {
            List<RemoteProgramConfiguration> allConfigs = repository.findAll();
            log.info("[SERWIS] Pomyślnie pobrano wszystkie konfiguracje. Łączna liczba: {}", allConfigs.size());
            return allConfigs;
        } catch (Exception e) {
            log.error("[SERWIS] Błąd podczas pobierania wszystkich konfiguracji z bazy: ", e);
            return Collections.emptyList();
        }
    }
}