package com.example.DynamicCode.deploy.strategy;

import com.example.DynamicCode.code.enums.LanguageType;
import com.example.DynamicCode.deploy.exception.UnsafeFileNameException;
import com.example.DynamicCode.process.entity.RemoteExecutable;
import com.example.DynamicCode.process.service.RemoteExecutableService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteExecutableUploadStrategyTest {

    @Mock
    private RemoteExecutableService remoteExecutableService;
    @InjectMocks
    private RemoteExecutableUploadStrategy strategy;

    @Test
    void shouldReturnRawBytesWhenFileIsAJar() {
        byte[] jar = {0x50, 0x4B, 0x03, 0x04, (byte) 0x9F, (byte) 0xE2, 0x00};
        RemoteExecutable executable = executable("app.jar", jar);
        when(remoteExecutableService.getAllFilesFromMainClass(5L)).thenReturn(List.of(executable));
        when(remoteExecutableService.hasSafeName(executable)).thenReturn(true);

        Map<String, byte[]> files = strategy.resolveFiles(5L);

        assertThat(files.get("app.jar")).isEqualTo(jar);
    }

    @Test
    void shouldFailWhenStoredNameIsUnsafe() {
        RemoteExecutable executable = executable("../x", new byte[]{1});
        when(remoteExecutableService.getAllFilesFromMainClass(5L)).thenReturn(List.of(executable));
        when(remoteExecutableService.hasSafeName(executable)).thenReturn(false);

        assertThatThrownBy(() -> strategy.resolveFiles(5L)).isInstanceOf(UnsafeFileNameException.class);
    }

    private static RemoteExecutable executable(String name, byte[] contents) {
        RemoteExecutable executable = new RemoteExecutable();
        executable.setId(5L);
        executable.setName(name);
        executable.setContents(contents);
        executable.setLanguage(LanguageType.UNKNOWN);
        return executable;
    }
}
