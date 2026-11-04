package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredService;
import com.example.DynamicCode.deploy.dto.ScriptResult;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceInspectorTest {

    private static final String IP = "192.168.1.5";

    @Mock
    private NmapRunner nmapRunner;

    @Mock
    private NmapDetailsParser nmapDetailsParser;

    @InjectMocks
    private DeviceInspector inspector;

    @Test
    void shouldCreateOneInspectionPerPortAndOneForHost() {
        assertThat(inspector.inspections(IP, List.of(22, 80))).hasSize(3);
        assertThat(inspector.inspections(IP, List.of())).isEmpty();
    }

    @Test
    void shouldKeepPortAsUnrecognizedWhenItTimesOut() {
        when(nmapRunner.run(eq(NmapCommands.portDetails(IP, 80)), any(Duration.class))).thenReturn("<x/>");
        when(nmapDetailsParser.parse(IP, "<x/>")).thenReturn(DeviceDetailsResponse.empty(IP));

        DeviceDetailsResponse result = inspector.inspections(IP, List.of(80)).get(0).get();

        assertThat(result.services()).extracting(DiscoveredService::port).containsExactly(80);
        assertThat(result.services().get(0).product()).isNull();
    }

    @Test
    void shouldKeepPortWhenNmapFails() {
        when(nmapRunner.run(any(), any(Duration.class))).thenThrow(new NetworkScanFailedException("błąd", null));

        DeviceDetailsResponse result = inspector.inspections(IP, List.of(80)).get(0).get();

        assertThat(result.services()).hasSize(1);
    }

    @Test
    void shouldRunHostScriptsThroughSmbPortAndReturnOnlyHostData() {
        DiscoveredService service = new DiscoveredService(445, "smb", null, null, null, null, null, List.of(), List.of());
        DeviceDetailsResponse parsed = new DeviceDetailsResponse(IP, "pc", null, null, null, "Typ", List.of(service), List.of(new ScriptResult("nbstat", "x")));
        when(nmapRunner.run(any(), any(Duration.class))).thenReturn("<x/>");
        when(nmapDetailsParser.parse(IP, "<x/>")).thenReturn(parsed);

        DeviceDetailsResponse result = inspector.inspections(IP, List.of(22, 445)).get(2).get();

        verify(nmapRunner).run(eq(NmapCommands.hostDetails(IP, 445)), any(Duration.class));
        assertThat(result.hostname()).isEqualTo("pc");
        assertThat(result.services()).isEmpty();
        assertThat(result.hostScripts()).hasSize(1);
    }
}
