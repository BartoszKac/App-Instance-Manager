package com.example.DynamicCode.code.repository;


import com.example.DynamicCode.code.entity.CompiledCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompiledCodeRepository extends JpaRepository<CompiledCode,Long>{

    List<CompiledCode> findByIdManClass(Long idManClass);

    void deleteByIdManClass(Long idManClass);
}
