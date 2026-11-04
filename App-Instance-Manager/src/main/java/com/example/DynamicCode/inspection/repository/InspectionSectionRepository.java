package com.example.DynamicCode.inspection.repository;

import com.example.DynamicCode.inspection.entity.InspectionSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InspectionSectionRepository extends JpaRepository<InspectionSection, Long> {

    List<InspectionSection> findByRunIdOrderById(Long runId);
}
