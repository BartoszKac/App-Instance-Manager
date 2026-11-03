package com.example.inspector.inspection;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DeniedPathsTest {

    @Test
    void shouldCollectDeniedPathsFromGnuAndBusyboxFind() throws IOException {
        DeniedPaths denied = new DeniedPaths();

        denied.write("find: '/root': Permission denied\n".getBytes(StandardCharsets.UTF_8));
        denied.write("find: ‘/var/x y’: Permission denied\n".getBytes(StandardCharsets.UTF_8));
        denied.write("find: /lost+found: Permission denied\n".getBytes(StandardCharsets.UTF_8));
        denied.write("find: '/a': No such file or directory\n".getBytes(StandardCharsets.UTF_8));

        assertThat(denied.paths()).containsExactly("/root", "/var/x y", "/lost+found");
    }
}
