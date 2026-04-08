package com.example.DynamicCode.service;

import com.example.DynamicCode.model.ProcesDefault;
import com.example.DynamicCode.service.util.FilesSave;
import com.example.DynamicCode.model.CodeRequest;
import com.example.DynamicCode.service.util.ProcessMenager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class AplicationService {

    @Autowired
    FilesSave files;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Wstrzykujemy manager procesów
    @Autowired
    private ProcessMenager processMenager;

    private final String workingDir = System.getProperty("user.dir") + File.separator + "app";

    private Process currentRunningProcess = null;

    public int SavaCode(ArrayList<CodeRequest> codes) {
        if (codes == null || codes.isEmpty()) return -1;

        // Tworzymy nową strukturę procesu dla managera
        // Zakładamy, że pierwszy plik na liście to główny proces (main)
        String mainClassName = codes.get(0).getName();
        ArrayList<String> allRelatedFiles = new ArrayList<>();

        codes.forEach(codeRequest -> {
            String fileName = codeRequest.getName() + ".java";
            allRelatedFiles.add(fileName);
            Path path = Paths.get(workingDir, fileName);

            try {
                // Usuwamy stary plik jeśli istnieje
                Files.deleteIfExists(path);
                // Zapisujemy nowy kod
                Files.write(path, codeRequest.getCode().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Rejestrujemy proces w managerze
        // Używamy refleksji/dostępności do klasy wewnętrznej (uważaj na widoczność klasy ProcesDefault)
        // Jeśli ProcesDefault nie jest publiczny, przenieś go do osobnego pliku lub zmień na public
        ProcesDefault newProcess = new ProcesDefault(allRelatedFiles, mainClassName);
        processMenager.addProcesDefault(newProcess);

        if(Files.exists(Paths.get(workingDir, mainClassName + ".java"))) {
            files.writeString(mainClassName);
        }
        return 0;
    }

    public String GetInfo() {
        return files.toString();
    }

    public String Delete(String name) {
        processMenager.getProcesDefaultByMainProcess(name).getProcesses().forEach(file -> {
            try {
                Files.deleteIfExists(Paths.get(workingDir, file));
                Files.deleteIfExists(Paths.get(workingDir, file.replace(".java", ".class")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        files.deleteString(name);
        String fileName = name + ".java";
        String fileNameclass = name + ".class";
        try {
            Path path = Paths.get(workingDir, fileName);
            Path pathclass = Paths.get(workingDir, fileNameclass);

            Files.deleteIfExists(path);
            Files.deleteIfExists(pathclass);

            return "Plik " + fileName + " został usunięty.";
        } catch (IOException e) {
            e.printStackTrace();
            return "Błąd podczas usuwania pliku: " + e.getMessage();
        }
    }

    public void CompileandRun(String name) {
        Path sourcePath = Paths.get(workingDir, name + ".java");
        if (!Files.exists(sourcePath)) {
            sendToFrontend("BŁĄD: Nie znaleziono pliku " + name + ".java");
            return;
        }

        new Thread(() -> {
            try {
                if (currentRunningProcess != null && currentRunningProcess.isAlive()) {
                    sendToFrontend(">>> Zatrzymywanie poprzedniego programu...");
                    currentRunningProcess.destroyForcibly();
                    currentRunningProcess.waitFor();
                }

                sendToFrontend("--- Rozpoczynam proces dla: " + name + " ---");

                // 1. KOMPILACJA
                // Pobieramy informację z managera o plikach powiązanych (jeśli są)
                ProcesDefault procInfo = processMenager.getProcesDefaultByMainProcess(name);

                ArrayList<String> compileCommands = new ArrayList<>();
                compileCommands.add("javac");
                compileCommands.add("-d");
                compileCommands.add(".");

                if (procInfo != null) {
                    // Kompilujemy wszystkie pliki należące do tego procesu
                    compileCommands.addAll(procInfo.getProcesses());
                } else {
                    compileCommands.add(name + ".java");
                }

                sendToFrontend(">>> Kompilacja plików: " + (procInfo != null ? procInfo.getProcesses() : name));

                ProcessBuilder compileBuilder = new ProcessBuilder(compileCommands);
                compileBuilder.directory(new File(workingDir));
                compileBuilder.redirectErrorStream(true);

                Process compileProcess = compileBuilder.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(compileProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sendToFrontend("[KOMPILATOR]: " + line);
                    }
                }

                if (compileProcess.waitFor() != 0) {
                    sendToFrontend("!!! BŁĄD KOMPILACJI !!!");
                    return;
                }

                // 2. URUCHOMIENIE
                sendToFrontend(">>> Uruchamianie: " + name);
                ProcessBuilder runBuilder = new ProcessBuilder("java", "-cp", ".", name);
                runBuilder.directory(new File(workingDir));
                runBuilder.redirectErrorStream(true);

                currentRunningProcess = runBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(currentRunningProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sendToFrontend(line);
                    }
                }

                int exitCode = currentRunningProcess.waitFor();
                sendToFrontend("--- Proces zakończony (exit code: " + exitCode + ") ---");

            } catch (IOException | InterruptedException e) {
                sendToFrontend("BŁĄD SYSTEMOWY: " + e.getMessage());
            }
        }).start();
    }

    private void sendToFrontend(String message) {
        messagingTemplate.convertAndSend("/topic/output", message);
    }
}