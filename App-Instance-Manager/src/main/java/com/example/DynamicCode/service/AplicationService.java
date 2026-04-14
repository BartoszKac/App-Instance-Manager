package com.example.DynamicCode.service;

import com.example.DynamicCode.model.CodeRequest;
import com.example.DynamicCode.model.ProcesDefault;
import com.example.DynamicCode.service.language.LanguageHandler;
import com.example.DynamicCode.service.util.FilesSave;
import com.example.DynamicCode.service.util.ProcessMenager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
public class AplicationService {

    @Autowired private FilesSave files;
    @Autowired private SimpMessagingTemplate messagingTemplate;
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

            if (!folder.exists()) {
                return "[]";
            }

            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null) return "[]";

            List<String> fileNames = new ArrayList<>();
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    fileNames.add(file.getName());
                }
            }


            return fileNames.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    public int SavaCode(ArrayList<CodeRequest> codes) {
        if (codes == null || codes.isEmpty()) return -1;

        String mainClassName = codes.get(0).getName();
        String ext = codes.get(0).getExtension(); // Upewnij się, że CodeRequest ma to pole!
        ArrayList<String> allRelatedFiles = new ArrayList<>();

        try {
            Files.createDirectories(Paths.get(workingDir));
            for (CodeRequest req : codes) {
                String fileName = req.getName() + req.getExtension();
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
            sendToFrontend("BŁĄD: Nieobsługiwany język: " + extension);
            return;
        }

        new Thread(() -> {
            try {
                killExistingProcess();
                sendToFrontend("--- Start: " + name + extension + " ---");

                ProcesDefault procInfo = processMenager.getProcesDefaultByMainProcess(name);
                List<String> compileCmd = handler.getCompileCommand(name,
                        procInfo != null ? procInfo.getProcesses() : null);

                if (compileCmd != null) {
                    int exitCode = runProcess(compileCmd, "[KOMPILATOR]");
                    if (exitCode != 0) {
                        sendToFrontend("!!! BŁĄD KOMPILACJI !!!");
                        return;
                    }
                }

                // 2. URUCHOMIENIE
                List<String> runCmd = handler.getRunCommand(name);
                int exitCode = runProcess(runCmd, "");
                sendToFrontend("--- Zakończono (Kod: " + exitCode + ") ---");

            } catch (Exception e) {
                sendToFrontend("BŁĄD KRYTYCZNY: " + e.getMessage());
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
                System.out.println("PROCES LOG: " + line); // Log w IntelliJ
                sendToFrontend(prefix + (prefix.isEmpty() ? "" : " ") + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("Proces zakończony błędem. Kod: " + exitCode);
        }
        return exitCode;
    }

    private void killExistingProcess() throws InterruptedException {
        if (currentRunningProcess != null && currentRunningProcess.isAlive()) {
            sendToFrontend(">>> Zamykanie aktywnego procesu...");
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

    private void sendToFrontend(String message) {
        messagingTemplate.convertAndSend("/topic/output", message);
    }
}