package com.example.DynamicCode.service.deploymentconfig;

import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeployConfigService {

    private final File configFile = new File("deploy-config.json");
    private final ObjectMapper objectMapper = new ObjectMapper();



    // POPRAWKA: upsert zamiast zawsze dodawać nowy wpis (poprzedni kod duplikował konfiguracje)
    public String saveNewConfig(RemoteSerwerConfiguration config) {
        if (config.getName() == null || config.getName().isBlank()) {
            return "Błąd: pole 'name' jest wymagane.";
        }
        if (config.getIp() == null || config.getUser() == null || config.getPass() == null) {
            return "Błąd: pola ip, user i pass są wymagane.";
        }

        try {
            List<RemoteSerwerConfiguration> configList = getAllConfigs();
            // Zastąp istniejącą o tej samej nazwie lub dodaj nową
            configList.removeIf(c -> config.getName().equals(c.getName()));
            configList.add(config);
            objectMapper.writeValue(configFile, configList);
        } catch (Exception e) {
            e.printStackTrace();
            return "Błąd zapisu konfiguracji: " + e.getMessage();
        }

        return "Konfiguracja '" + config.getName() + "' zapisana pomyślnie.";
    }

    public List<RemoteSerwerConfiguration> getAllConfigs() {
        try {
            if (configFile.exists() && configFile.length() > 0) {
                return objectMapper.readValue(configFile, new TypeReference<List<RemoteSerwerConfiguration>>() {});
            }
        } catch (IOException e) {
            System.err.println("Błąd odczytu konfiguracji: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    // POPRAWKA: szukaj po name, nie po ip (poprzedni kod był błędny)
    public RemoteSerwerConfiguration getConfigByName(String name) {
        return getAllConfigs().stream()
                .filter(config -> name.equals(config.getName()))
                .findFirst()
                .orElse(null);
    }
}
