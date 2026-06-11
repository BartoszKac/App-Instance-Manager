package com.example.DynamicCode.strategy.language;



import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BashHandler implements LanguageHandler {

    @Override
    public String getExtension() {
        return ".sh";
    }

    @Override
    public List<String> getCompileCommand(String mainFileName, List<String> allFiles) {
        // Bash jest językiem skryptowym i nie wymaga kompilacji
        return null;
    }

    @Override
    public List<String> getRunCommand(String mainFileName) {
        // Uruchamiamy skrypt bezpośrednio przez interpreter bash
        return List.of("bash", mainFileName + getExtension());
    }
}