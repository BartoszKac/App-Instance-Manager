package com.example.DynamicCode.process.entity;

import com.example.DynamicCode.code.enums.LanguageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "remote_executable")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteExecutable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "contents", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contents;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private LanguageType language;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;
}
