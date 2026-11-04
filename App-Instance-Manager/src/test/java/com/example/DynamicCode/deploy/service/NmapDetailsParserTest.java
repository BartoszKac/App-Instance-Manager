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
    void shouldReadServicesOsCpeAndScripts() {
        String xml = """
                <nmaprun><host>
                  <ports>
                    <port protocol="tcp" portid="22"><state state="open"/>
                      <service name="ssh" product="OpenSSH" version="9.6p1 Ubuntu 3ubuntu13.20" extrainfo="Ubuntu Linux; protocol 2.0" ostype="Linux">
                        <cpe>cpe:/a:openbsd:openssh:9.6p1</cpe>
                      </service>
                    </port>
                    <port protocol="tcp" portid="80"><state state="open"/>
                      <service name="http" product="Apache httpd" version="2.4.58"/>
                      <script id="http-title" output="Strona"><elem key="title">Strona</elem></script>
                      <script id="http-headers" output="  Server: Apache "/>
                      <script id="fingerprint-strings" output="szum"/>
                    </port>
                  </ports>
                </host></nmaprun>
                """;

        DeviceDetailsResponse details = parser.parse(IP, xml);

        assertThat(details.osName()).isEqualTo("Linux (Ubuntu)");
        assertThat(details.osFamily()).isEqualTo(OperationSystem.LINUX);
        assertThat(details.deviceType()).isEqualTo("Komputer lub serwer");
        DiscoveredService ssh = details.services().get(0);
        assertThat(ssh.cpe()).containsExactly("cpe:/a:openbsd:openssh:9.6p1");
        DiscoveredService http = details.services().get(1);
        assertThat(http.title()).isEqualTo("Strona");
        assertThat(http.scripts()).extracting("id").containsExactly("http-title", "http-headers");
        assertThat(http.scripts().get(1).output()).isEqualTo("Server: Apache");
    }

    @Test
    void shouldReadHostnameMacAndOsFromHostScripts() {
        String xml = """
                <nmaprun><host>
                  <ports><port protocol="tcp" portid="445"><state state="open"/><service name="microsoft-ds"/></port></ports>
                  <hostscript>
                    <script id="smb-os-discovery" output="&#xa;  OS: Windows 10 Pro 19045&#xa;  Computer name: DESKTOP-1&#xa;"/>
                    <script id="nbstat" output="NetBIOS name: OTHER, NetBIOS user: &lt;unknown&gt;, NetBIOS MAC: aa:bb:cc:dd:ee:ff (x)"/>
                  </hostscript>
                </host></nmaprun>
                """;

        DeviceDetailsResponse details = parser.parse(IP, xml);

        assertThat(details.hostname()).isEqualTo("DESKTOP-1");
        assertThat(details.macAddress()).isEqualTo("aa:bb:cc:dd:ee:ff");
        assertThat(details.osFamily()).isEqualTo(OperationSystem.WINDOWS);
        assertThat(details.hostScripts()).extracting("id").containsExactly("smb-os-discovery", "nbstat");
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
    void shouldLimitLongTextFromDevice() {
        String xml = "<nmaprun><host><ports><port portid=\"80\"><state state=\"open\"/>"
                + "<script id=\"http-title\" output=\"" + "y".repeat(5000) + "\"><elem key=\"title\">" + "x".repeat(500)
                + "</elem></script></port></ports></host></nmaprun>";

        DiscoveredService service = parser.parse(IP, xml).services().get(0);

        assertThat(service.title()).hasSize(200);
        assertThat(service.scripts().get(0).output()).hasSize(2000);
    }

    @Test
    void shouldReturnEmptyDetailsWhenNoHostInOutput() {
        DeviceDetailsResponse details = parser.parse(IP, "<nmaprun></nmaprun>");

        assertThat(details.ip()).isEqualTo(IP);
        assertThat(details.services()).isEmpty();
        assertThat(details.osFamily()).isNull();
    }
}
