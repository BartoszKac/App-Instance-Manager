package com.example.DynamicCode.service.language;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PythonHandler implements LanguageHandler {

    @Override
    public String getExtension() { return ".py"; }

    @Override
    public List<String> getCompileCommand(String mainFileName, List<String> allFiles) {
        // Python nie wymaga kompilacji - zwracamy null
        // Pozostałe pliki .py są automatycznie dostępne przez import jeśli są w tym samym folderze
        return null;
    }

    @Override
    public List<String> getRunCommand(String mainFileName) {
        // Próbujemy najpierw 'python3', fallback do 'python'
        String python = System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";
        return List.of(python, mainFileName + getExtension());
    }
}