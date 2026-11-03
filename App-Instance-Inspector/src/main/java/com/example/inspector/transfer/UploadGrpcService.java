package com.example.inspector.transfer;

import com.example.inspection.grpc.v1.ServerFileTransferGrpc;
import com.example.inspection.grpc.v1.UploadChunk;
import com.example.inspection.grpc.v1.UploadEvent;
import com.example.inspector.config.InspectorProperties;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
public class UploadGrpcService extends ServerFileTransferGrpc.ServerFileTransferImplBase {

    private final SftpUploader uploader;
    private final InspectorProperties properties;
    private final Semaphore capacity;
    private final ExecutorService workers = Executors.newCachedThreadPool();

    public UploadGrpcService(SftpUploader uploader, InspectorProperties properties) {
        this.uploader = uploader;
        this.properties = properties;
        this.capacity = new Semaphore(properties.maxConcurrentUploads());
    }

    @Override
    public StreamObserver<UploadChunk> upload(StreamObserver<UploadEvent> observer) {
        ServerCallStreamObserver<UploadEvent> response = (ServerCallStreamObserver<UploadEvent>) observer;
        response.disableAutoRequest();
        response.request(1);
        return new UploadCall(response, uploader, capacity, workers, properties.uploadIdleTimeout());
    }
}
