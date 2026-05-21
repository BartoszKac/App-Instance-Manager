package com.example.DynamicCode.service.deploy;

import com.example.DynamicCode.model.ProcesDefault;
import com.example.DynamicCode.notification.FrontendNotificationService;
import com.example.DynamicCode.service.util.ProcessMenager;
import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Service
public class DeployService {

    @Autowired
    private FrontendNotificationService frontendNotificationService;

    @Autowired
    private ProcessMenager processMenager;

    private final String workingDir = System.getProperty("user.dir") + File.separator + "app";


    public void sendAndRunSsh(String mainFileName, String remoteIp, String user, String password) {
        String remoteDirectory = "/home/" + user + "/deploy/";

        // Wyciągamy nazwę główną (bez rozszerzenia) żeby znaleźć ProcesDefault
        String mainName = mainFileName.contains(".")
                ? mainFileName.substring(0, mainFileName.lastIndexOf('.'))
                : mainFileName;
        String extension = mainFileName.contains(".")
                ? mainFileName.substring(mainFileName.lastIndexOf('.'))
                : "";

        // Pobieramy listę WSZYSTKICH powiązanych plików
        ProcesDefault procInfo = processMenager.getProcesDefaultByMainProcess(mainName);
        List<String> filesToSend = (procInfo != null && !procInfo.getProcesses().isEmpty())
                ? procInfo.getProcesses()
                : List.of(mainFileName); // fallback: tylko główny

        // Sprawdzamy czy wszystkie pliki istnieją lokalnie
        for (String fileName : filesToSend) {
            File localFile = new File(workingDir + File.separator + fileName);
            if (!localFile.exists()) {
                frontendNotificationService.sendToFrontend("!!! BŁĄD: Plik " + fileName + " nie istnieje w folderze /app. Skompiluj projekt!");
                return;
            }
        }

        new Thread(() -> {
            Session session = null;
            try {
                JSch jsch = new JSch();
                session = jsch.getSession(user, remoteIp, 22);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");

                frontendNotificationService.sendToFrontend(">>> SSH: Łączenie z " + remoteIp + "...");
                session.connect(10000);

                // 1. Tworzymy zdalny katalog deploy jeśli nie istnieje
                execRemoteCommand(session, "mkdir -p " + remoteDirectory);

                // 2. SFTP - wysyłamy WSZYSTKIE pliki
                ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
                sftp.connect();

                frontendNotificationService.sendToFrontend(">>> SSH: Wysyłanie " + filesToSend.size() + " pliku/plików...");
                for (String fileName : filesToSend) {
                    String localPath = workingDir + File.separator + fileName;
                    sftp.put(localPath, remoteDirectory + fileName);
                    frontendNotificationService.sendToFrontend(">>> SSH: Wysłano: " + fileName);
                }

                sftp.exit();

                // 3. Budujemy komendę uruchomienia na podstawie rozszerzenia
                String runCommand = buildRemoteRunCommand(mainName, extension, remoteDirectory);
                frontendNotificationService.sendToFrontend(">>> SSH: Uruchamiam: " + runCommand);

                // 4. EXEC - uruchamiamy i czytamy output
                ChannelExec exec = (ChannelExec) session.openChannel("exec");
                exec.setCommand(runCommand);

                InputStream in = exec.getInputStream();
                InputStream err = exec.getErrStream();
                exec.connect();

                frontendNotificationService.sendToFrontend(">>> SSH: ===== START PROGRAMU =====");

                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        frontendNotificationService.sendToFrontend(new String(tmp, 0, i));
                    }
                    while (err.available() > 0) {
                        int i = err.read(tmp, 0, 1024);
                        if (i < 0) break;
                        frontendNotificationService.sendToFrontend("!!! BŁĄD: " + new String(tmp, 0, i));
                    }
                    if (exec.isClosed()) {
                        if (in.available() > 0 || err.available() > 0) continue;
                        frontendNotificationService.sendToFrontend(">>> SSH: ===== Program zakończył pracę (Kod: " + exec.getExitStatus() + ") =====");
                        break;
                    }
                    Thread.sleep(200);
                }

                exec.disconnect();

            } catch (Exception e) {
                frontendNotificationService.sendToFrontend("!!! SSH BŁĄD KRYTYCZNY: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (session != null) session.disconnect();
                frontendNotificationService.sendToFrontend(">>> SSH: Połączenie zakończone.");
            }
        }).start();
    }

    /**
     * Buduje komendę uruchomienia na serwerze w zależności od języka.
     */
    private String buildRemoteRunCommand(String mainName, String extension, String remoteDir) {
        switch (extension) {
            case ".java":
            case ".class":
                // Java: uruchamiamy skompilowaną klasę
                return "bash -c 'cd " + remoteDir + " && java " + mainName + "'";
            case ".py":
                return "bash -c 'cd " + remoteDir + " && python3 " + mainName + ".py'";
            case ".cpp":
            case ".exe":
                // C++: plik binarny (skompilowany wcześniej lub .exe wysłane)
                return "bash -c 'cd " + remoteDir + " && chmod +x " + mainName + " && ./" + mainName + "'";
            default:
                // Fallback: próbujemy uruchomić jako plik wykonywalny
                return "bash -c 'cd " + remoteDir + " && chmod +x " + mainName + extension + " && ./" + mainName + extension + "'";
        }
    }

    /**
     * Pomocnicza metoda do wykonania prostej komendy (bez odczytu outputu).
     */
    private void execRemoteCommand(Session session, String command) throws JSchException, IOException {
        ChannelExec exec = (ChannelExec) session.openChannel("exec");
        exec.setCommand(command);
        exec.connect();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        exec.disconnect();
    }

    public void sendSsh(String fileName, String remoteIp, String user, String password) {
        String localFilePath = workingDir + File.separator + fileName;
        String remoteDirectory = "/home/" + user + "/";

        new Thread(() -> {
            Session session = null;
            try {
                JSch jsch = new JSch();
                session = jsch.getSession(user, remoteIp, 22);
                session.setPassword(password);

                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                config.put("PreferredAuthentications", "password,keyboard-interactive");
                session.setConfig(config);

                frontendNotificationService.sendToFrontend(">>> SSH: Łączenie z " + remoteIp + "...");
                session.connect(10000);

                ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
                sftp.connect();

                frontendNotificationService.sendToFrontend(">>> SSH: Przesyłanie " + fileName + "...");
                sftp.put(localFilePath, remoteDirectory + fileName);
                sftp.exit();

                frontendNotificationService.sendToFrontend(">>> SSH: SUKCES! Plik " + fileName + " jest już na Linuxie w " + remoteDirectory);

            } catch (Exception e) {
                frontendNotificationService.sendToFrontend("!!! SSH BŁĄD: " + e.getMessage());
                System.err.println("Błąd podczas wysyłania pliku: " + localFilePath);
                e.printStackTrace();
            } finally {
                if (session != null) session.disconnect();
            }
        }).start();
    }

    public String testSshConnection(String ip, String user, String password) {
        Session session = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, ip, 22);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "password");
            session.setConfig(config);

            System.out.println(">>> Logowanie do: " + ip + " jako: " + user);
            session.connect(5000);

            System.out.println(">>> SUKCES: SSH zalogowane!");
            return "SUKCES: Połączono z " + ip;

        } catch (Exception e) {
            System.out.println(">>> BŁĄD SSH: " + e.getMessage());
            return "BŁĄD: " + e.getMessage();
        } finally {
            if (session != null) session.disconnect();
        }
    }
}
