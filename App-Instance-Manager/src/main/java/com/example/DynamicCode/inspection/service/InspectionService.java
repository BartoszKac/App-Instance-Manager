package com.example.DynamicCode.inspection.service;

import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.service.RemoteSerwerService;
import com.example.DynamicCode.inspection.dto.InspectionStatusResponse;
import com.example.DynamicCode.inspection.entity.InspectionRun;
import com.example.DynamicCode.inspection.enums.InspectionState;
import com.example.DynamicCode.inspection.exception.InspectionNotPossibleException;
import com.example.DynamicCode.inspection.repository.InspectionHostKeyRepository;
import com.example.DynamicCode.inspection.repository.InspectionRunRepository;
import com.example.DynamicCode.shared.event.ServerCredentialsSavedEvent;
import com.example.DynamicCode.shared.event.ServerRemovedEvent;
import com.example.DynamicCode.shared.metrics.InspectorWorkGauge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionService {

    private static final String INTERRUPTED = "Badanie przerwał restart aplikacji. Uruchom je ponownie.";
    private static final String NO_PASSWORD = "Serwer nie ma zapisanego hasła, więc nie można go zbadać.";

    private final RemoteSerwerService servers;
    private final InspectionRunRepository runs;
    private final InspectionHostKeyRepository hostKeys;
    private final InspectionStore store;
    private final InspectionDispatcher dispatcher;
    private final InspectionMapper mapper;
    private final InspectorWorkGauge workGauge;

    public synchronized InspectionStatusResponse start(Long serverId) {
        RemoteSerwerConfiguration server = servers.getConfigById(serverId);
        if (server.getPass() == null || server.getPass().isEmpty()) {
            throw new InspectionNotPossibleException(NO_PASSWORD);
        }
        InspectionRun run = runs.findFirstByServerIdAndStateIn(serverId, InspectionState.ACTIVE).orElseGet(() -> enqueueNew(serverId));
        return mapper.toStatus(server, run, runs.findFirstByServerIdAndStateOrderByIdDesc(serverId, InspectionState.DONE).orElse(null));
    }

    public void forgetHostKey(Long serverId) {
        servers.getConfigById(serverId);
        hostKeys.deleteById(serverId);
    }

    public long load() {
        return runs.countByStateIn(InspectionState.ACTIVE) + workGauge.activeUploads();
    }

    @EventListener
    public void onCredentialsSaved(ServerCredentialsSavedEvent event) {
        try {
            start(event.serverId());
        } catch (RuntimeException e) {
            log.warn("Inspection of server {} not started: {}", event.serverId(), e.getClass().getSimpleName());
        }
    }

    @EventListener
    public synchronized void onServerRemoved(ServerRemovedEvent event) {
        runs.findFirstByServerIdAndStateIn(event.serverId(), InspectionState.ACTIVE)
                .ifPresent(run -> dispatcher.cancelRun(run.getId()));
        store.removeServer(event.serverId());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void failInterruptedRuns() {
        int interrupted = store.failInterrupted(INTERRUPTED);
        if (interrupted > 0) {
            log.info("Marked {} interrupted inspection runs as failed", interrupted);
        }
    }

    private InspectionRun enqueueNew(Long serverId) {
        InspectionRun run = runs.save(new InspectionRun(serverId, Instant.now()));
        dispatcher.enqueue(run.getId());
        return run;
    }
}
