package com.example.inspector.inspection;

import com.example.inspection.grpc.v1.TargetPlatform;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformTest {

    private static final int CMD_EXE_LIMIT = 8191;

    @Test
    void shouldKeepWindowsCommandsWithinCmdExeLimit() {
        assertThat(Platform.WINDOWS.factsCommand().length()).isLessThan(CMD_EXE_LIMIT);
        assertThat(Platform.WINDOWS.filesCommand().length()).isLessThan(CMD_EXE_LIMIT);
    }

    @Test
    void shouldNeverUseSudoOrReadFileContentsInScripts() {
        for (Platform platform : Platform.values()) {
            assertThat(platform.filesCommand()).doesNotContain("sudo");
        }
        assertThat(Platform.POSIX.factsCommand()).doesNotContain("sudo").doesNotContain("/etc/shadow").doesNotContain("args");
    }

    @Test
    void shouldTryPreferredPlatformFirst() {
        assertThat(Platform.orderFor(TargetPlatform.TARGET_PLATFORM_WINDOWS)).containsExactly(Platform.WINDOWS, Platform.POSIX);
        assertThat(Platform.orderFor(TargetPlatform.TARGET_PLATFORM_POSIX)).containsExactly(Platform.POSIX, Platform.WINDOWS);
    }
}
