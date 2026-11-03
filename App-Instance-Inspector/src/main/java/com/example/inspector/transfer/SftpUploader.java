package com.example.inspector.transfer;

import com.example.inspection.grpc.v1.UploadHeader;
import com.example.inspector.inspection.InspectionFailure;
import com.example.inspector.ssh.PinnedHostKeys;
import com.example.inspector.ssh.SshConnector;
import com.example.inspector.ssh.SshLogin;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SftpUploader {

    private static final int CHANNEL_TIMEOUT_MS = 15000;

    private final SshConnector connector;

    OpenedUpload open(UploadHeader header) {
        UploadPaths.resolve(header.getDestinationDir(), header.getRelativePath());
        PinnedHostKeys hostKeys = new PinnedHostKeys(header.getPinnedHostKey());
        SshLogin login = new SshLogin(header.getHost(), header.getUser(), header.getPassword());
        Session session = connector.open(login, hostKeys, created -> { });
        try {
            ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
            sftp.connect(CHANNEL_TIMEOUT_MS);
            RemoteUpload upload = new RemoteUpload(session, sftp, header);
            upload.begin();
            return new OpenedUpload(upload, hostKeys.seenKey());
        } catch (JSchException e) {
            session.disconnect();
            throw new InspectionFailure(Status.FAILED_PRECONDITION.withDescription("Serwer nie udostępnia SFTP, więc nie można wysłać pliku."));
        } catch (RuntimeException e) {
            session.disconnect();
            throw e;
        }
    }

    record OpenedUpload(RemoteUpload upload, String hostKey) {
    }
}
