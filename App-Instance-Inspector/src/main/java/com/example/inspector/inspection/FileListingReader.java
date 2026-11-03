package com.example.inspector.inspection;

import com.example.inspection.grpc.v1.FileBatch;
import com.example.inspection.grpc.v1.FileEntry;
import com.example.inspection.grpc.v1.InspectEvent;
import com.example.inspector.config.InspectorProperties;
import com.example.inspector.ssh.RemoteCommand;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FileListingReader {

    private static final int MAX_BATCH_CHARS = 1024 * 1024;

    private final RemoteCommand remoteCommand;
    private final InspectorProperties properties;

    public Result read(Session session, Platform platform, InspectionJob job) {
        DeniedPaths denied = new DeniedPaths();
        Collector collector = new Collector(platform, job, denied);
        remoteCommand.streamRecords(session, new RemoteCommand.RecordStream(platform.filesCommand(), denied, collector::accept));
        collector.flush();
        return new Result(collector.truncated, denied.paths());
    }

    public record Result(boolean truncated, List<String> deniedPaths) {
    }

    private final class Collector {

        private final Platform platform;
        private final InspectionJob job;
        private final DeniedPaths denied;
        private final List<FileEntry> batch = new ArrayList<>();
        private int batchChars;
        private int total;
        private boolean truncated;

        private Collector(Platform platform, InspectionJob job, DeniedPaths denied) {
            this.platform = platform;
            this.job = job;
            this.denied = denied;
        }

        private boolean accept(String record) {
            job.throwIfAborted();
            Optional<FileEntry> parsed = FileRecordParser.parse(record, platform);
            parsed.ifPresent(this::collect);
            truncated = total >= properties.maxEntries();
            return !truncated;
        }

        private void collect(FileEntry entry) {
            if (FileRecordParser.DENIED_TYPE.equals(entry.getType())) {
                denied.add(entry.getPath());
                return;
            }
            batch.add(entry);
            batchChars += entry.getPath().length();
            total++;
            if (batch.size() >= properties.batchSize() || batchChars >= MAX_BATCH_CHARS) {
                flush();
            }
        }

        private void flush() {
            if (batch.isEmpty()) {
                return;
            }
            job.sink().send(InspectEvent.newBuilder().setFiles(FileBatch.newBuilder().addAllEntries(batch)).build());
            batch.clear();
            batchChars = 0;
        }
    }
}
