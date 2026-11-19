package com.example.DynamicCode.process.repository;

import com.example.DynamicCode.process.dto.RemoteExecutableSummary;
import com.example.DynamicCode.process.entity.RemoteExecutable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RemoteExecutableRepository extends JpaRepository<RemoteExecutable, Long> {

    @Query("SELECT new com.example.DynamicCode.process.dto.RemoteExecutableSummary(r.id, r.name, r.language, r.description, r.sizeBytes, r.uploadedAt) FROM RemoteExecutable r ORDER BY r.id")
    List<RemoteExecutableSummary> findAllSummaries();
}
