package com.example.inspector.ssh;

import com.example.inspection.grpc.v1.InspectRequest;
import com.example.inspector.inspection.InspectionFailure;
import com.example.inspector.inspection.InspectionJob;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.grpc.Status;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SshConnector {

    private static final int SSH_PORT = 22;
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int KEEP_ALIVE_MS = 30000;

    public Session open(InspectRequest request, PinnedHostKeys hostKeys, InspectionJob job) {
        try {
            JSch jsch = new JSch();
            jsch.setHostKeyRepository(hostKeys);
            Session session = jsch.getSession(request.getUser(), request.getHost(), SSH_PORT);
            session.setPassword(request.getPassword());
            session.setConfig("StrictHostKeyChecking", "yes");
            session.setConfig("PreferredAuthentications", "password");
            session.setServerAliveInterval(KEEP_ALIVE_MS);
            job.attach(session);
            session.connect(CONNECT_TIMEOUT_MS);
            return session;
        } catch (JSchException e) {
            throw new InspectionFailure(toStatus(e, hostKeys));
        }
    }

    private Status toStatus(JSchException e, PinnedHostKeys hostKeys) {
        if (hostKeys.isMismatch()) {
            return Status.PERMISSION_DENIED.withDescription("Klucz SSH serwera się zmienił. Sprawdź serwer, zanim mu zaufasz.");
        }
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase(Locale.ROOT);
        if (message.contains("auth")) {
            return Status.UNAUTHENTICATED.withDescription("Serwer odrzucił login lub hasło. Popraw dane serwera.");
        }
        return Status.FAILED_PRECONDITION.withDescription("Nie udało się połączyć przez SSH. Sprawdź adres i czy serwer działa.");
    }
}
