package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.code.service.LanguageProcess;
import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.NetworkScanResponse;
import com.example.DynamicCode.deploy.exception.InvalidSubnetException;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkScanServiceTest {

    private static final String CIDR = "192.168.1.0/24";

    @Mock
    private LanguageProcess languageProcess;

    @Mock
    private LocalSubnetService localSubnetService;

    @Mock
    private NmapXmlParser nmapXmlParser;

    private NetworkScanService service() {
        return new NetworkScanService(languageProcess, localSubnetService, nmapXmlParser);
    }

    @Test
    void shouldPassArgumentsAsListAndSortDevicesWhenSubnetIsLocal() throws Exception {
        DiscoveredDevice second = new DiscoveredDevice("192.168.1.100", null, null, null, false);
        DiscoveredDevice first = new DiscoveredDevice("192.168.1.9", null, null, null, true);
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));
        when(languageProcess.captureOutput(any(), any())).thenReturn("<xml/>");
        when(nmapXmlParser.parse("<xml/>")).thenReturn(List.of(second, first));

        NetworkScanResponse response = service().scan(CIDR);

        assertThat(response.deviceCount()).isEqualTo(2);
        assertThat(response.devices()).containsExactly(first, second);
        verify(languageProcess).captureOutput(
                eq(List.of("nmap", "-Pn", "-sT", "-p", "22,80,135,139,443,445,3389,8080,9100,62078", "--open",
                        "-T4", "--max-retries", "1", "-oX", "-", CIDR)),
                eq(Duration.ofMinutes(4)));
    }

    @Test
    void shouldRejectSubnetWithoutStartingProcessWhenItIsNotLocal() {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));

        assertThatThrownBy(() -> service().scan("8.8.8.0/24")).isInstanceOf(InvalidSubnetException.class);

        verifyNoInteractions(languageProcess);
    }

    @Test
    void shouldRejectArgumentInjectionWithoutStartingProcess() {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));

        assertThatThrownBy(() -> service().scan("--script=evil.nse")).isInstanceOf(InvalidSubnetException.class);

        verifyNoInteractions(languageProcess);
    }

    @Test
    void shouldRejectNullSubnet() {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));

        assertThatThrownBy(() -> service().scan(null)).isInstanceOf(InvalidSubnetException.class);

        verifyNoInteractions(languageProcess);
    }

    @Test
    void shouldReportFailureAndAllowNextScanWhenNmapCannotStart() throws Exception {
        NetworkScanService service = service();
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));
        when(languageProcess.captureOutput(any(), any()))
                .thenThrow(new IOException("not found"))
                .thenReturn("<xml/>");
        when(nmapXmlParser.parse("<xml/>")).thenReturn(List.of());

        assertThatThrownBy(() -> service.scan(CIDR))
                .isInstanceOf(NetworkScanFailedException.class)
                .hasMessageContaining("nmap");

        assertThat(service.scan(CIDR).deviceCount()).isZero();
    }
}
