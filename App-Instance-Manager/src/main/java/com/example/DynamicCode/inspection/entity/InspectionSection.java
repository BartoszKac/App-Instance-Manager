package com.example.DynamicCode.inspection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(indexes = @Index(name = "idx_inspection_section_run", columnList = "runId"))
public class InspectionSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long runId;

    @Column(name = "section_key", nullable = false)
    private String key;

    private String label;

    private String category;

    @Lob
    @Column(name = "section_value")
    private String value;

    public InspectionSection(Long runId, com.example.inspection.grpc.v1.Section section) {
        this.runId = runId;
        this.key = section.getKey();
        this.label = section.getLabel();
        this.category = section.getCategory();
        this.value = section.getValue();
    }
}
