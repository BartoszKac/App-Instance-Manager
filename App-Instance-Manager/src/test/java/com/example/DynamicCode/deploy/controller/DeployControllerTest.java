package com.example.DynamicCode.deploy.controller;

import com.example.DynamicCode.code.enums.LanguageType;
import com.example.DynamicCode.deploy.dto.DeployableFilesResponse;
import com.example.DynamicCode.deploy.dto.DeployableFilesResponse.ProjectFile;
import com.example.DynamicCode.deploy.dto.DeployableFilesResponse.UploadedFile;
import com.example.DynamicCode.deploy.dto.TransferResult;
import com.example.DynamicCode.deploy.service.ProviderDeployService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeployController.class)
class DeployControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ProviderDeployService providerDeployService;

    @Test
    void shouldListDeployableFilesWithoutContents() throws Exception {
        when(providerDeployService.getDeployableFiles()).thenReturn(new DeployableFilesResponse(
                List.of(new ProjectFile(1L, "Main.java", LanguageType.JAVA, 2L)),
                List.of(new UploadedFile(5L, "app.bin", LanguageType.UNKNOWN, "Narzędzie", 2048L, Instant.parse("2026-11-05T12:00:00Z")))));

        mvc.perform(get("/api/deploy/deployable-files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects[0].mainClassId").value(1))
                .andExpect(jsonPath("$.projects[0].compiledFileCount").value(2))
                .andExpect(jsonPath("$.uploads[0].sizeBytes").value(2048))
                .andExpect(jsonPath("$.uploads[0].uploadedAt").value("2026-11-05T12:00:00Z"))
                .andExpect(content().string(not(containsString("contents"))))
                .andExpect(content().string(not(containsString("\"code\""))));
    }

    @Test
    void shouldReturnOneResultPerTaskWhenTransferPartlyFails() throws Exception {
        when(providerDeployService.executeEnvironmentUploads(anyList())).thenReturn(List.of(
                new TransferResult(9L, 1L, false, "Nie udało się połączyć z serwerem."),
                new TransferResult(9L, 2L, true, "Wysłano")));
        String body = "[{\"mainClassId\":9,\"configurationId\":1,\"uploadStrategyType\":\"SOURCE_CODE\"},"
                + "{\"mainClassId\":9,\"configurationId\":2,\"uploadStrategyType\":\"SOURCE_CODE\"}]";

        mvc.perform(post("/api/deploy/transfer/bulk").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].success").value(false))
                .andExpect(jsonPath("$[1].success").value(true));
    }
}
