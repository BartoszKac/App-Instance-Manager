package com.example.DynamicCode.repository.file;

import com.example.DynamicCode.model.entity.file.CompiledFolderInTheDisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompileFolderInTheDiskRepository extends JpaRepository<CompiledFolderInTheDisk, Long> {

    Optional<CompiledFolderInTheDisk> findByCompiledCodeId(Long compiledCodeId);

    void deleteByCompiledCodeId(Long compiledCodeId);
}
