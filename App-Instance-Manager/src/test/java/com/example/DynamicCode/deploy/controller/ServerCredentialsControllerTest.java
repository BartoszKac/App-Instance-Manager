package com.example.DynamicCode.deploy.controller;

import com.example.DynamicCode.deploy.dto.ServerCredentialsRequest;
import com.example.DynamicCode.deploy.exception.InvalidServerCredentialsException;
import com.example.DynamicCode.deploy.service.SerwerConfigurationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServerCredentialsController.class)
class ServerCredentialsControllerTest {

    private static final String BODY = "{\"user\":\"root\",\"password\":\"new\"}";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private SerwerConfigurationService service;

    @Test
    void shouldAnswerNoContentAfterSaving() throws Exception {
        mvc.perform(put("/api/deploy/server-credentials/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNoContent());

        verify(service).updateCredentials(1L, new ServerCredentialsRequest("root", "new"));
    }

    @Test
    void shouldAnswerBadRequestForInvalidCredentials() throws Exception {
        doThrow(new InvalidServerCredentialsException("Podaj")).when(service).updateCredentials(eq(1L), any());

        mvc.perform(put("/api/deploy/server-credentials/1").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAnswerNotFoundForMissingServer() throws Exception {
        doThrow(new IllegalArgumentException("missing")).when(service).updateCredentials(eq(9L), any());

        mvc.perform(put("/api/deploy/server-credentials/9").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
    }
}
