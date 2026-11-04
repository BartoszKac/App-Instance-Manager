package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.dto.DiscoveredService;
import com.example.DynamicCode.deploy.dto.ScanJobResponse;
import com.example.DynamicCode.deploy.enums.ScanState;
import com.example.DynamicCode.deploy.exception.InvalidScanTargetException;
import com.example.DynamicCode.deploy.exception.InvalidSubnetException;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import com.example.DynamicCode.deploy.exception.NetworkScanInProgressException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NetworkScanServiceTest {

    private static final String CIDR = "192.168.1.0/28";
    private static final String HOST = "192.168.1.5";
    private static final DiscoveredDevice DEVICE = new DiscoveredDevice(HOST, null, null, null, false, List.of(), null);
    private static final DiscoveredService SSH = new DiscoveredService(22, "ssh", null, null, null, null, null, List.of(), List.of());

    @Mock
    private NmapRunner nmapRunner;

    @Mock
    private LocalSubnetService localSubnetService;

    @Mock
    private NmapXmlParser nmapXmlParser;

    @Mock
    private DeviceInspector deviceInspector;

    private NetworkScanService service;

    @BeforeEach
    void setUp() {
        service = new NetworkScanService(nmapRunner, localSubnetService, nmapXmlParser, deviceInspector);
        when(localSubnetService.listSubnets()).thenReturn(List.of(CIDR));
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    private ScanJobResponse awaitFinished() throws InterruptedException {
        for (int attempt = 0; attempt < 200; attempt++) {
            ScanJobResponse current = service.currentScan().orElseThrow();
            if (current.state() != ScanState.RUNNING) {
                return current;
            }
            Thread.sleep(20);
        }
        throw new AssertionError("Scan did not finish");
    }

    private Supplier<DeviceDetailsResponse> inspectionOf(DiscoveredService found) {
        return () -> new DeviceDetailsResponse(HOST, null, null, null, null, null, List.of(found), List.of());
    }

    private void stubSuccessfulScan() {
        when(nmapRunner.run(any(), any(Duration.class))).thenReturn("<x/>");
        when(nmapXmlParser.parseLiveHosts(any())).thenReturn(List.of(DEVICE));
        when(nmapXmlParser.parseOpenPorts(any())).thenReturn(List.of(22, 53));
        when(nmapXmlParser.parseHostnames(any())).thenReturn(Map.of(HOST, "server"));
        when(deviceInspector.inspections(HOST, List.of(22, 53))).thenReturn(List.of(inspectionOf(SSH)));
    }

    @Test
    void shouldRunAllStagesInBackgroundAndFillDevice() throws Exception {
        stubSuccessfulScan();

        ScanJobResponse started = service.startScan(CIDR);
        ScanJobResponse finished = awaitFinished();

        assertThat(started.id()).isEqualTo(finished.id());
        assertThat(finished.state()).isEqualTo(ScanState.DONE);
        assertThat(finished.percent()).isEqualTo(100);
        assertThat(finished.addressesScanned()).isEqualTo(14);
        DiscoveredDevice device = finished.devices().get(0);
        assertThat(device.hostname()).isEqualTo("server");
        assertThat(device.sshOpen()).isTrue();
        assertThat(device.deviceType()).isEqualTo("Router lub serwer DNS");
        assertThat(finished.detailsTotal()).isEqualTo(1);
        assertThat(finished.details().get(HOST).services()).containsExactly(SSH);
        verify(nmapRunner).run(eq(NmapCommands.names(List.of(HOST), List.of(HOST))), any(Duration.class));
    }

    @Test
    void shouldFinishWithFailedStateAndAllowNextScanWhenNmapIsMissing() throws Exception {
        when(nmapRunner.run(any(), any(Duration.class))).thenThrow(new NetworkScanFailedException("brak nmap", null));

        service.startScan(CIDR);
        ScanJobResponse failed = awaitFinished();

        assertThat(failed.state()).isEqualTo(ScanState.FAILED);
        assertThat(failed.error()).isEqualTo("brak nmap");
        assertThat(service.startScan(CIDR).state()).isEqualTo(ScanState.RUNNING);
    }

    @Test
    void shouldSkipNamesWhenNoDnsServerFound() throws Exception {
        when(nmapRunner.run(any(), any(Duration.class))).thenReturn("<x/>");
        when(nmapXmlParser.parseLiveHosts(any())).thenReturn(List.of(DEVICE));
        when(nmapXmlParser.parseOpenPorts(any())).thenReturn(List.of());
        when(deviceInspector.inspections(HOST, List.of())).thenReturn(List.of());

        service.startScan(CIDR);
        ScanJobResponse finished = awaitFinished();

        assertThat(finished.state()).isEqualTo(ScanState.DONE);
        assertThat(finished.devices().get(0).openPorts()).isEmpty();
    }

    @Test
    void shouldRejectSecondScanWhileFirstIsRunning() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        when(nmapRunner.run(any(), any(Duration.class))).thenAnswer(invocation -> {
            release.await(5, TimeUnit.SECONDS);
            return "<x/>";
        });
        when(nmapXmlParser.parseLiveHosts(any())).thenReturn(List.of());
        service.startScan(CIDR);

        assertThatThrownBy(() -> service.startScan(CIDR)).isInstanceOf(NetworkScanInProgressException.class);

        release.countDown();
        awaitFinished();
    }

    @Test
    void shouldReturnEmptyCurrentScanBeforeFirstRun() {
        assertThat(service.currentScan()).isEmpty();
    }

    @Test
    void shouldRejectForeignSubnetAndArgumentInjectionWithoutStartingProcess() {
        for (String target : new String[] {"8.8.8.0/24", "--script=evil.nse", null}) {
            assertThatThrownBy(() -> service.startScan(target)).isInstanceOf(InvalidSubnetException.class);
        }

        verifyNoInteractions(nmapRunner);
    }

    @Test
    void shouldPassEveryTargetAsSeparateArgument() {
        assertThat(NmapCommands.sweep(List.of("192.168.1.1", "192.168.1.2"))).endsWith("192.168.1.1", "192.168.1.2");
        assertThat(NmapCommands.openPorts(HOST)).endsWith(HOST);
        assertThat(NmapCommands.portDetails(HOST, 80)).endsWith(HOST).contains("80");
        assertThat(NmapCommands.hostDetails(HOST, 445)).endsWith(HOST).contains("445");
        assertThat(NmapCommands.names(List.of(HOST), List.of("192.168.1.1"))).contains("192.168.1.1").endsWith(HOST);
    }

    @Test
    void shouldMergeInspectionsAndClassifyWhenAskedForOneHost() {
        when(localSubnetService.isAllowedHost(HOST)).thenReturn(true);
        when(nmapRunner.run(any(), any(Duration.class))).thenReturn("<x/>");
        when(nmapXmlParser.parseOpenPorts("<x/>")).thenReturn(List.of(22));
        when(deviceInspector.inspections(HOST, List.of(22))).thenReturn(List.of(inspectionOf(SSH)));

        DeviceDetailsResponse details = service.details(HOST);

        assertThat(details.services()).containsExactly(SSH);
        assertThat(details.deviceType()).isEqualTo("Komputer lub serwer");
    }

    @Test
    void shouldReturnEmptyDetailsWhenHostHasNoOpenPorts() {
        when(localSubnetService.isAllowedHost(HOST)).thenReturn(true);
        when(nmapRunner.run(any(), any(Duration.class))).thenReturn("<x/>");
        when(nmapXmlParser.parseOpenPorts("<x/>")).thenReturn(List.of());
        when(deviceInspector.inspections(HOST, List.of())).thenReturn(List.of());

        assertThat(service.details(HOST).services()).isEmpty();
    }

    @Test
    void shouldRejectDetailsWithoutStartingProcessWhenHostIsNotAllowed() {
        when(localSubnetService.isAllowedHost("8.8.8.8")).thenReturn(false);

        assertThatThrownBy(() -> service.details("8.8.8.8")).isInstanceOf(InvalidScanTargetException.class);

        verifyNoInteractions(nmapRunner);
    }
}
