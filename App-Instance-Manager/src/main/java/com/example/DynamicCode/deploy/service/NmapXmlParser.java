package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.dto.DiscoveredDevice;
import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Component
public class NmapXmlParser {

    private static final String SSH_PORT = "22";

    public List<DiscoveredDevice> parse(String xml) {
        try {
            Document document = newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            return elements(document.getDocumentElement(), "host").stream()
                    .filter(this::hasOpenPort)
                    .map(this::toDevice)
                    .flatMap(Optional::stream)
                    .toList();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new NetworkScanFailedException("Nie udało się odczytać wyniku skanu nmap.", e);
        }
    }

    private DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }

    private boolean hasOpenPort(Element host) {
        return elements(host, "port").stream()
                .flatMap(port -> elements(port, "state").stream())
                .anyMatch(state -> "open".equals(state.getAttribute("state")));
    }

    private Optional<DiscoveredDevice> toDevice(Element host) {
        Optional<Element> ipv4 = address(host, "ipv4");
        Optional<Element> mac = address(host, "mac");
        return ipv4.map(ip -> new DiscoveredDevice(
                ip.getAttribute("addr"),
                hostname(host),
                mac.map(element -> emptyToNull(element.getAttribute("addr"))).orElse(null),
                mac.map(element -> emptyToNull(element.getAttribute("vendor"))).orElse(null),
                isSshOpen(host)));
    }

    private Optional<Element> address(Element host, String type) {
        return elements(host, "address").stream()
                .filter(address -> type.equals(address.getAttribute("addrtype")))
                .findFirst();
    }

    private String hostname(Element host) {
        return elements(host, "hostname").stream()
                .map(element -> emptyToNull(element.getAttribute("name")))
                .filter(name -> name != null)
                .findFirst()
                .orElse(null);
    }

    private boolean isSshOpen(Element host) {
        return elements(host, "port").stream()
                .filter(port -> SSH_PORT.equals(port.getAttribute("portid")))
                .flatMap(port -> elements(port, "state").stream())
                .anyMatch(state -> "open".equals(state.getAttribute("state")));
    }

    private List<Element> elements(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return IntStream.range(0, nodes.getLength())
                .mapToObj(index -> (Element) nodes.item(index))
                .toList();
    }

    private String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }
}
