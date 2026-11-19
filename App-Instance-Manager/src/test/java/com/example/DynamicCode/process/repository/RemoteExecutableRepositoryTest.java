package com.example.DynamicCode.process.repository;

import com.example.DynamicCode.code.enums.LanguageType;
import com.example.DynamicCode.process.dto.RemoteExecutableSummary;
import com.example.DynamicCode.process.entity.RemoteExecutable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RemoteExecutableRepositoryTest {

    @Autowired
    private RemoteExecutableRepository repository;

    @Test
    void shouldReadSummariesWhenRowsHaveAndLackMetadata() {
        Instant uploadedAt = Instant.parse("2026-11-05T12:00:00Z");
        RemoteExecutable described = repository.save(new RemoteExecutable(null, "app.bin", new byte[]{1, 2}, LanguageType.UNKNOWN, "Narzędzie", 2L, uploadedAt));
        RemoteExecutable legacy = repository.save(new RemoteExecutable(null, "old.sh", new byte[]{3}, LanguageType.BASH, null, null, null));

        List<RemoteExecutableSummary> summaries = repository.findAllSummaries();

        assertThat(summaries).containsExactly(
                new RemoteExecutableSummary(described.getId(), "app.bin", LanguageType.UNKNOWN, "Narzędzie", 2L, uploadedAt),
                new RemoteExecutableSummary(legacy.getId(), "old.sh", LanguageType.BASH, null, null, null));
    }
}
