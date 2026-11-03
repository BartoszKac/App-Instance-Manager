package com.example.DynamicCode.transfer.entity;

import com.example.DynamicCode.transfer.enums.DirectUploadState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(indexes = @Index(name = "idx_direct_upload_server", columnList = "server_id"))
public class DirectUploadRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @Column(nullable = false, length = 4000)
    private String destination;

    @Column(nullable = false, length = 4000)
    private String relativePath;

    @Column(length = 4100)
    private String remotePath;

    private long sizeBytes;

    @Column(length = 64)
    private String sha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DirectUploadState state;

    @Column(length = 500)
    private String error;

    private Instant startedAt;

    private Instant finishedAt;

    public DirectUploadRecord(Long serverId, String destination, String relativePath, Instant startedAt) {
        this.serverId = serverId;
        this.destination = destination;
        this.relativePath = relativePath;
        this.startedAt = startedAt;
        this.state = DirectUploadState.IN_PROGRESS;
    }
}
