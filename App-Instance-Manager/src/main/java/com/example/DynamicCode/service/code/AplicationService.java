package com.example.DynamicCode.service.code;

import com.example.DynamicCode.model.dto.proces.ProcesDefault;
import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.notification.FrontendNotificationService;
import com.example.DynamicCode.strategy.language.LanguageHandler;
import com.example.DynamicCode.file.FilesSave;
import com.example.DynamicCode.service.util.ProcessMenager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class AplicationService {

    @Autowired private FilesSave files;
    @Autowired private FrontendNotificationService frontendNotificationService;
    @Autowired private ProcessMenager processMenager;



    private final Map<String, LanguageHandler> handlers = new HashMap<>();
    private final String workingDir = System.getProperty("user.dir") + File.separator + "app";
    private Process currentRunningProcess = null;

    @Autowired
    public AplicationService(List<LanguageHandler> handlerList) {
        for (LanguageHandler handler : handlerList) {
            handlers.put(handler.getExtension(), handler);
        }
    }

    public String GetInfo() {
        try {
            File folder = new File(workingDir);
            if (!folder.exists()) return "[]";

            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null) return "[]";

            List<String> fileNames = new ArrayList<>();
            for (File file : listOfFiles) {
                if (file.isFile()) fileNames.add(file.getName());
            }
            return fileNames.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public int SavaCode(ArrayList<SourceCode> codes) {

        if (codes == null || codes.isEmpty()) return -1;

        String mainClassName = codes.get(0).getName();
        ArrayList<String> allRelatedFiles = new ArrayList<>();

        try {
            Files.createDirectories(Paths.get(workingDir));
            for (SourceCode req : codes) {
                String fileName = req.getName() + req.getLanguage().getExtension();
                allRelatedFiles.add(fileName);
                Files.write(Paths.get(workingDir, fileName), req.getCode().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        processMenager.addProcesDefault(new ProcesDefault(allRelatedFiles, mainClassName));
        files.writeString(mainClassName);
        return 0;
    }

    public void CompileandRun(String name, String extension) {
        LanguageHandler handler = handlers.get(extension);
        if (handler == null) {
            frontendNotificationService.sendToFrontend("BŁĄD: Nieobsługiwany język: " + extension);
            return;
        }

        new Thread(() -> {
            try {
                killExistingProcess();
                frontendNotificationService.sendToFrontend("--- Start: " + name + extension + " ---");

                ProcesDefault procInfo = processMenager.getProcesDefaultByMainProcess(name);
                List<String> compileCmd = handler.getCompileCommand(name,
                        procInfo != null ? procInfo.getProcesses() : null);

                if (compileCmd != null) {
                    int exitCode = runProcess(compileCmd, "[KOMPILATOR]");
                    if (exitCode != 0) {
                        frontendNotificationService.sendToFrontend("!!! BŁĄD KOMPILACJI !!!");
                        return;
                    }
                }

                List<String> runCmd = handler.getRunCommand(name);
                int exitCode = runProcess(runCmd, "");
                frontendNotificationService.sendToFrontend("--- Zakończono (Kod: " + exitCode + ") ---");

            } catch (Exception e) {
                frontendNotificationService.sendToFrontend("BŁĄD KRYTYCZNY: " + e.getMessage());
            }
        }).start();
    }

    private int runProcess(List<String> command, String prefix) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(workingDir));
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

    private void killExistingProcess() throws InterruptedException {
        if (currentRunningProcess != null && currentRunningProcess.isAlive()) {
            frontendNotificationService.sendToFrontend(">>> Zamykanie aktywnego procesu...");
            currentRunningProcess.destroyForcibly();
            currentRunningProcess.waitFor();
        }
    }

    public String Delete(String name, String extension) {
        try {
            Files.deleteIfExists(Paths.get(workingDir, name + extension));
            Files.deleteIfExists(Paths.get(workingDir, name + ".class"));
            Files.deleteIfExists(Paths.get(workingDir, name + ".exe"));
            files.deleteString(name);
            return "Usunięto pomyślnie.";
        } catch (IOException e) {
            return "Błąd usuwania: " + e.getMessage();
        }
    }





}