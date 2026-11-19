package com.example.DynamicCode.process.controller;

import com.example.DynamicCode.process.entity.RemoteExecutable;
import com.example.DynamicCode.process.service.RemoteExecutableService;
import com.example.DynamicCode.process.service.SavingProcessInSystemService;
import com.example.DynamicCode.shared.config.ClockConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProcessController.class)
@Import({SavingProcessInSystemService.class, ClockConfig.class})
class ProcessControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private RemoteExecutableService remoteExecutableService;

    @Test
    void shouldAnswerBadRequestWhenNameTraversesFolders() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "../../x", "application/octet-stream", new byte[]{1});

        mvc.perform(multipart("/api/process/upload").file(file).param("language", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verify(remoteExecutableService, never()).saveAllExecutablesToDb(anyList());
    }

    @Test
    void shouldAnswerBadRequestWhenDescriptionIsTooLong() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "app.bin", "application/octet-stream", new byte[]{1});

        mvc.perform(multipart("/api/process/upload").file(file).param("language", "UNKNOWN").param("description", "x".repeat(201)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldStoreDescriptionAndSizeWhenUploadIsValid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "zażółć.sh", "application/octet-stream", new byte[]{1, 2});

        mvc.perform(multipart("/api/process/upload").file(file).param("language", "BASH").param("description", " Kopia bazy "))
                .andExpect(status().isCreated());

        ArgumentCaptor<List<RemoteExecutable>> saved = ArgumentCaptor.forClass(List.class);
        verify(remoteExecutableService).saveAllExecutablesToDb(saved.capture());
        RemoteExecutable executable = saved.getValue().get(0);
        assertThat(executable.getName()).isEqualTo("zażółć.sh");
        assertThat(executable.getDescription()).isEqualTo("Kopia bazy");
        assertThat(executable.getSizeBytes()).isEqualTo(2L);
        assertThat(executable.getUploadedAt()).isNotNull();
    }
}
