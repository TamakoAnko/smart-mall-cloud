package com.xzj.other.wechat.util;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

/** Minimal XML reader for WeChat callbacks; avoids importing payment business code. */
final class XmlHelper {
    private final Document document;

    private XmlHelper(Document document) {
        this.document = document;
    }

    static XmlHelper of(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            return new XmlHelper(factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml))));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid WeChat XML", e);
        }
    }

    String getString(String expression) {
        try {
            return XPathFactory.newInstance().newXPath().evaluate(expression, document);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid XML expression: " + expression, e);
        }
    }
}
