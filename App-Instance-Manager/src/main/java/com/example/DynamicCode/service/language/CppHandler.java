package com.example.DynamicCode.service.language;

import org.springframework.stereotype.Component;
import java.io.File;
import java.util.List;

@Component
public class CppHandler implements LanguageHandler {

    @Override
    public String getExtension() {
        return ".cpp";
    }

    @Override
    public List<String> getCompileCommand(String mainFileName, List<String> allFiles) {
        System.out.println("--- DIAGNOSTYKA ŚCIEŻEK C++ ---");

        // Pobieramy ścieżkę do folderu 'app'
        File currentDir = new File("app");
        System.out.println("Szukam plików w: " + currentDir.getAbsolutePath());

        // Wypisujemy zawartość folderu do konsoli IDE
        String[] files = currentDir.list();
        if (files != null && files.length > 0) {
            System.out.println("Pliki znalezione w folderze app:");
            for (String f : files) {
                System.out.println("  -> " + f);
            }
        } else {
            System.out.println("!!! UWAGA: Folder 'app' jest pustY lub nie istnieje !!!");
        }

        String gppExecutable = "C:\\msys64\\ucrt64\\bin\\g++.exe";

        String sourceFile = mainFileName + ".cpp";
        String outputFile = mainFileName + ".exe";

        System.out.println("Próba kompilacji: " + sourceFile + " do " + outputFile);


        return List.of(gppExecutable, sourceFile, "-o", outputFile, "-Wall");
    }

    @Override
    public List<String> getRunCommand(String mainFileName) {

        return List.of(mainFileName + ".exe");
    }
}