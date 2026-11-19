package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import com.example.DynamicCode.deploy.exception.NoFilesToTransferException;
import com.example.DynamicCode.deploy.strategy.FileUploadStrategy;
import com.example.DynamicCode.deploy.strategy.FileUploadStrategyFactory;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SshFileTransferServiceTest {

    private static final RemoteTarget TARGET = new RemoteTarget("10.0.0.5", "deploy", "secret", "test/");

    @Mock
    private SshSessionFactory sshSessionFactory;
    @Mock
    private FileUploadStrategyFactory strategyFactory;
    @Mock
    private FileUploadStrategy strategy;
    @Mock
    private Session session;
    @Mock
    private ChannelSftp channel;

    private SshFileTransferService service;

    @BeforeEach
    void setUp() {
        service = new SshFileTransferService(sshSessionFactory, strategyFactory);
        when(strategyFactory.getStrategy(UploadStrategyType.REMOTE_EXECUTABLE)).thenReturn(strategy);
    }

    @Test
    void shouldSendBytesUnchangedWhenFileIsBinary() throws Exception {
        byte[] binary = {0x00, (byte) 0x80, (byte) 0xC3, (byte) 0xFF, 0x7F, 0x0A};
        when(strategy.resolveFiles(7L)).thenReturn(Map.of("app.bin", binary));
        when(sshSessionFactory.createSession("10.0.0.5", "deploy", "secret")).thenReturn(session);
        when(session.openChannel("sftp")).thenReturn(channel);
        ByteArrayOutputStream sent = new ByteArrayOutputStream();
        doAnswer(call -> ((InputStream) call.getArgument(0)).transferTo(sent)).when(channel).put(any(InputStream.class), anyString());

        service.uploadWithStrategy(TARGET, UploadStrategyType.REMOTE_EXECUTABLE, 7L);

        assertThat(sent.toByteArray()).isEqualTo(binary);
        verify(channel).put(any(InputStream.class), eq("test/app.bin"));
    }

    @Test
    void shouldThrowWhenThereIsNothingToSend() {
        when(strategy.resolveFiles(7L)).thenReturn(Map.of());

        assertThatThrownBy(() -> service.uploadWithStrategy(TARGET, UploadStrategyType.REMOTE_EXECUTABLE, 7L))
                .isInstanceOf(NoFilesToTransferException.class);
        verifyNoInteractions(sshSessionFactory);
    }
}
