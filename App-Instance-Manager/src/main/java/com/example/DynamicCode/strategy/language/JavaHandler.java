package com.example.DynamicCode.strategy.language;

import org.springframework.stereotype.Component;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.List;

@Component
public class JavaHandler implements LanguageHandler {

    @Override
    public String getExtension() { return ".java"; }

    @Override
    public List<String> getCompileCommand(String mainFileName, List<String> allFiles) {
        List<String> cmd = new ArrayList<>(List.of("javac", "--release", "17", "-d", "."));

        if (allFiles != null && !allFiles.isEmpty()) {
            for (String file : allFiles) {
                if (file.endsWith(".java")) {
                    cmd.add(file);
                }
            }
        } else {
            cmd.add(mainFileName + ".java");
        }

        System.out.println("Java compile cmd: " + cmd);
        return cmd;
    }

    @Override
    public List<String> getRunCommand(String mainFileName) {
        return List.of("java", "-cp", ".", mainFileName);
    }

}