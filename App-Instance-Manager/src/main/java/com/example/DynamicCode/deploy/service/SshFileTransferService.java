package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import com.example.DynamicCode.deploy.exception.NoFilesToTransferException;
import com.example.DynamicCode.deploy.strategy.FileUploadStrategyFactory;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SshFileTransferService {

    private static final int CONNECTION_TIMEOUT_MS = 10000;

    private final SshSessionFactory sshSessionFactory;
    private final FileUploadStrategyFactory strategyFactory;

    public void uploadWithStrategy(RemoteTarget target, UploadStrategyType strategyType, Long id)
            throws JSchException, SftpException {
        Map<String, byte[]> files = strategyFactory.getStrategy(strategyType).resolveFiles(id);
        if (files == null || files.isEmpty()) {
            throw new NoFilesToTransferException(id, strategyType);
        }
        Session session = sshSessionFactory.createSession(target.ip(), target.user(), target.password());
        ChannelSftp channel = null;
        try {
            session.connect(CONNECTION_TIMEOUT_MS);
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            uploadAll(channel, target.directory(), files);
            log.info("Sent {} file(s) for id {} with strategy {}", files.size(), id, strategyType);
        } finally {
            closeChannel(channel);
            closeSession(session);
        }
    }

    private void uploadAll(ChannelSftp channel, String directory, Map<String, byte[]> files) throws SftpException {
        String normalizedDir = directory.isEmpty() || directory.endsWith("/") ? directory : directory + "/";
        for (Map.Entry<String, byte[]> file : files.entrySet()) {
            channel.put(new ByteArrayInputStream(file.getValue()), normalizedDir + file.getKey());
        }
    }

    private void closeChannel(ChannelSftp channel) {
        if (channel != null && channel.isConnected()) {
            channel.exit();
        }
    }

    private void closeSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}
