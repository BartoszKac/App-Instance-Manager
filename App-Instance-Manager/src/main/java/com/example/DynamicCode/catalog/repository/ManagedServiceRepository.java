package com.example.DynamicCode.catalog.repository;

import com.example.DynamicCode.catalog.entity.ManagedService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagedServiceRepository extends JpaRepository<ManagedService, Long> {
}
