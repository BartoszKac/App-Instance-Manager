package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredService;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class NmapDetailsParser {

    private static final int MAX_TEXT_LENGTH = 200;
    private static final Pattern NETBIOS_NAME = Pattern.compile("NetBIOS name: ([^,\\s]+)");
    private static final Pattern SMB_COMPUTER_NAME = Pattern.compile("Computer name: (\\S+)");
    private static final Pattern SMB_OS = Pattern.compile("OS: ([^\\r\\n]+)");
    private static final Pattern TLS_COMMON_NAME = Pattern.compile("commonName=([^/\\r\\n]+)");

    public DeviceDetailsResponse parse(String ip, String xml) {
        Optional<Element> host = NmapXml.elements(NmapXml.parseRoot(xml), "host").stream().findFirst();
        if (host.isEmpty()) {
            return new DeviceDetailsResponse(ip, null, null, null, null, List.of());
        }
        List<Element> ports = NmapXml.elements(host.get(), "port").stream().filter(NmapXmlParser::isOpen).toList();
        DeviceClassifier.OsMatch os = DeviceClassifier.detectOs(osEvidence(host.get(), ports));
        List<Integer> portNumbers = ports.stream().map(port -> Integer.valueOf(port.getAttribute("portid"))).toList();
        return new DeviceDetailsResponse(
                ip,
                hostname(host.get()),
                os == null ? null : os.name(),
                os == null ? null : os.family(),
                DeviceClassifier.deviceType(portNumbers),
                ports.stream().map(this::toService).toList());
    }

    private DiscoveredService toService(Element port) {
        Optional<Element> service = NmapXml.elements(port, "service").stream().findFirst();
        return new DiscoveredService(
                Integer.parseInt(port.getAttribute("portid")),
                service.map(element -> NmapXml.attribute(element, "name")).orElse(null),
                service.map(element -> NmapXml.attribute(element, "product")).orElse(null),
                service.map(element -> NmapXml.attribute(element, "version")).orElse(null),
                service.map(element -> NmapXml.attribute(element, "extrainfo")).orElse(null),
                pageTitle(port),
                script(port, "ssl-cert").map(output -> firstGroup(TLS_COMMON_NAME, output)).orElse(null));
    }

    private List<String> osEvidence(Element host, List<Element> ports) {
        List<String> evidence = new ArrayList<>();
        script(host, "smb-os-discovery").map(output -> firstGroup(SMB_OS, output)).ifPresent(evidence::add);
        ports.stream()
                .flatMap(port -> NmapXml.elements(port, "service").stream())
                .flatMap(service -> Stream.of("ostype", "version", "extrainfo", "product")
                        .map(name -> NmapXml.attribute(service, name)))
                .filter(value -> value != null)
                .forEach(evidence::add);
        return evidence;
    }

    private String hostname(Element host) {
        String smbName = script(host, "smb-os-discovery").map(output -> firstGroup(SMB_COMPUTER_NAME, output)).orElse(null);
        String name = smbName != null ? smbName : script(host, "nbstat").map(output -> firstGroup(NETBIOS_NAME, output)).orElse(null);
        boolean meaningless = name == null || name.startsWith("<") || "LOCALHOST".equalsIgnoreCase(name);
        return meaningless ? null : limited(name);
    }

    private String pageTitle(Element port) {
        return NmapXml.elements(port, "script").stream()
                .filter(script -> "http-title".equals(script.getAttribute("id")))
                .flatMap(script -> NmapXml.elements(script, "elem").stream())
                .filter(elem -> "title".equals(elem.getAttribute("key")))
                .map(elem -> limited(elem.getTextContent().trim()))
                .findFirst()
                .orElse(null);
    }

    private Optional<String> script(Element parent, String id) {
        return NmapXml.elements(parent, "script").stream()
                .filter(script -> id.equals(script.getAttribute("id")))
                .map(script -> script.getAttribute("output"))
                .findFirst();
    }

    private String firstGroup(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? limited(matcher.group(1).trim()) : null;
    }

    private String limited(String text) {
        return text.substring(0, Math.min(MAX_TEXT_LENGTH, text.length()));
    }
}
