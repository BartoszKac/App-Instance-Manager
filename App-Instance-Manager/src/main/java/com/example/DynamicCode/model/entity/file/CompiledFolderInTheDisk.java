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
@Table(name = "compiled_disk_folders") // Jawna nazwa nowej tabeli w bazie danych
public class CompiledFolderInTheDisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "path_in_project")
    private String path;

    @Column(name = "compiled_code_id", nullable = true)
    private Long compiledCodeId;
}