package com.example.DynamicCode.transfer.config;

import com.example.inspection.grpc.v1.ServerFileTransferGrpc;
import io.grpc.ManagedChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectUploadConfig {

    @Bean
    public ServerFileTransferGrpc.ServerFileTransferStub serverFileTransferStub(ManagedChannel inspectorChannel) {
        return ServerFileTransferGrpc.newStub(inspectorChannel);
    }
}
