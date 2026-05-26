package com.example.DynamicCode.service.prcoces;

import com.example.DynamicCode.constants.code.CompileConstats;
import com.example.DynamicCode.notification.FrontendNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class LanguageProces {

    @Autowired
    private FrontendNotificationService frontendNotificationService;


    private Process currentRunningProcess = null;

    public int runProcess(List<String> command, String prefix,String folder) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(CompileConstats.WORKING_DIR + "/" + folder));
        pb.redirectErrorStream(true);

        Process process = pb.start();
        currentRunningProcess = process;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("PROCES LOG: " + line);
                frontendNotificationService.sendToFrontend(prefix + (prefix.isEmpty() ? "" : " ") + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) System.out.println("Proces zakończony błędem. Kod: " + exitCode);
        return exitCode;
    }


}
