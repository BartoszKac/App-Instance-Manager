package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.TransferResult;
import com.example.DynamicCode.deploy.dto.TransferTask;
import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.deploy.enums.OperationSystem;
import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import com.example.DynamicCode.deploy.exception.NoFilesToTransferException;
import com.jcraft.jsch.JSchException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnviromentTransferServiceTest {

    @Mock
    private SshFileTransferService sshFileTransferService;
    @Mock
    private RemoteDirectoryService remoteDirectoryService;
    @Mock
    private RemoteSerwerService remoteSerwerService;
    @Mock
    private RemoteProgramService remoteProgramService;
    @InjectMocks
    private EnviromentTransferService service;

    @Test
    void shouldReportEachServerWhenOneFails() throws Exception {
        when(remoteSerwerService.getConfigById(1L)).thenReturn(server(1L, "10.0.0.1"));
        when(remoteSerwerService.getConfigById(2L)).thenReturn(server(2L, "10.0.0.2"));
        when(remoteDirectoryService.getRemoteDirectoryPath(OperationSystem.LINUX)).thenReturn("test/");
        doThrow(new JSchException("Auth fail for 10.0.0.1")).when(sshFileTransferService)
                .uploadWithStrategy(eq(new RemoteTarget("10.0.0.1", "u", "p", "test/")), any(), any());

        List<TransferResult> results = service.initiateUpload(List.of(
                new TransferTask(9L, 1L, UploadStrategyType.SOURCE_CODE),
                new TransferTask(9L, 2L, UploadStrategyType.SOURCE_CODE)));

        assertThat(results).extracting(TransferResult::configurationId, TransferResult::success)
                .containsExactly(tuple(1L, false), tuple(2L, true));
        assertThat(results.get(0).message()).doesNotContain("10.0.0.1").contains("połączyć");
        verify(remoteProgramService, times(1)).saveAllConfigurationsToDb(anyList());
    }

    @Test
    void shouldNotSaveConfigurationWhenNothingWasSent() throws Exception {
        when(remoteSerwerService.getConfigById(1L)).thenReturn(server(1L, "10.0.0.1"));
        when(remoteDirectoryService.getRemoteDirectoryPath(OperationSystem.LINUX)).thenReturn("test/");
        doThrow(new NoFilesToTransferException(9L, UploadStrategyType.COMPILED_CODE)).when(sshFileTransferService)
                .uploadWithStrategy(any(), eq(UploadStrategyType.COMPILED_CODE), eq(9L));

        List<TransferResult> results = service.initiateUpload(List.of(new TransferTask(9L, 1L, UploadStrategyType.COMPILED_CODE)));

        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result.success()).isFalse();
            assertThat(result.message()).startsWith("Brak plików");
        });
        verify(remoteProgramService, never()).saveAllConfigurationsToDb(anyList());
    }

    private static RemoteSerwerConfiguration server(Long id, String ip) {
        RemoteSerwerConfiguration server = new RemoteSerwerConfiguration();
        server.setIdConfiguration(id);
        server.setIp(ip);
        server.setUser("u");
        server.setPass("p");
        server.setOperationSystem(OperationSystem.LINUX);
        return server;
    }
}
