package com.example.DynamicCode.inspection.entity;

import com.example.DynamicCode.inspection.enums.InspectionFailureKind;
import com.example.DynamicCode.inspection.enums.InspectionState;
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
@Table(indexes = @Index(name = "idx_inspection_run_server", columnList = "serverId"))
public class InspectionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long serverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionState state;

    private String stage;

    private String targetIp;

    private Instant queuedAt;

    private Instant startedAt;

    private Instant finishedAt;

    private long fileCount;

    private boolean truncated;

    @Column(length = 500)
    private String error;

    @Enumerated(EnumType.STRING)
    private InspectionFailureKind failureKind;

    public InspectionRun(Long serverId, Instant queuedAt) {
        this.serverId = serverId;
        this.queuedAt = queuedAt;
        this.state = InspectionState.QUEUED;
    }
}
