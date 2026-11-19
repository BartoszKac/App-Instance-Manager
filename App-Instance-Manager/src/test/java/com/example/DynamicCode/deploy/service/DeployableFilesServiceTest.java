package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.code.dto.CompiledFileCount;
import com.example.DynamicCode.code.entity.SourceCode;
import com.example.DynamicCode.code.enums.LanguageType;
import com.example.DynamicCode.code.service.CompiledCodeService;
import com.example.DynamicCode.code.service.SourceCodeService;
import com.example.DynamicCode.deploy.dto.DeployableFilesResponse;
import com.example.DynamicCode.deploy.dto.DeployableFilesResponse.ProjectFile;
import com.example.DynamicCode.deploy.dto.DeployableFilesResponse.UploadedFile;
import com.example.DynamicCode.process.dto.RemoteExecutableSummary;
import com.example.DynamicCode.process.service.RemoteExecutableService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeployableFilesServiceTest {

    @Mock
    private SourceCodeService sourceCodeService;
    @Mock
    private CompiledCodeService compiledCodeService;
    @Mock
    private RemoteExecutableService remoteExecutableService;
    @InjectMocks
    private DeployableFilesService service;

    @Test
    void shouldJoinCompiledCountsWhenProjectWasCompiled() {
        when(sourceCodeService.getAllMainClassesFromDb()).thenReturn(List.of(
                new SourceCode(1L, "Main.java", "class Main {}", LanguageType.JAVA, 1L),
                new SourceCode(4L, "run.py", "print()", LanguageType.PYTHON, 4L)));
        when(compiledCodeService.countFilesPerProject()).thenReturn(List.of(new CompiledFileCount(1L, 3L)));
        when(remoteExecutableService.getSummaries()).thenReturn(List.of(
                new RemoteExecutableSummary(7L, "old.sh", LanguageType.BASH, null, null, null)));

        DeployableFilesResponse response = service.getDeployableFiles();

        assertThat(response.projects()).containsExactly(
                new ProjectFile(1L, "Main.java", LanguageType.JAVA, 3L),
                new ProjectFile(4L, "run.py", LanguageType.PYTHON, 0L));
        assertThat(response.uploads()).containsExactly(new UploadedFile(7L, "old.sh", LanguageType.BASH, null, null, null));
    }
}
