package com.example.DynamicCode.transfer.service;

import com.example.DynamicCode.shared.event.ServerRemovedEvent;
import com.example.DynamicCode.transfer.dto.DirectUploadCommand;
import com.example.DynamicCode.transfer.dto.DirectUploadPageResponse;
import com.example.DynamicCode.transfer.dto.DirectUploadResponse;
import com.example.DynamicCode.transfer.entity.DirectUploadRecord;
import com.example.DynamicCode.transfer.enums.DirectUploadState;
import com.example.DynamicCode.transfer.repository.DirectUploadRecordRepository;
import com.example.inspection.grpc.v1.UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DirectUploadRecords {

    private static final int MAX_ERROR_CHARS = 500;
    private static final int MAX_PAGE_SIZE = 200;
    private static final String INTERRUPTED = "Wysyłkę przerwał restart aplikacji. Wyślij plik ponownie.";

    private final DirectUploadRecordRepository records;

    DirectUploadRecord start(Long serverId, DirectUploadCommand command) {
        return records.save(new DirectUploadRecord(serverId, command.destination(), command.relativePath(), Instant.now()));
    }

    DirectUploadRecord finish(DirectUploadRecord record, UploadResult result) {
        record.setRemotePath(result.getRemotePath());
        record.setSizeBytes(result.getSizeBytes());
        record.setSha256(result.getSha256());
        record.setState(DirectUploadState.DONE);
        record.setFinishedAt(Instant.now());
        return records.save(record);
    }

    void fail(DirectUploadRecord record, String error) {
        record.setState(DirectUploadState.FAILED);
        record.setError(error.substring(0, Math.min(error.length(), MAX_ERROR_CHARS)));
        record.setFinishedAt(Instant.now());
        records.save(record);
    }

    public DirectUploadPageResponse history(Long serverId, int page, int size) {
        PageRequest request = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));
        Page<DirectUploadRecord> found = records.findByServerIdOrderByIdDesc(serverId, request);
        return new DirectUploadPageResponse(found.getContent().stream().map(DirectUploadResponse::of).toList(), found.getTotalElements());
    }

    @EventListener
    public void onServerRemoved(ServerRemovedEvent event) {
        records.deleteByServerId(event.serverId());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void failInterrupted() {
        records.failAllInState(DirectUploadState.IN_PROGRESS, DirectUploadState.FAILED, INTERRUPTED, Instant.now());
    }
}
