package com.example.DynamicCode.inspection.service;

import com.example.DynamicCode.inspection.entity.InspectionHostKey;
import com.example.DynamicCode.inspection.entity.InspectionRun;
import com.example.DynamicCode.inspection.entity.InspectionSection;
import com.example.DynamicCode.inspection.enums.InspectionFailureKind;
import com.example.DynamicCode.inspection.enums.InspectionState;
import com.example.DynamicCode.inspection.repository.InspectionHostKeyRepository;
import com.example.DynamicCode.inspection.repository.InspectionRunRepository;
import com.example.DynamicCode.inspection.repository.InspectionSectionRepository;
import com.example.inspection.grpc.v1.Completed;
import com.example.inspection.grpc.v1.FileEntry;
import com.example.inspection.grpc.v1.InspectEvent;
import com.example.inspection.grpc.v1.InspectionStage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
public class InspectionStore {

    private static final int MAX_ERROR_CHARS = 500;
    private static final String INSERT_FILE = "insert into inspected_file (run_id, path, parent, name, file_type, size_bytes, "
            + "owner_name, group_name, mode_bits, modified_epoch, denied) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, false)";

    private final InspectionRunRepository runs;
    private final InspectionHostKeyRepository hostKeys;
    private final JdbcTemplate jdbc;
    private final Map<InspectEvent.EventCase, BiConsumer<InspectionRun, InspectEvent>> handlers;

    public InspectionStore(InspectionRunRepository runs, InspectionSectionRepository sections,
                           InspectionHostKeyRepository hostKeys, JdbcTemplate jdbc) {
        this.runs = runs;
        this.hostKeys = hostKeys;
        this.jdbc = jdbc;
        this.handlers = Map.of(
                InspectEvent.EventCase.STAGE, (run, event) -> markStage(run, event.getStage().getStage()),
                InspectEvent.EventCase.SECTION, (run, event) -> sections.save(new InspectionSection(run.getId(), event.getSection())),
                InspectEvent.EventCase.FILES, (run, event) -> saveFiles(run, event.getFiles().getEntriesList()),
                InspectEvent.EventCase.COMPLETED, (run, event) -> complete(run, event.getCompleted()));
    }

    @Transactional
    public void apply(Long runId, InspectEvent event) {
        InspectionRun run = runs.findById(runId).orElseThrow();
        handlers.getOrDefault(event.getEventCase(), (ignoredRun, ignoredEvent) -> { }).accept(run, event);
    }

    @Transactional
    public void markRunning(Long runId, String targetIp) {
        InspectionRun run = runs.findById(runId).orElseThrow();
        run.setState(InspectionState.RUNNING);
        run.setStartedAt(Instant.now());
        run.setTargetIp(targetIp);
    }

    @Transactional
    public void requeue(Long runId) {
        deleteRowsOf(runId);
        InspectionRun run = runs.findById(runId).orElseThrow();
        run.setState(InspectionState.QUEUED);
        run.setStage(null);
        run.setFileCount(0);
    }

    @Transactional
    public void fail(Long runId, String error, InspectionFailureKind kind) {
        deleteRowsOf(runId);
        runs.findById(runId).ifPresent(run -> {
            run.setState(InspectionState.FAILED);
            run.setFinishedAt(Instant.now());
            run.setError(error == null ? null : error.substring(0, Math.min(error.length(), MAX_ERROR_CHARS)));
            run.setFailureKind(kind);
            run.setFileCount(0);
        });
    }

    @Transactional
    public void finishIfUnfinished(Long runId, String error) {
        runs.findById(runId)
                .filter(run -> run.getState() != InspectionState.DONE)
                .ifPresent(run -> fail(runId, error, InspectionFailureKind.OTHER));
    }

    @Transactional
    public int failInterrupted(String error) {
        List<InspectionRun> interrupted = runs.findByStateIn(InspectionState.ACTIVE);
        interrupted.forEach(run -> fail(run.getId(), error, InspectionFailureKind.OTHER));
        return interrupted.size();
    }

    @Transactional
    public void removeServer(Long serverId) {
        runs.findByServerId(serverId).forEach(run -> {
            deleteRowsOf(run.getId());
            runs.delete(run);
        });
        hostKeys.deleteById(serverId);
    }

    private void markStage(InspectionRun run, InspectionStage stage) {
        run.setStage(stage.name().replace("INSPECTION_STAGE_", ""));
    }

    private void saveFiles(InspectionRun run, List<FileEntry> entries) {
        jdbc.batchUpdate(INSERT_FILE, entries, entries.size(), (statement, entry) -> {
            statement.setLong(1, run.getId());
            statement.setString(2, entry.getPath());
            statement.setString(3, entry.getParent());
            statement.setString(4, entry.getName());
            statement.setString(5, entry.getType());
            statement.setLong(6, entry.getSizeBytes());
            statement.setString(7, entry.getOwner());
            statement.setString(8, entry.getOwnerGroup());
            statement.setString(9, entry.getMode());
            statement.setLong(10, entry.getModifiedEpochSeconds());
        });
        run.setFileCount(run.getFileCount() + entries.size());
    }

    private void complete(InspectionRun run, Completed completed) {
        markDenied(run.getId(), completed.getDeniedPathsList());
        run.setTruncated(completed.getTruncated());
        run.setState(InspectionState.DONE);
        run.setFinishedAt(Instant.now());
        pinHostKey(run, completed.getHostKey());
        removeOlderRuns(run);
    }

    private void markDenied(Long runId, List<String> denied) {
        if (!denied.isEmpty()) {
            jdbc.batchUpdate("update inspected_file set denied = true where run_id = ? and path = ?", denied, denied.size(),
                    (statement, path) -> {
                        statement.setLong(1, runId);
                        statement.setString(2, path);
                    });
        }
    }

    private void pinHostKey(InspectionRun run, String fingerprint) {
        if (!fingerprint.isEmpty()) {
            hostKeys.save(new InspectionHostKey(run.getServerId(), run.getTargetIp(), fingerprint));
        }
    }

    private void removeOlderRuns(InspectionRun current) {
        runs.findByServerId(current.getServerId()).stream()
                .filter(run -> run.getId() < current.getId())
                .forEach(run -> {
                    deleteRowsOf(run.getId());
                    runs.delete(run);
                });
    }

    private void deleteRowsOf(Long runId) {
        jdbc.update("delete from inspected_file where run_id = ?", runId);
        jdbc.update("delete from inspection_section where run_id = ?", runId);
    }
}
