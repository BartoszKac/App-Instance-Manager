package com.example.DynamicCode.model.entity.procces;


import com.example.DynamicCode.constants.code.LanguageType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
}