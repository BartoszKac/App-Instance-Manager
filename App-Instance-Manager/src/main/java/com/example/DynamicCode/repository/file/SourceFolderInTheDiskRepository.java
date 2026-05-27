package com.example.DynamicCode.repository.file;

import com.example.DynamicCode.model.entity.file.SourceFolderInTheDisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface SourceFolderInTheDiskRepository extends JpaRepository<SourceFolderInTheDisk, Long> {

    SourceFolderInTheDisk findByPath(String path);

    void deleteByPath(String path);

    SourceFolderInTheDisk findBySourceCodeId(Long sourceCodeId);

    void deleteBySourceCodeId(Long sourceCodeId);
}