package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DeviceDetailsResponse;
import com.example.DynamicCode.deploy.dto.DiscoveredService;
import com.example.DynamicCode.deploy.dto.ScriptResult;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class NmapDetailsParser {

    private static final int MAX_TEXT_LENGTH = 200;
    private static final int MAX_SCRIPT_OUTPUT_LENGTH = 2000;
    private static final Set<String> NOISY_SCRIPTS = Set.of("fingerprint-strings");
    private static final Pattern NETBIOS_NAME = Pattern.compile("NetBIOS name: ([^,\\s]+)");
    private static final Pattern NETBIOS_MAC = Pattern.compile("NetBIOS MAC: ([0-9a-fA-F:]{17})");
    private static final Pattern SMB_COMPUTER_NAME = Pattern.compile("Computer name: (\\S+)");
    private static final Pattern NTLM_COMPUTER_NAME = Pattern.compile("(?:DNS_Computer_Name|NetBIOS_Computer_Name): (\\S+)");
    private static final Pattern SMB_OS = Pattern.compile("OS: ([^\\r\\n]+)");
    private static final Pattern TLS_COMMON_NAME = Pattern.compile("commonName=([^/\\r\\n]+)");

    public DeviceDetailsResponse parse(String ip, String xml) {
        Optional<Element> host = NmapXml.elements(NmapXml.parseRoot(xml), "host").stream().findFirst();
        return host.map(element -> toDetails(ip, element)).orElseGet(() -> DeviceDetailsResponse.empty(ip));
    }

    private DeviceDetailsResponse toDetails(String ip, Element host) {
        List<Element> ports = NmapXml.elements(host, "port").stream().filter(NmapXmlParser::isOpen).toList();
        List<Integer> numbers = ports.stream().map(port -> Integer.valueOf(port.getAttribute("portid"))).toList();
        DeviceClassifier.OsMatch os = DeviceClassifier.detectOs(osEvidence(host, ports));
        String name = hostname(host);
        return new DeviceDetailsResponse(
                ip,
                name,
                scriptOutput(host, "nbstat").map(output -> firstGroup(NETBIOS_MAC, output)).orElse(null),
                os == null ? null : os.name(),
                os == null ? null : os.family(),
                DeviceClassifier.deviceType(numbers, name),
                ports.stream().map(this::toService).toList(),
                hostScripts(host));
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
                scriptOutput(port, "ssl-cert").map(output -> firstGroup(TLS_COMMON_NAME, output)).orElse(null),
                NmapXml.elements(port, "cpe").stream().map(cpe -> limited(cpe.getTextContent().trim())).toList(),
                scripts(port));
    }

    private List<ScriptResult> hostScripts(Element host) {
        return NmapXml.elements(host, "hostscript").stream().flatMap(element -> scripts(element).stream()).toList();
    }

    private List<ScriptResult> scripts(Element parent) {
        return NmapXml.elements(parent, "script").stream()
                .filter(script -> !NOISY_SCRIPTS.contains(script.getAttribute("id")))
                .map(script -> new ScriptResult(script.getAttribute("id"), limitedOutput(script.getAttribute("output"))))
                .toList();
    }

    private List<String> osEvidence(Element host, List<Element> ports) {
        List<String> evidence = new ArrayList<>();
        scriptOutput(host, "smb-os-discovery").map(output -> firstGroup(SMB_OS, output)).ifPresent(evidence::add);
        ports.stream()
                .flatMap(port -> NmapXml.elements(port, "service").stream())
                .flatMap(service -> Stream.of("ostype", "version", "extrainfo", "product")
                        .map(name -> NmapXml.attribute(service, name)))
                .filter(value -> value != null)
                .forEach(evidence::add);
        return evidence;
    }

    private String hostname(Element host) {
        return Stream.of(
                        scriptOutput(host, "smb-os-discovery").map(output -> firstGroup(SMB_COMPUTER_NAME, output)),
                        scriptOutput(host, "rdp-ntlm-info").map(output -> firstGroup(NTLM_COMPUTER_NAME, output)),
                        scriptOutput(host, "http-ntlm-info").map(output -> firstGroup(NTLM_COMPUTER_NAME, output)),
                        scriptOutput(host, "nbstat").map(output -> firstGroup(NETBIOS_NAME, output)))
                .flatMap(Optional::stream)
                .filter(name -> !name.startsWith("<") && !"LOCALHOST".equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
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

    private Optional<String> scriptOutput(Element parent, String id) {
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

    private String limitedOutput(String text) {
        String trimmed = text.trim();
        return trimmed.substring(0, Math.min(MAX_SCRIPT_OUTPUT_LENGTH, trimmed.length()));
    }
}
