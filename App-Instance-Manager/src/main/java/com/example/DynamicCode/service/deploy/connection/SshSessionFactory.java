package com.example.DynamicCode.service.deploy.connection;


import com.example.DynamicCode.notification.FrontendNotificationService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SshSessionFactory {

    private final FrontendNotificationService notificationService;

    private static final int SSH_PORT = 22;

    // Zmieniamy na package-private lub public, żeby transport i execution miały do tego dostęp
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
}
