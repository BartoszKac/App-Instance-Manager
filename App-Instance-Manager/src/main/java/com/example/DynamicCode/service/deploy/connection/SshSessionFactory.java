package com.example.DynamicCode.service.deploy.connection;


import com.example.DynamicCode.notification.FrontendNotificationService;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@RequiredArgsConstructor
@Component
@Slf4j
public class SshSessionFactory {

    private final FrontendNotificationService notificationService;

    private static final int SSH_PORT = 22;
    private static final int CONNECTION_TIMEOUT_MS = 10000;
    public Session createSession(String remoteIp, String user, String password) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, remoteIp, SSH_PORT);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    private void closeSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
            notificationService.sendToFrontend(">>> SSH: Połączenie zakończone.");
        }
    }
    public String executeCommand(String ip, String user, String pass, String command) throws Exception {
        notificationService.sendToFrontend(">>> SSH: Nawiązywanie połączenia z " + ip);

        Session session = createSession(ip, user, pass);
        ChannelExec channel = null;

        try {
            session.connect(CONNECTION_TIMEOUT_MS);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            InputStream in = channel.getInputStream();
            channel.connect();

            // Bezpieczny odczyt strumienia dla JSch
            StringBuilder outputBuffer = new StringBuilder();
            byte[] tmp = new byte[1024];

            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    outputBuffer.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    break;
                }
                try { Thread.sleep(100); } catch (Exception ee) {}
            }

            String result = outputBuffer.toString().trim();
            notificationService.sendToFrontend(">>> SSH: Polecenie wykonane pomyślnie.");
            return result;

        } catch (Exception e) {
            log.error("Błąd podczas wykonywania komendy SSH [{}] na serwerze {}: {}", command, ip, e.getMessage());
            notificationService.sendToFrontend(">>> SSH BŁĄD: " + e.getMessage());
            throw e;
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            closeSession(session);
        }
    }
}
