package com.example.DynamicCode.transfer.repository;

import com.example.DynamicCode.transfer.entity.DirectUploadRecord;
import com.example.DynamicCode.transfer.enums.DirectUploadState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface DirectUploadRecordRepository extends JpaRepository<DirectUploadRecord, Long> {

    Page<DirectUploadRecord> findByServerIdOrderByIdDesc(Long serverId, Pageable pageable);

    @Transactional
    void deleteByServerId(Long serverId);

    @Transactional
    @Modifying
    @Query("update DirectUploadRecord r set r.state = :failed, r.error = :error, r.finishedAt = :now where r.state = :active")
    int failAllInState(@Param("active") DirectUploadState active, @Param("failed") DirectUploadState failed,
                       @Param("error") String error, @Param("now") Instant now);
}
