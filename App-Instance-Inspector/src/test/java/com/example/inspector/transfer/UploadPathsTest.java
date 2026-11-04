package com.example.inspector.transfer;

import com.example.inspector.inspection.InspectionFailure;
import io.grpc.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadPathsTest {

    @Test
    void shouldResolveFileInsideNestedFolder() {
        UploadPaths paths = UploadPaths.resolve("/opt/app/", "site/css/a.css");

        assertThat(paths.target()).isEqualTo("/opt/app/site/css/a.css");
        assertThat(paths.directory()).isEqualTo("/opt/app/site/css");
        assertThat(paths.directoryChain()).containsExactly("/opt", "/opt/app", "/opt/app/site", "/opt/app/site/css");
    }

    @Test
    void shouldResolveFileDirectlyInRoot() {
        UploadPaths paths = UploadPaths.resolve("/", "a.txt");

        assertThat(paths.target()).isEqualTo("/a.txt");
        assertThat(paths.directoryChain()).isEmpty();
    }

    @Test
    void shouldKeepSpacesAndShellCharactersAsPlainNames() {
        assertThat(UploadPaths.resolve("/srv", "a b;$(x).txt").target()).isEqualTo("/srv/a b;$(x).txt");
    }

    @ParameterizedTest
    @CsvSource({
            "relative/dir, a.txt",
            "/opt/../etc, a.txt",
            "/opt, ../a.txt",
            "/opt, a/../../b.txt",
            "/opt, /etc/passwd",
            "/opt, a//b.txt",
            "/opt, a/./b.txt"
    })
    void shouldRejectPathsLeavingDestination(String destination, String relative) {
        assertThatThrownBy(() -> UploadPaths.resolve(destination, relative))
                .isInstanceOfSatisfying(InspectionFailure.class,
                        e -> assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT));
    }

    @Test
    void shouldRejectControlCharactersAndEmptyName() {
        assertThatThrownBy(() -> UploadPaths.resolve("/opt", "a\nb.txt")).isInstanceOf(InspectionFailure.class);
        assertThatThrownBy(() -> UploadPaths.resolve("/opt", "")).isInstanceOf(InspectionFailure.class);
    }
}
