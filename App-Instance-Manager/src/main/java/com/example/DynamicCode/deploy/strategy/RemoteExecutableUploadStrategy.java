package com.example.DynamicCode.deploy.strategy;

import com.example.DynamicCode.deploy.enums.UploadStrategyType;
import com.example.DynamicCode.deploy.exception.UnsafeFileNameException;
import com.example.DynamicCode.process.entity.RemoteExecutable;
import com.example.DynamicCode.process.service.RemoteExecutableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RemoteExecutableUploadStrategy implements FileUploadStrategy {

    private final RemoteExecutableService remoteExecutableService;

    @Override
    public Map<String, byte[]> resolveFiles(Long executableId) {
        List<RemoteExecutable> executables = remoteExecutableService.getAllFilesFromMainClass(executableId);
        executables.forEach(this::requireSafeName);
        return executables.stream()
                .filter(executable -> Objects.nonNull(executable.getContents()))
                .collect(Collectors.toMap(RemoteExecutable::getName, RemoteExecutable::getContents, (first, second) -> second));
    }

    @Override
    public UploadStrategyType getType() {
        return UploadStrategyType.REMOTE_EXECUTABLE;
    }

    private void requireSafeName(RemoteExecutable executable) {
        if (!remoteExecutableService.hasSafeName(executable)) {
            throw new UnsafeFileNameException(executable.getId());
        }
    }
}
