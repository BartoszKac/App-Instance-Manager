package com.example.DynamicCode.databaservice.deploy;


import com.example.DynamicCode.constants.code.LanguageType;
import com.example.DynamicCode.model.entity.deploy.RemoteProgramConfiguration;
import com.example.DynamicCode.repository.deploy.RemoteProgramRepository;
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
                null, 100L, LanguageType.JAVA, "/usr/bin/java", 1L
        );
        RemoteProgramConfiguration config2 = new RemoteProgramConfiguration(
                null, 100L, LanguageType.PYTHON, "/usr/bin/python3", 2L
        );
        RemoteProgramConfiguration config3 = new RemoteProgramConfiguration(
                null, 200L, LanguageType.BASH, "/bin/bash", 3L
        );

        remoteProgramRepository.save(config1);
        remoteProgramRepository.save(config2);
        remoteProgramRepository.save(config3);

        // WHEN
        List<RemoteProgramConfiguration> result = remoteProgramRepository.findByIdSerwer(100L);

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RemoteProgramConfiguration::getLanguage)
                .containsExactlyInAnyOrder(LanguageType.JAVA, LanguageType.PYTHON);
    }

    @Test
    @Transactional
    void shouldDeleteConfigurationsByServerId() {
        RemoteProgramConfiguration config = new RemoteProgramConfiguration(
                null, 500L, LanguageType.CPP, "/usr/bin/g++", 4L
        );
        remoteProgramRepository.save(config);

        remoteProgramRepository.flush();

        remoteProgramRepository.deleteByIdSerwer(500L);
        remoteProgramRepository.flush();

        List<RemoteProgramConfiguration> result = remoteProgramRepository.findByIdSerwer(500L);

        assertThat(result).isEmpty();
    }
}