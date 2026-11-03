package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.shared.notification.FrontendNotificationService;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@RequiredArgsConstructor
@Component
@Slf4j
public class SshSessionFactory {

    private static final int SSH_PORT = 22;
    private static final int CONNECTION_TIMEOUT_MS = 10000;
    private static final int MAX_BOUNDED_OUTPUT_BYTES = 256 * 1024;
    private static final int POLL_INTERVAL_MS = 100;

    private final FrontendNotificationService notificationService;

    public Session createSession(String remoteIp, String user, String password) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, remoteIp, SSH_PORT);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    public String executeCommand(String ip, String user, String pass, String command) throws Exception {
        return execute(new SshTarget(ip, user, pass), command, Long.MAX_VALUE, Integer.MAX_VALUE);
    }

    public String executeBounded(SshTarget target, String command, Duration timeout) throws Exception {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        return execute(target, command, deadline, MAX_BOUNDED_OUTPUT_BYTES);
    }

    private String execute(SshTarget target, String command, long deadline, int maxBytes) throws Exception {
        notificationService.sendToFrontend(">>> SSH: Nawiązywanie połączenia z " + target.ip());
        Session session = createSession(target.ip(), target.user(), target.pass());
        ChannelExec channel = null;
        try {
            session.connect(CONNECTION_TIMEOUT_MS);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            InputStream in = channel.getInputStream();
            channel.connect();
            String result = readUntilClosed(channel, in, deadline, maxBytes);
            notificationService.sendToFrontend(">>> SSH: Polecenie wykonane pomyślnie.");
            return result;
        } catch (Exception e) {
            log.error("Błąd SSH na serwerze {}: {}", target.ip(), e.getMessage());
            notificationService.sendToFrontend(">>> SSH BŁĄD: " + e.getMessage());
            throw e;
        } finally {
            close(channel, session);
        }
    }

    private String readUntilClosed(ChannelExec channel, InputStream in, long deadline, int maxBytes)
            throws IOException, InterruptedException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!channel.isClosed() || in.available() > 0) {
            if (System.currentTimeMillis() > deadline) {
                throw new IOException("Polecenie SSH przekroczyło limit czasu");
            }
            if (in.available() > 0) {
                int read = in.read(buffer, 0, buffer.length);
                output.write(buffer, 0, Math.min(Math.max(read, 0), Math.max(maxBytes - output.size(), 0)));
            } else {
                Thread.sleep(POLL_INTERVAL_MS);
            }
        }
        return output.toString(StandardCharsets.UTF_8).trim();
    }

    private void close(ChannelExec channel, Session session) {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
            notificationService.sendToFrontend(">>> SSH: Połączenie zakończone.");
        }
    }

    public record SshTarget(String ip, String user, String pass) {

        @Override
        public String toString() {
            return "SshTarget[" + ip + "]";
        }
    }
}
