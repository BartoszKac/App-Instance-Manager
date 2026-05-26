package com.example.DynamicCode.repository.code;



import com.example.DynamicCode.model.entity.code.SourceCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;


@Repository
public interface CodeRepository extends JpaRepository<SourceCode, Long> {

    List<SourceCode> findByIdManClass(Long idManClass);

    @Modifying // Wymagane dla operacji typu DELETE/UPDATE pisanych z ręki
    void deleteByIdManClass(Long idManClass);
}
