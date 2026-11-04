package com.example.inspector.ssh;

import com.example.inspector.inspection.InspectionFailure;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.grpc.Status;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;

@Component
public class RemoteCommand {

    private static final int CHANNEL_TIMEOUT_MS = 15000;
    private static final int MAX_RECORD_BYTES = 16 * 1024;

    public String capture(Session session, String command, int maxBytes) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        run(session, new Exec(command, OutputStream.nullOutputStream()), in -> copyBounded(in, output, maxBytes));
        return output.toString(StandardCharsets.UTF_8);
    }

    public void streamRecords(Session session, RecordStream stream) {
        run(session, new Exec(stream.command(), stream.errors()), in -> readRecords(new BufferedInputStream(in), stream.onRecord()));
    }

    private void run(Session session, Exec exec, StreamReader reader) {
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(exec.command());
            channel.setErrStream(exec.errors(), true);
            InputStream in = channel.getInputStream();
            channel.connect(CHANNEL_TIMEOUT_MS);
            reader.read(in);
            if (!session.isConnected()) {
                throw new IOException("Session closed before the command finished");
            }
        } catch (JSchException | IOException e) {
            throw new InspectionFailure(Status.FAILED_PRECONDITION.withDescription("Połączenie SSH z serwerem zostało przerwane."));
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    private void copyBounded(InputStream in, ByteArrayOutputStream output, int maxBytes) throws IOException {
        byte[] buffer = new byte[8192];
        int read = in.read(buffer);
        while (read >= 0) {
            output.write(buffer, 0, Math.min(read, Math.max(maxBytes - output.size(), 0)));
            read = in.read(buffer);
        }
    }

    private void readRecords(InputStream in, Predicate<String> onRecord) throws IOException {
        ByteArrayOutputStream record = new ByteArrayOutputStream();
        boolean proceed = true;
        int value = in.read();
        while (value >= 0 && proceed) {
            if (value == 0) {
                proceed = onRecord.test(record.toString(StandardCharsets.UTF_8));
                record.reset();
            } else if (record.size() < MAX_RECORD_BYTES) {
                record.write(value);
            }
            value = proceed ? in.read() : -1;
        }
    }

    public record RecordStream(String command, OutputStream errors, Predicate<String> onRecord) {
    }

    private record Exec(String command, OutputStream errors) {
    }

    @FunctionalInterface
    private interface StreamReader {
        void read(InputStream in) throws IOException;
    }
}
