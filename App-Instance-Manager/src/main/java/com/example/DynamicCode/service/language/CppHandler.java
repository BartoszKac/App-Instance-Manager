package com.example.DynamicCode.service.language;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class CppHandler implements LanguageHandler {

    @Override
    public String getExtension() { return ".cpp"; }

    @Override
    public List<String> getCompileCommand(String mainFileName, List<String> allFiles) {
        // Szukamy g++ w PATH (działa na Linux i Windows z MSYS2/MinGW w PATH)
        // Możesz też ustawić zmienną środowiskową GPP_PATH jeśli g++ nie jest w PATH
        String gpp = System.getenv("GPP_PATH") != null ? System.getenv("GPP_PATH") : "g++";

        String outputFile = mainFileName + (isWindows() ? ".exe" : "");

        List<String> cmd = new ArrayList<>();
        cmd.add(gpp);

        if (allFiles != null && !allFiles.isEmpty()) {
            // Kompilujemy WSZYSTKIE pliki .cpp naraz
            for (String file : allFiles) {
                if (file.endsWith(".cpp")) {
                    cmd.add(file);
                }
            }
        } else {
            cmd.add(mainFileName + ".cpp");
        }

        cmd.add("-o");
        cmd.add(outputFile);
        cmd.add("-Wall");

        System.out.println("C++ compile cmd: " + cmd);
        return cmd;
    }

    @Override
    public List<String> getRunCommand(String mainFileName) {
        String exe = mainFileName + (isWindows() ? ".exe" : "");
        // Na Linuxie trzeba poprzedzić './' żeby uruchomić plik z bieżącego katalogu
        return isWindows() ? List.of(exe) : List.of("./" + exe);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}