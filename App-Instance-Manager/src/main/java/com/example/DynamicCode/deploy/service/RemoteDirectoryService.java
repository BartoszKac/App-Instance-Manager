package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.enums.OperationSystem;
import org.springframework.stereotype.Service;

@Service
public class RemoteDirectoryService {

    public String getRemoteDirectoryPath(OperationSystem operationSystem) {
        if (operationSystem == null) {
            return "/remote/directory/linux/serwer_";
        }

        return switch (operationSystem) {
            case WINDOWS -> "";
            case LINUX -> "test/"; // Pełna, bezwzględna ścieżka
            case MAC -> "";
        };
    }
}