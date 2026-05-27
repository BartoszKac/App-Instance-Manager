package com.example.DynamicCode.model.entity.file;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SourceFolderInTheDisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "path_in_project")
    private String path;

    @Column(name = "source_code_id", nullable = true)
    private Long sourceCodeId;

}