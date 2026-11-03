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
                    <ports><port protocol="tcp" portid="22"><state state="open"/></port></ports>
                  </host>
                </nmaprun>
                """;

        List<DiscoveredDevice> devices = parser.parse(xml);

        assertThat(devices).containsExactly(
                new DiscoveredDevice("192.168.1.10", "pi.local", "AA:BB:CC:DD:EE:FF", "Raspberry Pi", true));
    }

    @Test
    void shouldLeaveOptionalFieldsEmptyWhenNmapDidNotReportThem() {
        String xml = HEADER + """
                <nmaprun>
                  <host>
                    <status state="up"/>
                    <address addr="192.168.1.20" addrtype="ipv4"/>
                    <ports>
                      <port protocol="tcp" portid="22"><state state="closed"/></port>
                      <port protocol="tcp" portid="80"><state state="open"/></port>
                    </ports>
                  </host>
                </nmaprun>
                """;

        List<DiscoveredDevice> devices = parser.parse(xml);

        assertThat(devices).containsExactly(new DiscoveredDevice("192.168.1.20", null, null, null, false));
    }

    @Test
    void shouldSkipHostsWithoutOpenPortAndHostHints() {
        String xml = HEADER + """
                <nmaprun>
                  <hosthint><status state="up"/><address addr="192.168.1.30" addrtype="ipv4"/></hosthint>
                  <host><status state="down"/><address addr="192.168.1.31" addrtype="ipv4"/></host>
                  <host starttime="1793658779" endtime="1793658809">
                    <status state="up" reason="user-set" reason_ttl="0"/>
                    <address addr="192.168.1.103" addrtype="ipv4"/>
                    <hostnames></hostnames>
                  </host>
                  <host>
                    <status state="up"/>
                    <address addr="192.168.1.32" addrtype="ipv4"/>
                    <ports><port protocol="tcp" portid="22"><state state="filtered"/></port></ports>
                  </host>
                </nmaprun>
                """;

        assertThat(parser.parse(xml)).isEmpty();
    }

    @Test
    void shouldFailWhenOutputIsNotXml() {
        assertThatThrownBy(() -> parser.parse("nmap: command output"))
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
                    <ports><port protocol="tcp" portid="22"><state state="open"/></port></ports>
                  </host>
                </nmaprun>
                """;

        assertThatNoSecretLeaks(xml);
    }

    private void assertThatNoSecretLeaks(String xml) {
        try {
            assertThat(parser.parse(xml)).allSatisfy(device -> assertThat(device.hostname()).isNull());
        } catch (NetworkScanFailedException expectedWhenParserRejectsEntity) {
            assertThat(expectedWhenParserRejectsEntity).hasMessageContaining("nmap");
        }
    }
}
