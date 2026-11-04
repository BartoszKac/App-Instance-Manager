package com.example.inspector.ssh;

import com.jcraft.jsch.HostKeyRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PinnedHostKeysTest {

    private static final byte[] KEY = {1, 2, 3};
    private static final byte[] OTHER_KEY = {4, 5, 6};

    @Test
    void shouldTrustAndReportKeyOnFirstUse() {
        PinnedHostKeys keys = new PinnedHostKeys("");

        assertThat(keys.check("host", KEY)).isEqualTo(HostKeyRepository.OK);
        assertThat(keys.seenKey()).startsWith("SHA256:");
    }

    @Test
    void shouldAcceptPinnedKey() {
        PinnedHostKeys first = new PinnedHostKeys("");
        first.check("host", KEY);

        PinnedHostKeys pinned = new PinnedHostKeys(first.seenKey());

        assertThat(pinned.check("host", KEY)).isEqualTo(HostKeyRepository.OK);
        assertThat(pinned.isMismatch()).isFalse();
    }

    @Test
    void shouldRejectChangedKey() {
        PinnedHostKeys first = new PinnedHostKeys("");
        first.check("host", KEY);

        PinnedHostKeys pinned = new PinnedHostKeys(first.seenKey());

        assertThat(pinned.check("host", OTHER_KEY)).isEqualTo(HostKeyRepository.CHANGED);
        assertThat(pinned.isMismatch()).isTrue();
    }
}
