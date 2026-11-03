package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.NetworkScanResponse;
import com.example.DynamicCode.deploy.exception.InvalidScanTargetException;
import com.example.DynamicCode.deploy.exception.InvalidSubnetException;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import com.example.DynamicCode.deploy.exception.NetworkScanInProgressException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkScanServiceTest {

    private static final String CIDR = "192.168.1.0/24";
    private static final DiscoveredDevice HIGH = new DiscoveredDevice("192.168.1.100", null, null, null, false, List.of());
    private static final DiscoveredDevice LOW = new DiscoveredDevice("192.168.1.9", null, null, null, false, List.of());

    @Mock
    private NmapRunner nmapRunner;

    @Mock
    private LocalSubnetService localSubnetService;

    @Mock
    private NmapXmlParser nmapXmlParser;

    @Mock
    private NmapDetailsParser nmapDetailsParser;

    private NetworkScanService service() {
        return new NetworkScanService(nmapRunner, localSubnetService, nmapXmlParser, nmapDetailsParser);
    }

    @Test
    void shouldSweepThenProbeOnlyLiveHostsAndSortThem() {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));
        when(nmapRunner.run(eq(NmapCommands.sweep(CIDR)), any(Duration.class))).thenReturn("<sweep/>");
        when(nmapXmlParser.parseLiveHosts("<sweep/>")).thenReturn(List.of(HIGH, LOW));
        when(nmapRunner.run(eq(NmapCommands.openPorts(List.of(HIGH.ip(), LOW.ip()))), any(Duration.class))).thenReturn("<ports/>");
        when(nmapXmlParser.parseOpenPorts("<ports/>")).thenReturn(Map.of(LOW.ip(), List.of(22)));

        NetworkScanResponse response = service().scan(CIDR);

        assertThat(response.deviceCount()).isEqualTo(2);
        assertThat(response.devices()).extracting(DiscoveredDevice::ip).containsExactly(LOW.ip(), HIGH.ip());
        assertThat(response.devices().get(0).sshOpen()).isTrue();
        assertThat(response.devices().get(1).openPorts()).isEmpty();
    }

    @Test
    void shouldSkipPortScanWhenNoLiveHosts() {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));
        when(nmapRunner.run(eq(NmapCommands.sweep(CIDR)), any(Duration.class))).thenReturn("<sweep/>");
        when(nmapXmlParser.parseLiveHosts("<sweep/>")).thenReturn(List.of());

        assertThat(service().scan(CIDR).deviceCount()).isZero();

        verify(nmapXmlParser, never()).parseOpenPorts(any());
    }

    @Test
    void shouldPassSweepTargetAsSingleArgument() {
        assertThat(NmapCommands.sweep(CIDR)).endsWith(CIDR);
        assertThat(NmapCommands.details("192.168.1.5")).endsWith("192.168.1.5");
        assertThat(NmapCommands.openPorts(List.of("192.168.1.5", "192.168.1.6"))).endsWith("192.168.1.5", "192.168.1.6");
    }

    @Test
    void shouldRejectSubnetWithoutStartingProcessWhenItIsNotLocal() {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));

        assertThatThrownBy(() -> service().scan("8.8.8.0/24")).isInstanceOf(InvalidSubnetException.class);

        verifyNoInteractions(nmapRunner);
    }

    @Test
    void shouldRejectArgumentInjectionWithoutStartingProcess() {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));

        assertThatThrownBy(() -> service().scan("--script=evil.nse")).isInstanceOf(InvalidSubnetException.class);

        verifyNoInteractions(nmapRunner);
    }

    @Test
    void shouldRejectNullSubnet() {
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));

        assertThatThrownBy(() -> service().scan(null)).isInstanceOf(InvalidSubnetException.class);

        verifyNoInteractions(nmapRunner);
    }

    @Test
    void shouldAllowNextScanWhenPreviousFailed() {
        NetworkScanService service = service();
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));
        when(nmapRunner.run(any(), any(Duration.class)))
                .thenThrow(new NetworkScanFailedException("brak nmap", null))
                .thenReturn("<sweep/>");
        when(nmapXmlParser.parseLiveHosts("<sweep/>")).thenReturn(List.of());

        assertThatThrownBy(() -> service.scan(CIDR)).isInstanceOf(NetworkScanFailedException.class);

        assertThat(service.scan(CIDR).deviceCount()).isZero();
    }

    @Test
    void shouldRejectSecondScanWhileFirstIsRunning() throws Exception {
        NetworkScanService service = service();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));
        when(nmapRunner.run(any(), any(Duration.class))).thenAnswer(invocation -> {
            started.countDown();
            release.await(5, TimeUnit.SECONDS);
            return "<sweep/>";
        });
        Thread first = new Thread(() -> service.scan(CIDR));
        first.start();
        assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

        assertThatThrownBy(() -> service.scan(CIDR)).isInstanceOf(NetworkScanInProgressException.class);

        release.countDown();
        first.join(5000);
    }

    @Test
    void shouldReturnDetailsWhenHostIsAllowed() {
        DeviceDetailsResponse expected = new DeviceDetailsResponse("192.168.1.5", null, null, null, null, List.of());
        when(localSubnetService.isAllowedHost("192.168.1.5")).thenReturn(true);
        when(nmapRunner.run(eq(NmapCommands.details("192.168.1.5")), any(Duration.class))).thenReturn("<d/>");
        when(nmapDetailsParser.parse("192.168.1.5", "<d/>")).thenReturn(expected);

        assertThat(service().details("192.168.1.5")).isEqualTo(expected);
    }

    @Test
    void shouldRejectDetailsWithoutStartingProcessWhenHostIsNotAllowed() {
        when(localSubnetService.isAllowedHost("8.8.8.8")).thenReturn(false);

        assertThatThrownBy(() -> service().details("8.8.8.8")).isInstanceOf(InvalidScanTargetException.class);

        verifyNoInteractions(nmapRunner);
    }
}
