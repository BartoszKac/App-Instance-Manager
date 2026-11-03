package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NmapXmlParserTest {

    private static final String HEADER = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE nmaprun>
            <?xml-stylesheet href="file:///usr/bin/../share/nmap/nmap.xsl" type="text/xsl"?>
            """;

    private final NmapXmlParser parser = new NmapXmlParser();

    @Test
    void shouldReadDeviceDetailsWhenHostIsUp() {
        String xml = HEADER + """
                <nmaprun>
                  <host>
                    <status state="up" reason="arp-response"/>
                    <address addr="192.168.1.10" addrtype="ipv4"/>
                    <address addr="AA:BB:CC:DD:EE:FF" addrtype="mac" vendor="Raspberry Pi"/>
                    <hostnames><hostname name="pi.local" type="PTR"/></hostnames>
                  </host>
                </nmaprun>
                """;

        assertThat(parser.parseLiveHosts(xml)).containsExactly(new DiscoveredDevice(
                "192.168.1.10", "pi.local", "AA:BB:CC:DD:EE:FF", "Raspberry Pi", false, List.of(), null));
    }

    @Test
    void shouldCountHostWithoutOpenPortsWhenItAnsweredPing() {
        String xml = HEADER + """
                <nmaprun>
                  <host><status state="up" reason="echo-reply"/><address addr="192.168.1.8" addrtype="ipv4"/></host>
                </nmaprun>
                """;

        assertThat(parser.parseLiveHosts(xml)).extracting(DiscoveredDevice::ip).containsExactly("192.168.1.8");
    }

    @Test
    void shouldSkipHostsThatAreDownAndHostHints() {
        String xml = HEADER + """
                <nmaprun>
                  <hosthint><status state="up"/><address addr="192.168.1.30" addrtype="ipv4"/></hosthint>
                  <host><status state="down"/><address addr="192.168.1.31" addrtype="ipv4"/></host>
                </nmaprun>
                """;

        assertThat(parser.parseLiveHosts(xml)).isEmpty();
    }

    @Test
    void shouldReadOnlyOpenPortsSorted() {
        String xml = HEADER + """
                <nmaprun><host>
                  <address addr="192.168.1.20" addrtype="ipv4"/>
                  <ports>
                    <port protocol="tcp" portid="8080"><state state="open"/></port>
                    <port protocol="tcp" portid="22"><state state="closed"/></port>
                    <port protocol="tcp" portid="80"><state state="open"/></port>
                    <port protocol="tcp" portid="443"><state state="filtered"/></port>
                  </ports>
                </host></nmaprun>
                """;

        assertThat(parser.parseOpenPorts(xml)).containsExactly(80, 8080);
    }

    @Test
    void shouldReadHostnamesFromListScan() {
        String xml = HEADER + """
                <nmaprun>
                  <host><status state="unknown"/><address addr="192.168.1.8" addrtype="ipv4"/>
                    <hostnames><hostname name="Pixel-4a" type="PTR"/></hostnames></host>
                  <host><status state="unknown"/><address addr="192.168.1.1" addrtype="ipv4"/><hostnames></hostnames></host>
                </nmaprun>
                """;

        assertThat(parser.parseHostnames(xml)).containsOnlyKeys("192.168.1.8").containsEntry("192.168.1.8", "Pixel-4a");
    }

    @Test
    void shouldSetSshOpenWhenPortsContainSsh() {
        DiscoveredDevice device = new DiscoveredDevice("192.168.1.1", null, null, null, false, List.of(), null);

        assertThat(device.withOpenPorts(List.of(22, 80)).sshOpen()).isTrue();
        assertThat(device.withOpenPorts(List.of(80)).sshOpen()).isFalse();
    }

    @Test
    void shouldFailWhenOutputIsNotXml() {
        assertThatThrownBy(() -> parser.parseLiveHosts("nmap: command output"))
                .isInstanceOf(NetworkScanFailedException.class);
    }

    @Test
    void shouldNotReadExternalEntityWhenXmlDeclaresOne() {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE nmaprun [<!ENTITY secret SYSTEM "file:///etc/hostname">]>
                <nmaprun>
                  <host>
                    <status state="up"/>
                    <address addr="192.168.1.40" addrtype="ipv4"/>
                    <hostnames><hostname name="&secret;"/></hostnames>
                  </host>
                </nmaprun>
                """;

        assertThatNoSecretLeaks(xml);
    }

    private void assertThatNoSecretLeaks(String xml) {
        try {
            assertThat(parser.parseLiveHosts(xml)).allSatisfy(device -> assertThat(device.hostname()).isNull());
        } catch (NetworkScanFailedException expectedWhenParserRejectsEntity) {
            assertThat(expectedWhenParserRejectsEntity).hasMessageContaining("nmap");
        }
    }
}
