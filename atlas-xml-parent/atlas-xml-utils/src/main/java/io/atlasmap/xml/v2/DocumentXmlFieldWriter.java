package io.atlasmap.xml.v2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import io.atlasmap.api.AtlasException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 */
public class DocumentXmlFieldWriter extends XmlFieldTransformer {

    public DocumentXmlFieldWriter() {
    }

    public DocumentXmlFieldWriter(Map<String, String> namespaces) {
        super(namespaces);
    }

    public Document write(XmlField xmlField) throws AtlasException {
        if (xmlField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'xmlField' cannot be null"));
        }

        Document document = createDocument();
        write(xmlField, document);
        return document;
    }

    public Document write(List<XmlField> xmlFields) throws AtlasException {
        if (xmlFields == null || xmlFields.isEmpty()) {
            throw new AtlasException(new IllegalArgumentException("Argument 'xmlFields' cannot be null nor empty"));
        }
        Document document = createDocument();
        write(xmlFields, document);
        return document;
    }

    public void write(XmlField xmlField, Document document) throws AtlasException {
        if (xmlField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'xmlField' cannot be null nor empty"));
        }
        if (document == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'document' cannot be null nor empty"));
        }

        //check to see if the seed document has namespaces
        seedDocumentNamespaces(document);
        doWrite(xmlField, document);
    }

    public void write(List<XmlField> xmlFields, Document document) throws AtlasException {
        if (xmlFields == null || xmlFields.isEmpty()) {
            throw new AtlasException(new IllegalArgumentException("Argument 'xmlFields' cannot be null nor empty"));
        }
        if (document == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'document' cannot be null nor empty"));
        }

        //check to see if the seed document has namespaces
        seedDocumentNamespaces(document);
        xmlFields.stream().filter(Objects::nonNull).forEach(xmlField -> doWrite(xmlField, document));
    }

    private void doWrite(XmlField xmlField, Document document) {
        //given the xmlPath find/create the Document Node and update its value.
        String xmlPath = xmlField.getPath();
        String attr = null;
        LinkedList<String> elements = getElementsInXmlPath(xmlPath);
        XmlPathCoordinate attrXmlPathCoordinate = null;
        //check the last, is it an attribute?
        if (elements.getLast().contains("@")) {
            attr = elements.getLast().replaceAll("@", "");
            attrXmlPathCoordinate = createXmlPathCoordinates(Collections.singletonList(elements.getLast())).get(0);
            //we no longer need this reference
            elements.removeLast();
        }
        LinkedList<XmlPathCoordinate> xmlPathCoordinates = (LinkedList<XmlPathCoordinate>) createXmlPathCoordinates(elements);
        // the first coordinate sets the 'root' node
        XmlPathCoordinate root = xmlPathCoordinates.getFirst();
        Node node;
        NodeList nodes = document.getElementsByTagName(root.getElementName());
        if (nodes == null || nodes.getLength() == 0) {
            node = buildRootNode(document, root);
        } else {
            //should be the root
            node = nodes.item(0);
        }

        for (XmlPathCoordinate xmlPathCoordinate : xmlPathCoordinates.subList(1, xmlPathCoordinates.size())) {
            Node childNode = getChildNodeForWrite(node, xmlPathCoordinate);
            if (childNode == null && !xmlPathCoordinate.getElementName().contains("@")) {
                childNode = createNamespaceAwareElement(xmlPathCoordinate, document);
                if (node != null) {
                    node.appendChild(childNode);
                }
            }
            node = childNode;
        }

        if (node != null) {
            if (attr != null) {
                //does the attr have a namespace?
                if (attrXmlPathCoordinate != null && attrXmlPathCoordinate.getNamespace() != null) {
                    Map.Entry<String, String> namespace = attrXmlPathCoordinate.getNamespace().entrySet().iterator().next();
                    //check for default namespace
                    if (!namespace.getValue().isEmpty()) {
                        ((Element) node).setAttributeNS(namespace.getKey(), attr, String.valueOf(xmlField.getValue()));
                    } else {
                        ((Element) node).setAttribute(attr, String.valueOf(xmlField.getValue()));
                    }
                } else {
                    ((Element) node).setAttribute(attr, String.valueOf(xmlField.getValue()));
                }
            } else {
                Text nodeVal = document.createTextNode(String.valueOf(xmlField.getValue()));
                node.appendChild(nodeVal);
            }
        }
    }

    private Node buildRootNode(Document document, XmlPathCoordinate xmlPathCoordinate) {
        Node node;
        if (namespaces != null && !namespaces.isEmpty()) {
            node = createNamespaceAwareElement(xmlPathCoordinate, document);
            addRootNodeNamespaces((Element) node);
        } else {
            node = document.createElement(xmlPathCoordinate.getElementName());
        }
        document.appendChild(node);
        return node;
    }

    private void addRootNodeNamespaces(Element element) {
        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                element.setAttributeNS("http://www.w3.org/2000/xmlns/",
                    "xmlns:".concat(entry.getValue()), entry.getKey());
            }
        }
    }

    private Node getChildNodeForWrite(final Node node, final XmlPathCoordinate xmlPathCoordinate) {
        if (!node.hasChildNodes()) {
            return null;
        }
        NodeList nodeList;
        if (xmlPathCoordinate.getNamespace() != null) {
            String namespaceURI = xmlPathCoordinate.getNamespace().entrySet().iterator().next().getKey();
            String elementName = xmlPathCoordinate.getElementName();
            elementName = elementName.substring(elementName.indexOf(":") + 1, elementName.length());
            nodeList = ((Element) node).getElementsByTagNameNS(namespaceURI, elementName);
        } else {
            nodeList = ((Element) node).getElementsByTagName(xmlPathCoordinate.getElementName());
        }
        if (nodeList != null) {
            return nodeList.item(xmlPathCoordinate.getIndex());
        }
        return null;
    }

    private Document createDocument() throws AtlasException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        if (namespaces != null && !namespaces.isEmpty()) {
            documentBuilderFactory.setNamespaceAware(true);
        }
        Document document;
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new AtlasException(e);
        }
        return document;
    }

    private Node createNamespaceAwareElement(final XmlPathCoordinate xmlPathCoordinate, final Document document) {
        if (xmlPathCoordinate.getNamespace() == null || xmlPathCoordinate.getNamespace().isEmpty()) {
            return document.createElement(xmlPathCoordinate.getElementName());
        }
        Map<String, String> elementNs = xmlPathCoordinate.getNamespace();
        Map.Entry<String, String> ns = elementNs.entrySet().iterator().next(); //should really only be one here
        return document.createElementNS(ns.getKey(), xmlPathCoordinate.getElementName());
    }
}
