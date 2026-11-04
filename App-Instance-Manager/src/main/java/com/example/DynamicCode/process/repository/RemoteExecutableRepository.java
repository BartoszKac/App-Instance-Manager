package com.example.DynamicCode.process.repository;

import com.example.DynamicCode.process.entity.RemoteExecutable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemoteExecutableRepository extends JpaRepository<RemoteExecutable, Long> {
}
