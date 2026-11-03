package com.example.DynamicCode.transfer.controller;

import com.example.DynamicCode.transfer.dto.DirectUploadCommand;
import com.example.DynamicCode.transfer.dto.DirectUploadPageResponse;
import com.example.DynamicCode.transfer.dto.DirectUploadResponse;
import com.example.DynamicCode.transfer.enums.DirectUploadState;
import com.example.DynamicCode.transfer.exception.DirectUploadException;
import com.example.DynamicCode.transfer.service.DirectUploadRecords;
import com.example.DynamicCode.transfer.service.DirectUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DirectUploadController.class)
class DirectUploadControllerTest {

    private static final DirectUploadResponse DONE = new DirectUploadResponse(1L, "a.txt", "/opt/a.txt", 3, "abc",
            DirectUploadState.DONE, null, Instant.now(), Instant.now());

    @Autowired
    private MockMvc mvc;
    @MockBean
    private DirectUploadService service;
    @MockBean
    private DirectUploadRecords records;

    @Test
    void shouldPassQueryAndContentLengthToService() throws Exception {
        DirectUploadCommand expected = new DirectUploadCommand("/opt", "a.txt", true, 3);
        when(service.upload(eq(1L), eq(expected), any())).thenReturn(DONE);

        mvc.perform(post("/api/transfers/servers/1/files").param("destination", "/opt").param("relativePath", "a.txt")
                        .param("overwrite", "true").contentType(MediaType.APPLICATION_OCTET_STREAM).content(new byte[]{1, 2, 3}))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sha256").value("abc"));
    }

    @Test
    void shouldMapUploadFailureToItsStatus() throws Exception {
        when(service.upload(eq(1L), any(), any())).thenThrow(new DirectUploadException(HttpStatus.CONFLICT, "Plik już istnieje"));

        mvc.perform(post("/api/transfers/servers/1/files").param("destination", "/opt").param("relativePath", "a.txt")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM).content(new byte[]{1}))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Plik już istnieje"));
    }

    @Test
    void shouldReturnHistoryWithoutFileContent() throws Exception {
        when(records.history(1L, 0, 50)).thenReturn(new DirectUploadPageResponse(List.of(DONE), 1));

        mvc.perform(get("/api/transfers/servers/1/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.entries[0].remotePath").value("/opt/a.txt"));
    }
}
