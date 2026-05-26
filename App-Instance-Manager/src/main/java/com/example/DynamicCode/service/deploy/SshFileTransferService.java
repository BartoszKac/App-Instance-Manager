package com.example.DynamicCode.service.deploy;

import org.springframework.stereotype.Service;


import com.jcraft.jsch.*;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.List;

@Service
public class SshFileTransferService {

    private static final int SSH_PORT = 22;
    private static final int CONNECTION_TIMEOUT_MS = 10000;


    public void uploadFiles(String remoteIp, String user, String password,
                            String localDirectory, List<String> fileNames, String remoteDirectory) throws JSchException, SftpException {

        Session session = createSession(remoteIp, user, password);
        ChannelSftp sftpChannel = null;

        try {
            session.connect(CONNECTION_TIMEOUT_MS);

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            for (String fileName : fileNames) {
                String localPath = localDirectory + File.separator + fileName;
                sftpChannel.put(localPath, remoteDirectory + fileName);
            }

        } finally {
            if (sftpChannel != null && sftpChannel.isConnected()) {
                sftpChannel.exit();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private Session createSession(String remoteIp, String user, String password) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, remoteIp, SSH_PORT);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }
}