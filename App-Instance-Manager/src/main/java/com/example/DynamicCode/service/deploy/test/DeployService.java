package com.example.DynamicCode.service.deploy.test;

import com.example.DynamicCode.model.dto.proces.ProcesDefault;
import com.example.DynamicCode.notification.FrontendNotificationService;
import com.example.DynamicCode.service.test.util.ProcessMenager; // Popraw literówkę w nazwie klasy jeśli możesz (Manager)
import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

@Service
public class DeployService {

    private final FrontendNotificationService notificationService;
    private final ProcessMenager processManager;
    private final String workingDir;

    // Clean Code: Wstrzykiwanie przez konstruktor zamiast @Autowired na polach
    public DeployService(FrontendNotificationService notificationService, ProcessMenager processManager) {
        this.notificationService = notificationService;
        this.processManager = processManager;
        this.workingDir = System.getProperty("user.dir") + File.separator + "app";
    }

    public void sendAndRunSsh(String mainFileName, String remoteIp, String user, String password,
                              Integer localPort, Integer remotePort) {

        String mainName = extractFileNameWithoutExtension(mainFileName);
        String extension = extractExtension(mainFileName);
        List<String> filesToSend = resolveFilesToSend(mainFileName, mainName);

        if (!validateLocalFilesExist(filesToSend)) {
            return;
        }

        // Uruchomienie całego procesu SSH asynchronicznie
        new Thread(() -> executeDeploymentLifecycle(remoteIp, user, password, localPort, remotePort, mainName, extension, filesToSend)).start();
    }

    // Przeciążenie dla wstecznej kompatybilności
    public void sendAndRunSsh(String mainFileName, String remoteIp, String user, String password) {
        sendAndRunSsh(mainFileName, remoteIp, user, password, null, null);
    }

    private void executeDeploymentLifecycle(String remoteIp, String user, String password,
                                            Integer localPort, Integer remotePort,
                                            String mainName, String extension, List<String> filesToSend) {
        Session session = null;
        String remoteDirectory = "/home/" + user + "/deploy/";

        try {
            session = createSshSession(remoteIp, user, password);
            setupPortForwarding(session, remoteIp, localPort, remotePort);

            execRemoteCommand(session, "mkdir -p " + remoteDirectory);
            transferFilesViaSftp(session, filesToSend, remoteDirectory);

            executeRemoteApplication(session, mainName, extension, remoteDirectory);

            cleanUpPortForwarding(session, localPort);
        } catch (Exception e) {
            notificationService.sendToFrontend("!!! SSH BŁĄD KRYTYCZNY: " + e.getMessage());
        } finally {
            closeSession(session);
        }
    }

    private Session createSshSession(String remoteIp, String user, String password) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, remoteIp, 22);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");

        notificationService.sendToFrontend(">>> SSH: Łączenie z " + remoteIp + "...");
        session.connect(10000);
        return session;
    }

    private void setupPortForwarding(Session session, String remoteIp, Integer localPort, Integer remotePort) throws JSchException {
        if (localPort != null && remotePort != null) {
            session.setPortForwardingL(localPort, "localhost", remotePort);
            notificationService.sendToFrontend(String.format(">>> SSH: Port forwarding aktywny: localhost:%d → %s:%d", localPort, remoteIp, remotePort));
        }
    }

    private void cleanUpPortForwarding(Session session, Integer localPort) {
        if (localPort != null) {
            try {
                session.delPortForwardingL(localPort);
                notificationService.sendToFrontend(">>> SSH: Port forwarding na porcie " + localPort + " zamknięty.");
            } catch (JSchException ignored) {}
        }
    }

    private void transferFilesViaSftp(Session session, List<String> filesToSend, String remoteDirectory) throws JSchException, SftpException {
        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();

        notificationService.sendToFrontend(">>> SSH: Wysyłanie " + filesToSend.size() + " pliku/plików...");
        for (String fileName : filesToSend) {
            String localPath = workingDir + File.separator + fileName;
            sftp.put(localPath, remoteDirectory + fileName);
            notificationService.sendToFrontend(">>> SSH: Wysłano: " + fileName);
        }
        sftp.exit();
    }

    private void executeRemoteApplication(Session session, String mainName, String extension, String remoteDirectory) throws JSchException, IOException, InterruptedException {
        String runCommand = buildRemoteRunCommand(mainName, extension, remoteDirectory);
        notificationService.sendToFrontend(">>> SSH: Uruchamiam: " + runCommand);

        ChannelExec exec = (ChannelExec) session.openChannel("exec");
        exec.setCommand(runCommand);
        exec.connect();

        notificationService.sendToFrontend(">>> SSH: ===== START PROGRAMU =====");

        // Odczyt strumieni w osobnych wątkach (zrównoleglenie odczytu)
        Thread stdoutThread = spawnStreamReader(exec.getInputStream(), false);
        Thread stderrThread = spawnStreamReader(exec.getErrStream(), true);

        stdoutThread.join();
        stderrThread.join();

        notificationService.sendToFrontend(">>> SSH: ===== Program zakończył pracę (Kod: " + exec.getExitStatus() + ") =====");
        exec.disconnect();
    }

    private Thread spawnStreamReader(InputStream inputStream, boolean isErrorStream) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String prefix = isErrorStream ? "!!! BŁĄD: " : "";
                    notificationService.sendToFrontend(prefix + line);
                }
            } catch (Exception ignored) {}
        });
        thread.start();
        return thread;
    }

    private List<String> resolveFilesToSend(String mainFileName, String mainName) {
        ProcesDefault procInfo = processManager.getProcesDefaultByMainProcess(mainName);
        return (procInfo != null && !procInfo.getProcesses().isEmpty())
                ? procInfo.getProcesses()
                : List.of(mainFileName);
    }

    private boolean validateLocalFilesExist(List<String> filesToSend) {
        for (String fileName : filesToSend) {
            File localFile = new File(workingDir + File.separator + fileName);
            if (!localFile.exists()) {
                notificationService.sendToFrontend("!!! BŁĄD: Plik " + fileName + " nie istnieje w /app. Skompiluj projekt!");
                return false;
            }
        }
        return true;
    }

    private String extractFileNameWithoutExtension(String fileName) {
        return fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
    }

    private String extractExtension(String fileName) {
        return fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
    }

    private String buildRemoteRunCommand(String mainName, String extension, String remoteDir) {
        String commandPattern = "bash -c 'cd " + remoteDir + " && %s'";
        switch (extension) {
            case ".java":
            case ".class":
                return String.format(commandPattern, "java " + mainName);
            case ".py":
                return String.format(commandPattern, "python3 " + mainName + ".py");
            case ".cpp":
            case ".exe":
                return String.format(commandPattern, "chmod +x " + mainName + " && ./" + mainName);
            default:
                return String.format(commandPattern, "chmod +x " + mainName + extension + " && ./" + mainName + extension);
        }
    }

    private void execRemoteCommand(Session session, String command) throws JSchException, IOException {
        ChannelExec exec = (ChannelExec) session.openChannel("exec");
        exec.setCommand(command);
        exec.connect();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        exec.disconnect();
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

            session.connect(5000);
            return "SUKCES: Połączono z " + ip;
        } catch (Exception e) {
            return "BŁĄD: " + e.getMessage();
        } finally {
            closeSession(session);
        }
    }

    private void closeSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
            notificationService.sendToFrontend(">>> SSH: Połączenie zakończone.");
        }
    }
}