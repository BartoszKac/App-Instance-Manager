package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.InventorySection;
import com.example.DynamicCode.deploy.dto.ServerInventoryResponse;
import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.InventoryScript;
import com.example.DynamicCode.deploy.enums.OperationSystem;
import com.jcraft.jsch.JSchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerInventoryServiceTest {

    private static final String OUTPUT = "\n@@hostname\nserver\n\n@@os\nUbuntu 24.04\n\n@@virt\n\n@@unknown\nx\n";

    @Mock
    private RemoteSerwerService remoteSerwerService;

    @Mock
    private SshSessionFactory sshSessionFactory;

    @InjectMocks
    private ServerInventoryService service;

    private final RemoteSerwerConfiguration server =
            new RemoteSerwerConfiguration(1L, "Serwer", "192.168.1.67", "root", "sekret", OperationSystem.LINUX);

    @BeforeEach
    void setUp() {
        lenient().when(remoteSerwerService.getConfigById(1L)).thenReturn(server);
    }

    @Test
    void shouldReturnKnownNonEmptySectionsWithLabelsInOrder() throws Exception {
        when(sshSessionFactory.executeBounded(any(), any(), any(Duration.class))).thenReturn(OUTPUT);

        ServerInventoryResponse response = service.inventory(1L);

        assertThat(response.connected()).isTrue();
        assertThat(response.sections()).containsExactly(
                new InventorySection("hostname", "Nazwa", "server"),
                new InventorySection("os", "System", "Ubuntu 24.04"));
    }

    @Test
    void shouldNeverExposePassword() throws Exception {
        when(sshSessionFactory.executeBounded(any(), any(), any(Duration.class))).thenReturn(OUTPUT);

        assertThat(service.inventory(1L).toString()).doesNotContain("sekret");
        assertThat(new SshSessionFactory.SshTarget("ip", "user", "sekret").toString()).doesNotContain("sekret");
    }

    @Test
    void shouldTryWindowsScriptWhenPosixOutputHasNoMarkers() throws Exception {
        when(sshSessionFactory.executeBounded(any(), eq(InventoryScript.POSIX.command()), any(Duration.class))).thenReturn("not recognized");
        when(sshSessionFactory.executeBounded(any(), eq(InventoryScript.WINDOWS.command()), any(Duration.class))).thenReturn(OUTPUT);

        assertThat(service.inventory(1L).sections()).isNotEmpty();
    }

    @Test
    void shouldReportWhenConnectedButNoData() throws Exception {
        when(sshSessionFactory.executeBounded(any(), any(), any(Duration.class))).thenReturn("nic");

        ServerInventoryResponse response = service.inventory(1L);

        assertThat(response.connected()).isTrue();
        assertThat(response.error()).isNotBlank();
        assertThat(response.sections()).isEmpty();
    }

    @Test
    void shouldExplainWrongPassword() throws Exception {
        when(sshSessionFactory.executeBounded(any(), any(), any(Duration.class))).thenThrow(new JSchException("Auth fail"));

        ServerInventoryResponse response = service.inventory(1L);

        assertThat(response.connected()).isFalse();
        assertThat(response.error()).contains("login lub hasło");
    }

    @Test
    void shouldExplainConnectionFailureWithoutTechnicalDetails() throws Exception {
        when(sshSessionFactory.executeBounded(any(), any(), any(Duration.class))).thenThrow(new JSchException("timeout: socket is not established"));

        ServerInventoryResponse response = service.inventory(1L);

        assertThat(response.error()).contains("połączyć").doesNotContain("socket");
    }

    @Test
    void shouldUseWindowsScriptFirstForWindowsServer() {
        assertThat(InventoryScript.orderFor(OperationSystem.WINDOWS)).containsExactly(InventoryScript.WINDOWS, InventoryScript.POSIX);
        assertThat(InventoryScript.orderFor(null)).containsExactly(InventoryScript.POSIX, InventoryScript.WINDOWS);
        assertThat(InventoryScript.WINDOWS.command()).startsWith("powershell -NoProfile -NonInteractive -EncodedCommand ");
        assertThat(List.of(InventoryScript.POSIX.command())).allSatisfy(command -> assertThat(command).contains("section hostname"));
    }
}
