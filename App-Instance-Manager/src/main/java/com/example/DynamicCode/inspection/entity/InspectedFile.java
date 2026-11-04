package com.example.DynamicCode.inspection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "idx_inspected_file_parent", columnList = "run_id, parent"),
        @Index(name = "idx_inspected_file_path", columnList = "run_id, path")
})
public class InspectedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(nullable = false, length = 4000)
    private String path;

    @Column(nullable = false, length = 4000)
    private String parent;

    @Column(nullable = false, length = 1000)
    private String name;

    @Column(name = "file_type", nullable = false, length = 1)
    private String type;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(name = "owner_name", length = 120)
    private String owner;

    @Column(name = "group_name", length = 120)
    private String group;

    @Column(name = "mode_bits", length = 120)
    private String mode;

    @Column(name = "modified_epoch")
    private long modifiedEpochSeconds;

    private boolean denied;
}
