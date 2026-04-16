package com.example.DynamicCode.service.util;

import com.example.DynamicCode.model.CodeRequest;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class WorkspaceManager {

    // Używamy prostej ścieżki - stworzy folder "app" tam gdzie jest JAR
    private final String workingDir = "app";

    public boolean saveFiles(List<CodeRequest> codes) {
        if (codes == null || codes.isEmpty()) {
            System.out.println("DEBUG: Brak danych do zapisu!");
            return false;
        }

        try {
            Path path = Paths.get(workingDir);

            // 1. Upewnij się, że katalog istnieje
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("DEBUG: Utworzono katalog: " + path.toAbsolutePath());
            }

            // 2. Zapisz każdy plik
            for (CodeRequest req : codes) {
                if (req.getName() == null || req.getCode() == null) continue;

                String fileName = req.getName() + req.getExtension();
                Path filePath = path.resolve(fileName);

                // Zapisujemy dane (getBytes wymusza standardowe kodowanie UTF-8)
                Files.write(filePath, req.getCode().getBytes());
                System.out.println("DEBUG: Plik zapisany pomyślnie: " + filePath.toAbsolutePath());
            }
            return true;

        } catch (IOException e) {
            System.err.println("!!! BŁĄD KRYTYCZNY ZAPISU NA LINUXIE !!!");
            e.printStackTrace();
            return false;
        }
    }
}