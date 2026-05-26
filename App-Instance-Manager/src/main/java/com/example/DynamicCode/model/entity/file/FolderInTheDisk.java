package com.example.DynamicCode.model.entity.file;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class FolderInTheDisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFolder;

    private String name;

    private String path;

    private boolean compiledCode;

    private Long idMainClass;
}
