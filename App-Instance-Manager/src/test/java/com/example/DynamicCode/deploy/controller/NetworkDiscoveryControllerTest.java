package com.example.DynamicCode.deploy.controller;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.DiscoveredService;
import com.example.DynamicCode.deploy.dto.ScanJobResponse;
import com.example.DynamicCode.deploy.enums.OperationSystem;
import com.example.DynamicCode.deploy.enums.ScanStage;
import com.example.DynamicCode.deploy.enums.ScanState;
import com.example.DynamicCode.deploy.exception.InvalidScanTargetException;
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
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NetworkDiscoveryController.class)
class NetworkDiscoveryControllerTest {

    private static final String CIDR = "192.168.1.0/24";
    private static final String IP = "192.168.1.10";
    private static final ScanJobResponse JOB = new ScanJobResponse("id-1", CIDR, ScanState.RUNNING, ScanStage.PORTS, 30, 254, 254,
            1, 0, 0, 1, List.of(new DiscoveredDevice(IP, "pi", null, null, true, List.of(22), null)), Map.of(), null);

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

    private ResultActions postDetails() throws Exception {
        return mockMvc.perform(post("/api/deploy/network/device-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ip\":\"" + IP + "\"}"));
    }

    @Test
    void shouldReturnSubnets() throws Exception {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));

        mockMvc.perform(get("/api/deploy/network/subnets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(CIDR));
    }

    @Test
    void shouldAcceptScanAndReturnProgress() throws Exception {
        when(networkScanService.startScan(CIDR)).thenReturn(JOB);

        postScan()
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("id-1"))
                .andExpect(jsonPath("$.percent").value(30))
                .andExpect(jsonPath("$.stage").value("PORTS"))
                .andExpect(jsonPath("$.devices[0].openPorts[0]").value(22));
    }

    @Test
    void shouldReturnCurrentScan() throws Exception {
        when(networkScanService.currentScan()).thenReturn(Optional.of(JOB));

        mockMvc.perform(get("/api/deploy/network/scan/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addressesScanned").value(254));
    }

    @Test
    void shouldReturnNoContentWhenNoScanYet() throws Exception {
        when(networkScanService.currentScan()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/deploy/network/scan/current")).andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnDeviceDetails() throws Exception {
        DiscoveredService service = new DiscoveredService(80, "http", "Apache httpd", "2.4.58", null, "Strona", null, List.of(), List.of());
        when(networkScanService.details(IP)).thenReturn(new DeviceDetailsResponse(
                IP, "pi", null, "Linux", OperationSystem.LINUX, "Komputer lub serwer", List.of(service), List.of()));

        postDetails()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.osFamily").value("LINUX"))
                .andExpect(jsonPath("$.services[0].product").value("Apache httpd"));
    }

    @Test
    void shouldReturnBadRequestWhenDetailsTargetIsNotAllowed() throws Exception {
        when(networkScanService.details(IP)).thenThrow(new InvalidScanTargetException());

        postDetails().andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenSubnetIsNotAllowed() throws Exception {
        when(networkScanService.startScan(CIDR)).thenThrow(new InvalidSubnetException());

        postScan().andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void shouldReturnConflictWhenScanIsRunning() throws Exception {
        when(networkScanService.startScan(CIDR)).thenThrow(new NetworkScanInProgressException());

        postScan().andExpect(status().isConflict());
    }

    @Test
    void shouldReturnServiceUnavailableWithHintWhenDetailsFail() throws Exception {
        when(networkScanService.details(IP)).thenThrow(new NetworkScanFailedException("brak nmap", null));

        postDetails().andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.detail").value("brak nmap"));
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
