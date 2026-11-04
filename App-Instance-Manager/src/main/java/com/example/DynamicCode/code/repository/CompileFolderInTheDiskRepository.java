package com.example.DynamicCode.code.repository;

import com.example.DynamicCode.code.entity.CompiledFolderInTheDisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompileFolderInTheDiskRepository extends JpaRepository<CompiledFolderInTheDisk, Long> {

    Optional<CompiledFolderInTheDisk> findByCompiledCodeId(Long compiledCodeId);

    void deleteByCompiledCodeId(Long compiledCodeId);
}
