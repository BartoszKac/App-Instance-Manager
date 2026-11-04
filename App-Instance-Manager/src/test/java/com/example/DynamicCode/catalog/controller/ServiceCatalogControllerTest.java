package com.example.DynamicCode.catalog.controller;

import com.example.DynamicCode.catalog.dto.ManagedServiceRequest;
import com.example.DynamicCode.catalog.dto.ManagedServiceResponse;
import com.example.DynamicCode.catalog.exception.InvalidManagedServiceException;
import com.example.DynamicCode.catalog.exception.ManagedServiceNotFoundException;
import com.example.DynamicCode.catalog.service.ServiceCatalog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceCatalogController.class)
class ServiceCatalogControllerTest {

    private static final ManagedServiceResponse WIKI = new ManagedServiceResponse(1L, "Wiki", "wiki.lan", 80, null);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceCatalog serviceCatalog;

    private ResultActions postService() throws Exception {
        return mockMvc.perform(post("/api/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Wiki\",\"host\":\"wiki.lan\",\"port\":80}"));
    }

    @Test
    void shouldListServices() throws Exception {
        when(serviceCatalog.list()).thenReturn(List.of(WIKI));

        mockMvc.perform(get("/api/services")).andExpect(status().isOk()).andExpect(jsonPath("$[0].name").value("Wiki"));
    }

    @Test
    void shouldReturnCreatedWhenServiceIsSaved() throws Exception {
        when(serviceCatalog.create(any(ManagedServiceRequest.class))).thenReturn(WIKI);

        postService().andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenServiceIsInvalid() throws Exception {
        when(serviceCatalog.create(any(ManagedServiceRequest.class))).thenThrow(new InvalidManagedServiceException("zły port"));

        postService().andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("zły port"));
    }

    @Test
    void shouldReturnNoContentWhenDeleted() throws Exception {
        mockMvc.perform(delete("/api/services/1")).andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingService() throws Exception {
        doThrow(new ManagedServiceNotFoundException()).when(serviceCatalog).delete(9L);

        mockMvc.perform(delete("/api/services/9")).andExpect(status().isNotFound());
    }
}
