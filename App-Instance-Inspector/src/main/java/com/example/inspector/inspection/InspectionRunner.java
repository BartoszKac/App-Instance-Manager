package com.example.inspector.inspection;

import com.example.inspection.grpc.v1.Completed;
import com.example.inspection.grpc.v1.InspectEvent;
import com.example.inspection.grpc.v1.InspectRequest;
import com.example.inspection.grpc.v1.InspectionStage;
import com.example.inspection.grpc.v1.Section;
import com.example.inspection.grpc.v1.StageChanged;
import com.example.inspection.grpc.v1.TargetPlatform;
import com.example.inspector.ssh.PinnedHostKeys;
import com.example.inspector.ssh.RemoteCommand;
import com.example.inspector.ssh.SshConnector;
import com.example.inspector.ssh.SshLogin;
import com.jcraft.jsch.Session;
import io.grpc.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InspectionRunner {

    private static final int MAX_FACTS_BYTES = 4 * 1024 * 1024;

    private final SshConnector connector;
    private final RemoteCommand remoteCommand;
    private final FileListingReader fileListingReader;

    public void run(InspectRequest request, InspectionJob job) {
        stage(job, InspectionStage.INSPECTION_STAGE_CONNECTING);
        PinnedHostKeys hostKeys = new PinnedHostKeys(request.getPinnedHostKey());
        SshLogin login = new SshLogin(request.getHost(), request.getUser(), request.getPassword());
        Session session = connector.open(login, hostKeys, job::attach);
        try {
            stage(job, InspectionStage.INSPECTION_STAGE_FACTS);
            Platform platform = collectFacts(session, request, job);
            stage(job, InspectionStage.INSPECTION_STAGE_FILES);
            FileListingReader.Result listing = fileListingReader.read(session, platform, job);
            job.throwIfAborted();
            job.sink().send(completed(hostKeys.seenKey(), platform, listing));
        } finally {
            session.disconnect();
        }
    }

    private Platform collectFacts(Session session, InspectRequest request, InspectionJob job) {
        for (Platform platform : Platform.orderFor(request.getPreferredPlatform())) {
            String output = remoteCommand.capture(session, platform.factsCommand(), MAX_FACTS_BYTES);
            job.throwIfAborted();
            if (FactsParser.isFacts(output)) {
                FactsParser.parse(output).forEach(section -> sendSection(job, section));
                return platform;
            }
        }
        throw new InspectionFailure(Status.FAILED_PRECONDITION.withDescription("Połączono, ale serwer nie zwrócił danych."));
    }

    private void sendSection(InspectionJob job, Section section) {
        job.sink().send(InspectEvent.newBuilder().setSection(section).build());
    }

    private void stage(InspectionJob job, InspectionStage stage) {
        job.sink().send(InspectEvent.newBuilder().setStage(StageChanged.newBuilder().setStage(stage)).build());
    }

    private InspectEvent completed(String hostKey, Platform platform, FileListingReader.Result listing) {
        List<String> denied = listing.deniedPaths();
        TargetPlatform target = platform.target();
        return InspectEvent.newBuilder().setCompleted(Completed.newBuilder()
                .setHostKey(hostKey)
                .setPlatform(target)
                .setTruncated(listing.truncated())
                .addAllDeniedPaths(denied)).build();
    }
}
