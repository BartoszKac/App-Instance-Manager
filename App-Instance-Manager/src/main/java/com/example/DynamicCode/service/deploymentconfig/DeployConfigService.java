package com.example.DynamicCode.service.deploymentconfig;


import com.example.DynamicCode.model.DeployConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeployConfigService {

    private final File configFile = new File("deploy-config.json");
    private final ObjectMapper objectMapper = new ObjectMapper();


    public String saveNewConfig(DeployConfig config) {
        if (config.getIp() == null || config.getUser() == null || config.getPass() == null) {
            return "Invalid configuration: IP, user, and pass are required.";
        }

        try {
            List<DeployConfig> configList;

            if (configFile.exists() && configFile.length() > 0) {
                configList = objectMapper.readValue(configFile, new TypeReference<List<DeployConfig>>() {
                });
            } else {
                configList = new ArrayList<>();
            }

            configList.add(config);

            objectMapper.writeValue(configFile, configList);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error saving configuration: " + e.getMessage();
        }

        return "Deployment configuration saved successfully!";
    }

    public List<DeployConfig> getAllConfigs() {
        try {
            if (configFile.exists() && configFile.length() > 0) {
                return objectMapper.readValue(configFile, new TypeReference<List<DeployConfig>>() {
                });
            }
        } catch (IOException e) {
            System.err.println("Błąd odczytu konfiguracji: " + e.getMessage());
        }
        // Zwraca pustą listę w razie braku pliku lub błędu
        return new ArrayList<>();
    }

    public DeployConfig getConfigByName(String ip) {
        return getAllConfigs().stream()
                .filter(config -> config.getIp().equals(ip))
                .findFirst()
                .orElse(null);
    }
}
