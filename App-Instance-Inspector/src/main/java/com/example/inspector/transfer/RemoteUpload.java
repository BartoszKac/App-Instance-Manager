package com.example.inspector.transfer;

import com.example.inspection.grpc.v1.UploadHeader;
import com.example.inspection.grpc.v1.UploadResult;
import com.example.inspector.inspection.InspectionFailure;
import com.google.protobuf.ByteString;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import io.grpc.Status;

import java.io.IOException;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

final class RemoteUpload {

    private final Session session;
    private final ChannelSftp sftp;
    private final UploadPaths paths;
    private final UploadHeader header;
    private final String tempPath;
    private final MessageDigest digest;
    private OutputStream output;
    private long written;

    RemoteUpload(Session session, ChannelSftp sftp, UploadHeader header) {
        this.session = session;
        this.sftp = sftp;
        this.header = header;
        this.paths = UploadPaths.resolve(header.getDestinationDir(), header.getRelativePath());
        this.tempPath = paths.join(".aim-part-" + UUID.randomUUID());
        this.digest = sha256();
    }

    void begin() {
        try {
            rejectExistingTarget();
            createDirectories();
            output = new DigestOutputStream(sftp.put(tempPath, ChannelSftp.OVERWRITE), digest);
        } catch (SftpException e) {
            throw failure("Nie można zapisać w tym katalogu. Sprawdź ścieżkę i uprawnienia konta.");
        }
    }

    void write(ByteString data) {
        try {
            data.writeTo(output);
            written += data.size();
        } catch (IOException e) {
            throw failure("Zapis na serwerze się nie udał. Sprawdź wolne miejsce na dysku.");
        }
    }

    UploadResult finish() {
        try {
            output.close();
            output = null;
            verifySize();
            if (header.getOverwrite() && exists(paths.target())) {
                sftp.rm(paths.target());
            }
            sftp.rename(tempPath, paths.target());
            return UploadResult.newBuilder()
                    .setRemotePath(paths.target())
                    .setSizeBytes(written)
                    .setSha256(HexFormat.of().formatHex(digest.digest()))
                    .build();
        } catch (IOException | SftpException e) {
            throw failure("Nie udało się dokończyć zapisu pliku na serwerze.");
        }
    }

    void abort() {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
            output = null;
        }
        removeTempQuietly();
    }

    void close() {
        sftp.disconnect();
        session.disconnect();
    }

    private void rejectExistingTarget() throws SftpException {
        SftpATTRS attrs = attrsOf(paths.target());
        if (attrs == null) {
            return;
        }
        if (attrs.isDir()) {
            throw new InspectionFailure(Status.ALREADY_EXISTS.withDescription("W tym miejscu jest katalog o tej nazwie: " + paths.target()));
        }
        if (!header.getOverwrite()) {
            throw new InspectionFailure(Status.ALREADY_EXISTS.withDescription("Plik już istnieje: " + paths.target()));
        }
    }

    private void createDirectories() throws SftpException {
        for (String directory : paths.directoryChain()) {
            if (!exists(directory)) {
                mkdirTolerant(directory);
            }
        }
    }

    private void mkdirTolerant(String directory) throws SftpException {
        try {
            sftp.mkdir(directory);
        } catch (SftpException e) {
            if (!exists(directory)) {
                throw e;
            }
        }
    }

    private void verifySize() {
        boolean incomplete = header.getExpectedSize() >= 0 && header.getExpectedSize() != written;
        if (incomplete) {
            throw failure("Plik dotarł niekompletny. Wyślij go ponownie.");
        }
    }

    private boolean exists(String path) throws SftpException {
        return attrsOf(path) != null;
    }

    private SftpATTRS attrsOf(String path) throws SftpException {
        try {
            return sftp.stat(path);
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return null;
            }
            throw e;
        }
    }

    private void removeTempQuietly() {
        try {
            sftp.rm(tempPath);
        } catch (SftpException e) {
            output = null;
        }
    }

    private InspectionFailure failure(String message) {
        return new InspectionFailure(Status.FAILED_PRECONDITION.withDescription(message));
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
