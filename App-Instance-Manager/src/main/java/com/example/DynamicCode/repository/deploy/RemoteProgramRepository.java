package com.example.DynamicCode.repository.deploy;

import com.example.DynamicCode.constants.deploy.UploadStrategyType;
import com.example.DynamicCode.model.entity.deploy.RemoteProgramConfiguration;
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