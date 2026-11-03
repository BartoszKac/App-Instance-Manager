package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredService;
import com.example.DynamicCode.deploy.enums.OperationSystem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NmapDetailsParserTest {

    private static final String IP = "192.168.1.67";

    private final NmapDetailsParser parser = new NmapDetailsParser();

    @Test
    void shouldReadServicesOsAndTitle() {
        String xml = """
                <nmaprun><host>
                  <address addr="192.168.1.67" addrtype="ipv4"/>
                  <ports>
                    <port protocol="tcp" portid="22"><state state="open"/>
                      <service name="ssh" product="OpenSSH" version="9.6p1 Ubuntu 3ubuntu13.20" extrainfo="Ubuntu Linux; protocol 2.0" ostype="Linux"/>
                    </port>
                    <port protocol="tcp" portid="80"><state state="open"/>
                      <service name="http" product="Apache httpd" version="2.4.58" extrainfo="(Ubuntu)"/>
                      <script id="http-title" output="Apache2 Ubuntu Default Page"><elem key="title">Apache2 Ubuntu Default Page</elem></script>
                    </port>
                  </ports>
                </host></nmaprun>
                """;

        DeviceDetailsResponse details = parser.parse(IP, xml);

        assertThat(details.osName()).isEqualTo("Linux (Ubuntu)");
        assertThat(details.osFamily()).isEqualTo(OperationSystem.LINUX);
        assertThat(details.deviceType()).isEqualTo("Komputer lub serwer");
        assertThat(details.services()).containsExactly(
                new DiscoveredService(22, "ssh", "OpenSSH", "9.6p1 Ubuntu 3ubuntu13.20", "Ubuntu Linux; protocol 2.0", null, null),
                new DiscoveredService(80, "http", "Apache httpd", "2.4.58", "(Ubuntu)", "Apache2 Ubuntu Default Page", null));
    }

    @Test
    void shouldReadHostnameAndOsFromSmbDiscovery() {
        String xml = """
                <nmaprun><host>
                  <ports><port protocol="tcp" portid="445"><state state="open"/><service name="microsoft-ds"/></port></ports>
                  <hostscript>
                    <script id="smb-os-discovery" output="&#xa;  OS: Windows 10 Pro 19045&#xa;  Computer name: DESKTOP-1&#xa;"/>
                    <script id="nbstat" output="NetBIOS name: OTHER, NetBIOS user: &lt;unknown&gt;"/>
                  </hostscript>
                </host></nmaprun>
                """;

        DeviceDetailsResponse details = parser.parse(IP, xml);

        assertThat(details.hostname()).isEqualTo("DESKTOP-1");
        assertThat(details.osFamily()).isEqualTo(OperationSystem.WINDOWS);
    }

    @Test
    void shouldUseNetbiosNameAndIgnoreMeaninglessOne() {
        String named = "<nmaprun><host><hostscript><script id=\"nbstat\" output=\"NetBIOS name: NAS, x\"/></hostscript></host></nmaprun>";
        String localhost = "<nmaprun><host><hostscript><script id=\"nbstat\" output=\"NetBIOS name: LOCALHOST, x\"/></hostscript></host></nmaprun>";

        assertThat(parser.parse(IP, named).hostname()).isEqualTo("NAS");
        assertThat(parser.parse(IP, localhost).hostname()).isNull();
    }

    @Test
    void shouldReadTlsCommonNameAndSkipClosedPorts() {
        String xml = """
                <nmaprun><host><ports>
                  <port protocol="tcp" portid="443"><state state="open"/><service name="https"/>
                    <script id="ssl-cert" output="Subject: commonName=router.lan/organizationName=ADB&#xa;Issuer: x"/>
                  </port>
                  <port protocol="tcp" portid="80"><state state="filtered"/><service name="http"/></port>
                </ports></host></nmaprun>
                """;

        List<DiscoveredService> services = parser.parse(IP, xml).services();

        assertThat(services).hasSize(1);
        assertThat(services.get(0).tlsSubject()).isEqualTo("router.lan");
    }

    @Test
    void shouldLimitLongTitleFromDevice() {
        String title = "x".repeat(500);
        String xml = "<nmaprun><host><ports><port portid=\"80\"><state state=\"open\"/>"
                + "<script id=\"http-title\"><elem key=\"title\">" + title + "</elem></script></port></ports></host></nmaprun>";

        assertThat(parser.parse(IP, xml).services().get(0).title()).hasSize(200);
    }

    @Test
    void shouldReturnEmptyDetailsWhenNoHostInOutput() {
        DeviceDetailsResponse details = parser.parse(IP, "<nmaprun></nmaprun>");

        assertThat(details.ip()).isEqualTo(IP);
        assertThat(details.services()).isEmpty();
        assertThat(details.osFamily()).isNull();
    }
}
