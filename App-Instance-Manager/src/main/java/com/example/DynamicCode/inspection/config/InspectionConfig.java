package com.example.DynamicCode.inspection.config;

import com.example.inspection.grpc.v1.ServerInspectionGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableConfigurationProperties(InspectionProperties.class)
public class InspectionConfig {

    private static final int MAX_INBOUND_BYTES = 16 * 1024 * 1024;

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel inspectorChannel(InspectionProperties properties) {
        return ManagedChannelBuilder.forTarget(properties.target())
                .defaultLoadBalancingPolicy("round_robin")
                .usePlaintext()
                .maxInboundMessageSize(MAX_INBOUND_BYTES)
                .build();
    }

    @Bean
    public ServerInspectionGrpc.ServerInspectionStub serverInspectionStub(ManagedChannel inspectorChannel) {
        return ServerInspectionGrpc.newStub(inspectorChannel);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService inspectionPersistenceExecutor(InspectionProperties properties) {
        return Executors.newFixedThreadPool(properties.persistenceThreads());
    }

    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService inspectionRetryScheduler() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
