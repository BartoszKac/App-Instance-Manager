package com.example.DynamicCode.service.deploy.storage;

import com.example.DynamicCode.constants.deploy.OperationSystem;
import org.springframework.stereotype.Service;

@Service
public class RemoteDirectoryService {

    public String getRemoteDirectoryPath(OperationSystem operationSystem) {
        if (operationSystem == null) {
            return "/remote/directory/linux/serwer_";
        }

        return switch (operationSystem) {
            case WINDOWS -> ""; // SFTP domyślnie wrzuci do katalogu użytkownika na Windowsie
            case LINUX -> "";   // SFTP domyślnie wrzuci do /home/nazwa_uzytkownika/
            case MAC -> "";     // SFTP domyślnie wrzuci do /Users/nazwa_uzytkownika/
        };
    }
}