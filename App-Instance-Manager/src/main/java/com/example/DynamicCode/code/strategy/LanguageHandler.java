package com.example.DynamicCode.code.strategy;
import java.util.List;

public interface LanguageHandler {
    String getExtension();
    List<String> getCompileCommand(String mainFileName, List<String> allFiles);
    List<String> getRunCommand(String mainFileName);
}