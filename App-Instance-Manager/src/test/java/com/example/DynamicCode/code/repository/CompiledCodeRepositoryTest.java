package com.example.DynamicCode.code.repository;

import com.example.DynamicCode.code.dto.CompiledFileCount;
import com.example.DynamicCode.code.entity.CompiledCode;
import com.example.DynamicCode.code.enums.LanguageType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CompiledCodeRepositoryTest {

    @Autowired
    private CompiledCodeRepository repository;

    @Test
    void shouldCountCompiledFilesPerProjectWhenSeveralProjectsExist() {
        repository.save(new CompiledCode(null, "Main.class", new byte[]{1}, LanguageType.JAVA, 10L));
        repository.save(new CompiledCode(null, "Helper.class", new byte[]{2}, LanguageType.JAVA, 10L));
        repository.save(new CompiledCode(null, "App.class", new byte[]{3}, LanguageType.JAVA, 20L));
        repository.save(new CompiledCode(null, "Broken.class", null, LanguageType.JAVA, 30L));

        assertThat(repository.countFilesPerProject())
                .containsExactlyInAnyOrder(new CompiledFileCount(10L, 2L), new CompiledFileCount(20L, 1L));
    }
}
