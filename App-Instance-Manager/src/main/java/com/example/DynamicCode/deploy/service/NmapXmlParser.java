package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class NmapXmlParser {

    public List<DiscoveredDevice> parseLiveHosts(String xml) {
        return NmapXml.elements(NmapXml.parseRoot(xml), "host").stream()
                .filter(this::isUp)
                .map(this::toDevice)
                .flatMap(Optional::stream)
                .toList();
    }

    public Map<String, List<Integer>> parseOpenPorts(String xml) {
        Map<String, List<Integer>> openPorts = new LinkedHashMap<>();
        for (Element host : NmapXml.elements(NmapXml.parseRoot(xml), "host")) {
            address(host, "ipv4").ifPresent(ip -> openPorts.put(ip.getAttribute("addr"), openPortsOf(host)));
        }
        return openPorts;
    }

    static boolean isOpen(Element port) {
        return NmapXml.elements(port, "state").stream()
                .anyMatch(state -> "open".equals(state.getAttribute("state")));
    }

    private List<Integer> openPortsOf(Element host) {
        return NmapXml.elements(host, "port").stream()
                .filter(NmapXmlParser::isOpen)
                .map(port -> Integer.valueOf(port.getAttribute("portid")))
                .toList();
    }

    private boolean isUp(Element host) {
        return NmapXml.elements(host, "status").stream()
                .anyMatch(status -> "up".equals(status.getAttribute("state")));
    }

    private Optional<DiscoveredDevice> toDevice(Element host) {
        Optional<Element> mac = address(host, "mac");
        return address(host, "ipv4").map(ip -> new DiscoveredDevice(
                ip.getAttribute("addr"),
                hostname(host),
                mac.map(element -> NmapXml.attribute(element, "addr")).orElse(null),
                mac.map(element -> NmapXml.attribute(element, "vendor")).orElse(null),
                false,
                List.of()));
    }

    private Optional<Element> address(Element host, String type) {
        return NmapXml.elements(host, "address").stream()
                .filter(address -> type.equals(address.getAttribute("addrtype")))
                .findFirst();
    }

    private String hostname(Element host) {
        return NmapXml.elements(host, "hostname").stream()
                .map(element -> NmapXml.attribute(element, "name"))
                .filter(name -> name != null)
                .findFirst()
                .orElse(null);
    }
}
