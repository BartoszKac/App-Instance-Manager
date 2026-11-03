package com.example.inspector.config;

import com.example.inspector.grpc.InspectionGrpcService;
import com.example.inspector.transfer.UploadGrpcService;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcServerLifecycle implements SmartLifecycle {

    private static final Duration MAX_CONNECTION_AGE = Duration.ofMinutes(2);
    private static final Duration SHUTDOWN_MARGIN = Duration.ofMinutes(1);

    private final InspectorProperties properties;
    private final InspectionGrpcService inspectionService;
    private final UploadGrpcService uploadService;

    private Server server;
    private HealthStatusManager health;

    @Override
    public void start() {
        health = new HealthStatusManager();
        server = buildServer();
        try {
            server.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Thread keepAlive = new Thread(this::awaitTermination, "grpc-await");
        keepAlive.setDaemon(false);
        keepAlive.start();
        log.info("Inspector gRPC server listening on port {}", server.getPort());
    }

    @Override
    public void stop() {
        health.enterTerminalState();
        server.shutdown();
        try {
            long graceSeconds = properties.jobTimeout().plus(SHUTDOWN_MARGIN).toSeconds();
            server.awaitTermination(graceSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        server.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return server != null && !server.isShutdown();
    }

    private Server buildServer() {
        return NettyServerBuilder.forPort(properties.port())
                .maxConnectionAge(MAX_CONNECTION_AGE.toSeconds(), TimeUnit.SECONDS)
                .maxConnectionAgeGrace(properties.jobTimeout().plus(SHUTDOWN_MARGIN).toSeconds(), TimeUnit.SECONDS)
                .addService(inspectionService)
                .addService(uploadService)
                .addService(health.getHealthService())
                .build();
    }

    private void awaitTermination() {
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
