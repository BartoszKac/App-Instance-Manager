package com.example.DynamicCode.inspection.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class InspectionHostKey {

    @Id
    private Long serverId;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private String fingerprint;
}
