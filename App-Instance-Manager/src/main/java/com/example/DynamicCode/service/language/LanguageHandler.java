package com.example.DynamicCode.service.language;
import java.util.List;

public interface LanguageHandler {
    String getExtension();
    List<String> getCompileCommand(String mainFileName, List<String> allFiles);
    List<String> getRunCommand(String mainFileName);
}