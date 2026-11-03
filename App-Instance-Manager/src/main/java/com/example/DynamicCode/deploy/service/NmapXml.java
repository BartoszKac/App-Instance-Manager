package com.example.DynamicCode.deploy.service;

import com.example.DynamicCode.deploy.exception.NetworkScanFailedException;
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
import java.util.stream.IntStream;

final class NmapXml {

    private NmapXml() {
    }

    static Element parseRoot(String xml) {
        try {
            return newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new NetworkScanFailedException("Nie udało się odczytać wyniku skanu nmap.", e);
        }
    }

    static List<Element> elements(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return IntStream.range(0, nodes.getLength())
                .mapToObj(index -> (Element) nodes.item(index))
                .toList();
    }

    static String attribute(Element element, String name) {
        String value = element.getAttribute(name);
        return value.isEmpty() ? null : value;
    }

    private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }
}
