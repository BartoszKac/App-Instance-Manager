package com.example.DynamicCode.deploy.controller;

import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.NetworkScanResponse;
import com.example.DynamicCode.deploy.exception.InvalidSubnetException;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import com.example.DynamicCode.deploy.exception.NetworkScanInProgressException;
import com.example.DynamicCode.deploy.service.LocalSubnetService;
import com.example.DynamicCode.deploy.service.NetworkScanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NetworkDiscoveryController.class)
class NetworkDiscoveryControllerTest {

    private static final String CIDR = "192.168.1.0/24";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocalSubnetService localSubnetService;

    @MockBean
    private NetworkScanService networkScanService;

    private ResultActions postScan() throws Exception {
        return mockMvc.perform(post("/api/deploy/network/scan")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cidr\":\"" + CIDR + "\"}"));
    }

    @Test
    void shouldReturnSubnets() throws Exception {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));

        mockMvc.perform(get("/api/deploy/network/subnets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(CIDR));
    }

    @Test
    void shouldReturnDeviceCountAndDevicesWhenScanSucceeds() throws Exception {
        DiscoveredDevice device = new DiscoveredDevice("192.168.1.10", "pi.local", null, null, true);
        when(networkScanService.scan(CIDR)).thenReturn(new NetworkScanResponse(CIDR, 1, List.of(device)));

        postScan()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceCount").value(1))
                .andExpect(jsonPath("$.devices[0].ip").value("192.168.1.10"))
                .andExpect(jsonPath("$.devices[0].sshOpen").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenSubnetIsNotAllowed() throws Exception {
        when(networkScanService.scan(CIDR)).thenThrow(new InvalidSubnetException());

        postScan().andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void shouldReturnConflictWhenScanIsRunning() throws Exception {
        when(networkScanService.scan(CIDR)).thenThrow(new NetworkScanInProgressException());

        postScan().andExpect(status().isConflict());
    }

    @Test
    void shouldReturnServiceUnavailableWithHintWhenNmapFails() throws Exception {
        when(networkScanService.scan(CIDR)).thenThrow(new NetworkScanFailedException("brak nmap", null));

        postScan().andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.detail").value("brak nmap"));
    }

    @Test
    void shouldAllowViteOriginAndRejectOtherWebsites() throws Exception {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));

        mockMvc.perform(get("/api/deploy/network/subnets").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mockMvc.perform(get("/api/deploy/network/subnets").header("Origin", "https://evil.example"))
                .andExpect(status().isForbidden());
    }
}
