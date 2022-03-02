/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.xml.inspect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.XMLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;
import io.atlasmap.xml.core.XmlComplexTypeFactory;
import io.atlasmap.xml.core.XmlPath;
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlFields;
import io.atlasmap.xml.v2.XmlNamespace;
import io.atlasmap.xml.v2.XmlNamespaces;

public class XmlInstanceInspector {

    private static final Logger LOG = LoggerFactory.getLogger(XmlInstanceInspector.class);
    private XmlDocument xmlDocument = AtlasXmlModelFactory.createXmlDocument();

    public void inspect(Document document) {
        xmlDocument.setFields(new Fields());
        parseDocument(document.getDocumentElement());
    }

    public XmlDocument getXmlDocument() {
        return xmlDocument;
    }

    private void parseDocument(Node rootNode) {
        if (rootNode.getParentNode() == null || rootNode.getParentNode().getNodeType() != Node.DOCUMENT_NODE) {
            return;
        }

        XmlComplexType rootComplexType = createXmlComplexType(rootNode, null);
        xmlDocument.getFields().getField().add(rootComplexType);
        mapAttributes(rootNode, rootComplexType);
        if (rootNode.hasChildNodes()) {
            mapChildNodes(rootNode.getChildNodes(), rootComplexType);
        }
    }

