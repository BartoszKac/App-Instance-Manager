package com.example.DynamicCode.databaseservice.deploy;

import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.repository.deploy.RemoteSerwerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteSerwerService {

    private final RemoteSerwerRepository deployConfigRepository;

    @Transactional
    public String saveAllConfigsToDb(List<RemoteSerwerConfiguration> configsFromRequest) {
        try {
            log.info("Rozpoczynam zapis {} konfiguracji wdrożeniowych do bazy...", configsFromRequest.size());
            deployConfigRepository.saveAll(configsFromRequest);
            log.info("Konfiguracje zostały pomyślnie zapisane!");
            return "All deploy configurations saved to DB successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas zapisu konfiguracji do bazy. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving deploy configurations to DB: " + e.getMessage(), e);
        }
    }

    @Transactional
    public RemoteSerwerConfiguration updateConfig(RemoteSerwerConfiguration updatedConfig) {
        try {
            log.info("Aktualizuję konfigurację o ID: {}", updatedConfig.getIdConfiguration());
            return deployConfigRepository.save(updatedConfig);
        } catch (Exception e) {
            log.error("Błąd podczas aktualizacji konfiguracji o ID: {}. Powód: {}", updatedConfig.getIdConfiguration(), e.getMessage(), e);
            throw new RuntimeException("Nie udało się zaktualizować konfiguracji", e);
        }
    }

    @Transactional(readOnly = true)
    public RemoteSerwerConfiguration getConfigsByName(String name) {
        try {
            log.info("Pobieram konfiguracje dla nazwy: {}", name);
            return deployConfigRepository.findByName(name);
        } catch (Exception e) {
            log.error("Błąd podczas pobierania konfiguracji dla nazwy: {}. Powód: ", name, e);
            throw new RuntimeException("Nie udało się znalesc konfiguracji o takiej nazwie", e);
        }
    }

    @Transactional
    public void deleteConfigsByName(String name) {
        try {
            log.info("Usuwam konfiguracje dla nazwy: {}", name);
            deployConfigRepository.deleteByName(name);
            log.info("Pomyślnie usunięto konfiguracje dla nazwy: {}", name);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania konfiguracji dla nazwy: {}. Powód: {}", name, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć konfiguracji o podanej nazwie", e);
        }
    }

    @Transactional
    public void deleteConfigById(Long idConfiguration) {
        try {
            log.info("Usuwam pojedynczą konfigurację o ID: {}", idConfiguration);
            deployConfigRepository.deleteById(idConfiguration);
            log.info("Pomyślnie usunięto konfigurację o ID: {}", idConfiguration);
        } catch (Exception e) {
            log.error("Błąd podczas usuwania konfiguracji o ID: {}. Powód: {}", idConfiguration, e.getMessage(), e);
            throw new RuntimeException("Nie udało się usunąć konkretnej konfiguracji", e);
        }
    }

    @Transactional
    public String deleteAllConfigsFromDb() {
        try {
            log.warn("UWAGA: Rozpoczynam usuwanie WSZYSTKICH konfiguracji z bazy danych!");
            deployConfigRepository.deleteAll();
            log.info("Baza danych konfiguracji została całkowicie wyczyszczona.");
            return "All deploy configurations cleared successfully!";
        } catch (Exception e) {
            log.error("Błąd podczas czyszczenia bazy danych konfiguracji. Powód: {}", e.getMessage(), e);
            throw new RuntimeException("Nie udało się wyczyścić bazy danych konfiguracji", e);
        }
    }

    @Transactional(readOnly = true)
    public RemoteSerwerConfiguration getConfigById(Long idConfiguration) {
        try {
            log.info("Pobieram konfigurację o idConfiguration: {}", idConfiguration);
            return deployConfigRepository.findById(idConfiguration)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono konfiguracji o podanym ID: " + idConfiguration));
        } catch (IllegalArgumentException e) {
            log.warn("Brak rekordu w bazie: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Błąd podczas pobierania konfiguracji o idConfiguration: {}. Powód: ", idConfiguration, e);
            throw new RuntimeException("Nie udało się pobrać konfiguracji z bazy danych", e);
        }
    }
}