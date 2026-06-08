package com.example.DynamicCode.repository.file;

import com.example.DynamicCode.model.entity.file.SourceFolderInTheDisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface SourceFolderInTheDiskRepository extends JpaRepository<SourceFolderInTheDisk, Long> {

    SourceFolderInTheDisk findByPath(String path);

    void deleteByPath(String path);

    @Query("SELECT s FROM SourceFolderInTheDisk s WHERE s.sourceCodeId = :sourceCodeId")
    SourceFolderInTheDisk findBySourceCodeId(@Param("sourceCodeId") Long sourceCodeId);

    void deleteBySourceCodeId(Long sourceCodeId);
}