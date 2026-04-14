package com.example.DynamicCode.service.language;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PythonHandler implements LanguageHandler {
    @Override public String getExtension() { return ".py"; }

    @Override
    public List<String> getCompileCommand(String mainFileName, List<String> allFiles) {
        return null;
    }

    @Override
    public List<String> getRunCommand(String mainFileName) {
        return List.of("python", mainFileName + getExtension());
    }
}