package com.example.DynamicCode.process.service;

import com.example.DynamicCode.code.enums.LanguageType;
import com.example.DynamicCode.process.entity.RemoteExecutable;
import com.example.DynamicCode.process.exception.InvalidExecutableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SavingProcessInSystemServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-06T10:00:00Z");
    private static final Instant EARLIER = Instant.parse("2026-10-01T08:00:00Z");

    @Mock
    private RemoteExecutableService remoteExecutableService;

    private SavingProcessInSystemService service;

    @BeforeEach
    void setUp() {
        service = new SavingProcessInSystemService(remoteExecutableService, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", ".", "..", "../../x", "a/b", "a\\b", "-rf", "bad\u0000name", "line\nbreak"})
    void shouldRejectNameWhenItIsUnsafe(String name) {
        assertThatThrownBy(() -> service.save(executable(name, null)))
                .isInstanceOf(InvalidExecutableException.class);
        verify(remoteExecutableService, never()).saveAllExecutablesToDb(anyList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"app.bin", "mój skrypt.sh", "zażółć.py", "a-b", ".env"})
    void shouldAcceptNameWhenItIsSafe(String name) {
        service.save(executable(name, null));

        verify(remoteExecutableService).saveAllExecutablesToDb(anyList());
    }

    @Test
    void shouldRejectDescriptionWhenLongerThanLimit() {
        assertThatThrownBy(() -> service.save(executable("app.bin", "x".repeat(201))))
                .isInstanceOf(InvalidExecutableException.class);
    }

    @Test
    void shouldSetSizeAndTimeWhenFileIsNew() {
        RemoteExecutable executable = executable("app.bin", "Narzędzie");

        service.save(executable);

        assertThat(executable.getSizeBytes()).isEqualTo(3L);
        assertThat(executable.getUploadedAt()).isEqualTo(NOW);
    }

    @Test
    void shouldKeepStoredTimeWhenClientSendsAnotherOnUpdate() {
        RemoteExecutable stored = executable("app.bin", null);
        stored.setUploadedAt(EARLIER);
        when(remoteExecutableService.findExecutable(4L)).thenReturn(Optional.of(stored));
        RemoteExecutable incoming = executable("app.bin", null);
        incoming.setSizeBytes(999_999L);
        incoming.setUploadedAt(Instant.parse("2030-01-01T00:00:00Z"));

        service.update(4L, incoming);

        assertThat(incoming.getId()).isEqualTo(4L);
        assertThat(incoming.getUploadedAt()).isEqualTo(EARLIER);
        assertThat(incoming.getSizeBytes()).isEqualTo(3L);
        verify(remoteExecutableService).saveAllExecutablesToDb(List.of(incoming));
    }

    private static RemoteExecutable executable(String name, String description) {
        RemoteExecutable executable = new RemoteExecutable();
        executable.setName(name);
        executable.setContents(new byte[]{1, 2, 3});
        executable.setLanguage(LanguageType.UNKNOWN);
        executable.setDescription(description);
        return executable;
    }
}
