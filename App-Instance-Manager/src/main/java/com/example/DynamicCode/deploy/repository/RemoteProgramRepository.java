package com.example.DynamicCode.deploy.repository;

import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import com.example.DynamicCode.deploy.entity.RemoteProgramConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RemoteProgramRepository extends JpaRepository<RemoteProgramConfiguration, Long> {


    List<RemoteProgramConfiguration> findByIdSerwer(Long idSerwer);

    Optional<RemoteProgramConfiguration> findByIdCodeAndUploadStrategyType(Long idCode, UploadStrategyType uploadStrategyType);
    @Modifying
    void deleteByIdSerwer(Long idSerwer);
}