package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.code.dto.CompiledFileCount;
import com.example.DynamicCode.code.entity.SourceCode;
import com.example.DynamicCode.code.service.CompiledCodeService;
import com.example.DynamicCode.code.service.SourceCodeService;
import com.example.DynamicCode.deploy.dto.DeployableFilesResponse;
import com.example.DynamicCode.deploy.dto.DeployableFilesResponse.ProjectFile;
import com.example.DynamicCode.deploy.dto.DeployableFilesResponse.UploadedFile;
import com.example.DynamicCode.process.dto.RemoteExecutableSummary;
import com.example.DynamicCode.process.service.RemoteExecutableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeployableFilesService {

    private final SourceCodeService sourceCodeService;
    private final CompiledCodeService compiledCodeService;
    private final RemoteExecutableService remoteExecutableService;

    public DeployableFilesResponse getDeployableFiles() {
        Map<Long, Long> compiledCounts = compiledCodeService.countFilesPerProject().stream()
                .collect(Collectors.toMap(CompiledFileCount::idManClass, CompiledFileCount::count, Long::sum));
        List<ProjectFile> projects = sourceCodeService.getAllMainClassesFromDb().stream()
                .map(main -> toProject(main, compiledCounts))
                .toList();
        List<UploadedFile> uploads = remoteExecutableService.getSummaries().stream()
                .map(DeployableFilesService::toUpload)
                .toList();
        return new DeployableFilesResponse(projects, uploads);
    }

    private static ProjectFile toProject(SourceCode main, Map<Long, Long> compiledCounts) {
        return new ProjectFile(main.getIdManClass(), main.getName(), main.getLanguage(), compiledCounts.getOrDefault(main.getIdManClass(), 0L));
    }

    private static UploadedFile toUpload(RemoteExecutableSummary summary) {
        return new UploadedFile(summary.id(), summary.name(), summary.language(), summary.description(), summary.sizeBytes(), summary.uploadedAt());
    }
}
