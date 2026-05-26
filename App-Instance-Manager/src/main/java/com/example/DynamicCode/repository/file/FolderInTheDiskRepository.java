package com.example.DynamicCode.repository.file;

import com.example.DynamicCode.model.entity.file.FolderInTheDisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderInTheDiskRepository extends JpaRepository<FolderInTheDisk, Long> {

    FolderInTheDisk findByPath(String path);

    void deleteByPath(String path);

    List<FolderInTheDisk> findByIdMainClass(Long idMainClass);

    void deleteByIdMainClass(Long idMainClass);
}