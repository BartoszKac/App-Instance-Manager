package com.example.inspector.inspection;

import com.example.inspection.grpc.v1.FileEntry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FileRecordParserTest {

    @Test
    void shouldParseGnuFindRecord() {
        FileEntry entry = FileRecordParser.parse("f\t120\troot\tadm\t640\t1700000000.5\t/etc/ssh/sshd_config", Platform.POSIX).orElseThrow();

        assertThat(entry.getType()).isEqualTo("f");
        assertThat(entry.getSizeBytes()).isEqualTo(120);
        assertThat(entry.getOwner()).isEqualTo("root");
        assertThat(entry.getOwnerGroup()).isEqualTo("adm");
        assertThat(entry.getMode()).isEqualTo("640");
        assertThat(entry.getModifiedEpochSeconds()).isEqualTo(1700000000L);
        assertThat(entry.getParent()).isEqualTo("/etc/ssh");
        assertThat(entry.getName()).isEqualTo("sshd_config");
    }

    @Test
    void shouldNormalizeStatFileTypeWhenFindHasNoPrintf() {
        assertThat(FileRecordParser.parse("-rw-r--r--\t1\troot\troot\t644\t1\t/a", Platform.POSIX).orElseThrow().getType()).isEqualTo("f");
        assertThat(FileRecordParser.parse("drwxr-xr-x\t1\troot\troot\t755\t1\t/a", Platform.POSIX).orElseThrow().getType()).isEqualTo("d");
    }

    @Test
    void shouldKeepTabsInsidePath() {
        FileEntry entry = FileRecordParser.parse("f\t1\tu\tg\t644\t1\t/tmp/a\tb", Platform.POSIX).orElseThrow();

        assertThat(entry.getPath()).isEqualTo("/tmp/a\tb");
    }

    @Test
    void shouldRejectMalformedRecordWhenFieldsAreMissing() {
        assertThat(FileRecordParser.parse("garbage", Platform.POSIX)).isEmpty();
        assertThat(FileRecordParser.parse("f\t1\tu\tg\t644\t1\t", Platform.POSIX)).isEmpty();
    }

    @Test
    void shouldTreatNonNumericSizeAsZero() {
        assertThat(FileRecordParser.parse("f\tx\tu\tg\t644\ty\t/a", Platform.POSIX).orElseThrow().getSizeBytes()).isZero();
    }

    @Test
    void shouldResolveWindowsParents() {
        assertThat(Platform.WINDOWS.parentOf("C:\\")).isEmpty();
        assertThat(Platform.WINDOWS.parentOf("C:\\Users")).isEqualTo("C:\\");
        assertThat(Platform.WINDOWS.parentOf("C:\\Users\\me")).isEqualTo("C:\\Users");
        assertThat(Platform.WINDOWS.nameOf("C:\\Users\\me")).isEqualTo("me");
        assertThat(Platform.WINDOWS.nameOf("C:\\")).isEqualTo("C:\\");
    }

    @Test
    void shouldResolvePosixParents() {
        assertThat(Platform.POSIX.parentOf("/")).isEmpty();
        assertThat(Platform.POSIX.parentOf("/etc")).isEqualTo("/");
        assertThat(Platform.POSIX.parentOf("/etc/ssh")).isEqualTo("/etc");
        assertThat(Platform.POSIX.nameOf("/")).isEqualTo("/");
    }
}
