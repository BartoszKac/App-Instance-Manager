package com.example.DynamicCode.code.service;

import com.example.DynamicCode.shared.notification.FrontendNotificationService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
class LanguageProcessTest {

    private final LanguageProcess languageProcess =
            new LanguageProcess(Mockito.mock(FrontendNotificationService.class));

    private String javaBinary() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }

    @Test
    void shouldCaptureStandardOutputWhenProcessSucceeds() throws Exception {
        String output = languageProcess.captureOutput(List.of(javaBinary(), "--version"), Duration.ofSeconds(30));

        assertThat(output).isNotBlank();
    }

    @Test
    void shouldFailWhenProcessExitsWithError() {
        assertThatThrownBy(() -> languageProcess.captureOutput(
                List.of(javaBinary(), "--no-such-option"), Duration.ofSeconds(30)))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("kodem");
    }

    @Test
    void shouldFailWhenProcessExceedsTimeout() {
        assertThatThrownBy(() -> languageProcess.captureOutput(
                List.of(javaBinary(), "--version"), Duration.ofMillis(1)))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("limit czasu");
    }

    @Test
    void shouldFailWhenBinaryDoesNotExist() {
        assertThatThrownBy(() -> languageProcess.captureOutput(
                List.of("no-such-binary-aim"), Duration.ofSeconds(5)))
                .isInstanceOf(IOException.class);
    }
}
