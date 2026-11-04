package com.example.DynamicCode.deploy.repository;


import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import com.example.DynamicCode.deploy.entity.RemoteProgramConfiguration;
import com.example.DynamicCode.deploy.repository.RemoteProgramRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RemoteProgramRepositoryTest {

    @Autowired
    private RemoteProgramRepository remoteProgramRepository;

    @Test
    void shouldSaveAndFindAllConfigurationsByServerId() {
        // GIVEN
        RemoteProgramConfiguration config1 = new RemoteProgramConfiguration(
                null, 100L, UploadStrategyType.COMPILED_CODE, "/opt/app/java", 1L
        );
        RemoteProgramConfiguration config2 = new RemoteProgramConfiguration(
                null, 100L, UploadStrategyType.SOURCE_CODE, "/opt/app/python", 2L
        );
        RemoteProgramConfiguration config3 = new RemoteProgramConfiguration(
                null, 200L, UploadStrategyType.REMOTE_EXECUTABLE, "/opt/app/bash", 3L
        );

        remoteProgramRepository.save(config1);
        remoteProgramRepository.save(config2);
        remoteProgramRepository.save(config3);

        // WHEN
        List<RemoteProgramConfiguration> result = remoteProgramRepository.findByIdSerwer(100L);

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RemoteProgramConfiguration::getUploadStrategyType)
                .containsExactlyInAnyOrder(UploadStrategyType.COMPILED_CODE, UploadStrategyType.SOURCE_CODE);
    }

    @Test
    @Transactional
    void shouldDeleteConfigurationsByServerId() {
        RemoteProgramConfiguration config = new RemoteProgramConfiguration(
                null, 500L, UploadStrategyType.SOURCE_CODE, "/opt/app/cpp", 4L
        );
        remoteProgramRepository.save(config);

        remoteProgramRepository.flush();

        remoteProgramRepository.deleteByIdSerwer(500L);
        remoteProgramRepository.flush();

        List<RemoteProgramConfiguration> result = remoteProgramRepository.findByIdSerwer(500L);

        assertThat(result).isEmpty();
    }
}