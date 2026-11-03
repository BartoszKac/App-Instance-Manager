package com.example.DynamicCode.inspection.controller;

import com.example.DynamicCode.inspection.dto.FileEntryResponse;
import com.example.DynamicCode.inspection.dto.FilePageResponse;
import com.example.DynamicCode.inspection.dto.InspectionStatusResponse;
import com.example.DynamicCode.inspection.enums.InspectionState;
import com.example.DynamicCode.inspection.exception.InspectionNotPossibleException;
import com.example.DynamicCode.inspection.service.InspectionQueries;
import com.example.DynamicCode.inspection.service.InspectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InspectionController.class)
class InspectionControllerTest {

    private static final InspectionStatusResponse QUEUED = new InspectionStatusResponse(
            1L, "box", "10.0.0.1", "u", InspectionState.QUEUED, null, 0, null, null, null, false);

    @Autowired
    private MockMvc mvc;
    @MockBean
    private InspectionService service;
    @MockBean
    private InspectionQueries queries;

    @Test
    void shouldAnswerAcceptedWhenInspectionIsQueued() throws Exception {
        when(service.start(1L)).thenReturn(QUEUED);

        mvc.perform(post("/api/inspections/servers/1"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.state").value("QUEUED"))
                .andExpect(jsonPath("$.pass").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldAnswerNotFoundForMissingServer() throws Exception {
        when(service.start(9L)).thenThrow(new IllegalArgumentException("missing"));

        mvc.perform(post("/api/inspections/servers/9")).andExpect(status().isNotFound());
    }

    @Test
    void shouldAnswerBadRequestWhenInspectionIsNotPossible() throws Exception {
        when(service.start(1L)).thenThrow(new InspectionNotPossibleException("brak hasła"));

        mvc.perform(post("/api/inspections/servers/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("brak hasła"));
    }

    @Test
    void shouldListChildrenOfRequestedFolder() throws Exception {
        FileEntryResponse entry = new FileEntryResponse("/etc/ssh", "ssh", "d", 0, "root", "root", "755", 1, true);
        when(queries.children(eq(1L), any())).thenReturn(new FilePageResponse(List.of(entry), 1));

        mvc.perform(get("/api/inspections/servers/1/files").param("parent", "/etc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.entries[0].denied").value(true));
    }

    @Test
    void shouldExposeLoadForAutoscaling() throws Exception {
        when(service.load()).thenReturn(5L);

        mvc.perform(get("/api/inspections/load")).andExpect(jsonPath("$.load").value(5));
    }

    @Test
    void shouldForgetHostKey() throws Exception {
        mvc.perform(delete("/api/inspections/servers/1/host-key")).andExpect(status().isNoContent());
    }
}