    private void mapChildNodes(NodeList nodes, XmlComplexType rootComplexType) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node childNode = nodes.item(i);
            if (!Arrays.asList(new Short[]{Node.ELEMENT_NODE, Node.ATTRIBUTE_NODE}).contains(childNode.getNodeType())) {
                continue;
            }
            if (childNode.hasAttributes()) {
                mapAttributes(childNode, rootComplexType);
            }
            if (((Element) childNode).getElementsByTagName("*").getLength() > 0) {
                mapParentNode(childNode, rootComplexType);
            } else {
                mapNodeToXmlField(childNode, rootComplexType);
            }
        }
    }

    private void mapNamespace(Node node) {
        if (xmlDocument.getXmlNamespaces() == null) {
            XmlNamespaces namespaces = new XmlNamespaces();
            xmlDocument.setXmlNamespaces(namespaces);
        }
        List<XmlNamespace> namespaces = xmlDocument.getXmlNamespaces().getXmlNamespace();
        if (namespaces.stream().noneMatch(ns -> {
            if (ns.getAlias() == null) {
                return node.getPrefix() == null && ns.getUri().equals(node.getNamespaceURI());
            }
            return ns.getAlias().equals(node.getPrefix());
        })) {
            XmlNamespace namespace = new XmlNamespace();
            namespace.setAlias(node.getPrefix());
            namespace.setUri(node.getNamespaceURI());
            namespaces.add(namespace);
        };
    }

    private void mapParentNode(Node node, XmlComplexType parent) {
        if (!node.hasChildNodes()) {
            return;
        }
        NodeList childNodes = node.getChildNodes();
        XmlComplexType childParent = null;
        XmlField[] existing = parent.getXmlFields().getXmlField().stream().filter(f ->
            f.getName().equals(node.getNodeName()) && f.getFieldType() == FieldType.COMPLEX).toArray(XmlField[]::new);
        if (existing.length > 0) {
            childParent = (XmlComplexType) existing[0];
            if (existing.length > 1) {
                LOG.warn("Ignoring duplicate complex field '{}'", childParent.getPath());
            }
            updateCollectionType(node.getParentNode(), childParent);
        } else {
            childParent = createXmlComplexType(node, parent);
            parent.getXmlFields().getXmlField().add(childParent);
        }

        if (node.hasAttributes()) {
            mapAttributes(node, childParent);
        }
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node e = childNodes.item(i);
            if (e.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            // do we have child elements?
            NodeList childElements = ((Element) e).getElementsByTagName("*");
            if (childElements.getLength() > 0) {
                mapParentNode(e, childParent);
            } else {
                mapNodeToXmlField(e, childParent);
                if (e.hasAttributes()) {
                    mapAttributes(e, childParent);
                }
            }
        }
        if (node.getNamespaceURI() != null) {
            mapNamespace(node);
        }
    }

    private void mapAttributes(Node node, XmlComplexType xmlComplexType) {
        NamedNodeMap attrs = node.getAttributes();
        if (attrs == null) {
            return;
        }
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attrNode = attrs.item(i);
            // don't map default namespace attribute ...
            if (attrNode.getNamespaceURI() != null
                    && attrNode.getNamespaceURI().equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                continue;
            } else if (attrNode.getNamespaceURI() != null
                    && attrNode.getNamespaceURI().equals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI)) {
                mapNamespace(attrNode);
                xmlComplexType.setTypeName(attrNode.getTextContent());
                continue;
            } else if (attrNode.getNamespaceURI() != null) {
                mapNamespace(attrNode);
            }
            mapNodeToXmlField(attrNode, xmlComplexType);
        }
    }

    private void mapNodeToXmlField(Node node, XmlComplexType parentComplexType) {
        XmlField xmlField = null;
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            XmlField[] existing = parentComplexType.getXmlFields().getXmlField().stream().filter(f ->
                f.getName().equals(node.getNodeName()) && f.isAttribute()).toArray(XmlField[]::new);
            if (existing.length > 0) {
                xmlField = existing[0];
                if (existing.length > 1) {
                    LOG.error("Ignoring duplicated attribute '{}'", xmlField.getPath());
                }
            }
        } else {
            XmlField[] existing = parentComplexType.getXmlFields().getXmlField().stream().filter(f ->
                f.getName().equals(node.getNodeName()) && !f.isAttribute()).toArray(XmlField[]::new);
            if (existing.length > 0) {
                xmlField = existing[0];
                if (existing.length > 1) {
                    LOG.warn("Ignoring duplicated element '{}'", xmlField.getPath());
                }
            }
        }
        if (xmlField == null) {
            xmlField = AtlasXmlModelFactory.createXmlField();
            xmlField.setValue(node.getTextContent());
            xmlField.setFieldType(FieldType.STRING);
            xmlField.setName(node.getNodeName());
            xmlField.setStatus(FieldStatus.SUPPORTED);
            xmlField.setAttribute(node.getNodeType() == Node.ATTRIBUTE_NODE);
            parentComplexType.getXmlFields().getXmlField().add(xmlField);
            StringBuffer fieldPath = new StringBuffer();
            if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                fieldPath.append(XmlPath.PATH_ATTRIBUTE_PREFIX);
            }
            fieldPath.append(node.getNodeName());
            XmlPath path = new XmlPath(parentComplexType.getPath());
            path.appendField(fieldPath.toString());
            xmlField.setPath(path.toString());
        }
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            updateCollectionType(node.getParentNode(), xmlField);
        }
        if (node.getNamespaceURI() != null) {
            mapNamespace(node);
        }
    }

    private XmlComplexType createXmlComplexType(Node childNode, XmlComplexType parentField) {
        XmlComplexType childComplexType = XmlComplexTypeFactory.createXmlComlexField();
        childComplexType.setXmlFields(new XmlFields());
        childComplexType.setName(childNode.getNodeName());
        XmlPath path = null;
        if (parentField == null) {
            path = new XmlPath(XmlPath.PATH_SEPARATOR + childNode.getNodeName());
        } else {
            path = new XmlPath(parentField.getPath());
            Element parentElement = (Element)childNode.getParentNode();
            if (isCollection(parentElement, childNode.getNodeName())) {
                childComplexType.setCollectionType(CollectionType.LIST);
                path.appendField(childNode.getNodeName() + XmlPath.PATH_LIST_START + XmlPath.PATH_LIST_END);
            } else {
                childComplexType.setCollectionType(CollectionType.NONE);
                path.appendField(childNode.getNodeName());
            }
        }
        childComplexType.setPath(path.toString());
        return childComplexType;
    }

    private void updateCollectionType(Node parentNode, XmlField field) {
        if (field.getCollectionType() == CollectionType.LIST
            || parentNode.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        if (!isCollection((Element)parentNode, field.getName())) {
            return;
        }

        field.setCollectionType(CollectionType.LIST);
        field.setPath(field.getPath() + XmlPath.PATH_LIST_START + XmlPath.PATH_LIST_END);
        // Propagate parent collection to descendants
        if (field instanceof XmlComplexType) {
            XmlComplexType complex = (XmlComplexType)field;
            for (XmlField child : complex.getXmlFields().getXmlField()) {
                updateFieldPathFromParent(child, complex);
            }
        }
    }

    private boolean isCollection(Element parent, String name) {
        NodeList siblings = parent.getChildNodes();
        List<Element> dups = new ArrayList<>();
        for (int i=0; i<siblings.getLength(); i++) {
            Node n = siblings.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(name)) {
                dups.add((Element)n);
            }
        }
        return dups.size() > 1;
    }

    private void updateFieldPathFromParent(XmlField child, XmlComplexType parent) {
        XmlPath oldPath = new XmlPath(child.getPath());
        XmlPath newPath = new XmlPath(parent.getPath());
        newPath.appendField(oldPath.getLastSegment().getExpression());
        child.setPath(newPath.toString());
        if (child instanceof XmlComplexType) {
            for (XmlField grandChild : ((XmlComplexType)child).getXmlFields().getXmlField()) {
                updateFieldPathFromParent(grandChild, (XmlComplexType)child);
            }
        }
    }

}
