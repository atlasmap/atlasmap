/**
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

import javax.xml.XMLConstants;

import org.w3c.dom.Attr;
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
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlFields;
import io.atlasmap.xml.v2.XmlNamespace;
import io.atlasmap.xml.v2.XmlNamespaces;

public class XmlInstanceInspector {

    private XmlDocument xmlDocument = AtlasXmlModelFactory.createXmlDocument();

    public void inspect(Document document) {
        xmlDocument.setFields(new Fields());
        parseDocument(document.getDocumentElement());
    }

    public XmlDocument getXmlDocument() {
        return xmlDocument;
    }

    private void parseDocument(Node rootNode) {
        if (rootNode.getParentNode() != null && rootNode.getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
            XmlComplexType rootComplexType = getXmlComplexType(rootNode);
            xmlDocument.getFields().getField().add(rootComplexType);
            mapAttributes(rootNode, rootComplexType);
            if (rootNode.hasChildNodes()) {
                mapChildNodes(rootNode.getChildNodes(), rootComplexType);
            }
        }
    }

    private void mapChildNodes(NodeList nodes, XmlComplexType rootComplexType) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node childNode = nodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE || childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                if (childNode.hasAttributes()) {
                    mapAttributes(childNode, rootComplexType);
                }
                if (((Element) childNode).getElementsByTagName("*").getLength() > 0) {
                    mapParentNode(childNode, rootComplexType);
                } else {
                    mapNodeToXmlField(childNode, rootComplexType);
                }
                if (childNode.getNamespaceURI() != null) {
                    mapNamespace(childNode);
                }
            }
        }
    }

    private void mapNamespace(Node node) {
        if (xmlDocument.getXmlNamespaces() == null) {
            XmlNamespaces namespaces = new XmlNamespaces();
            xmlDocument.setXmlNamespaces(namespaces);
        }
        XmlNamespace namespace = new XmlNamespace();
        namespace.setAlias(node.getPrefix());
        namespace.setUri(node.getNamespaceURI());
        if (!xmlDocument.getXmlNamespaces().getXmlNamespace().contains(namespace)) {
            xmlDocument.getXmlNamespaces().getXmlNamespace().add(namespace);
        }
    }

    private void mapParentNode(Node node, XmlComplexType parent) {
        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            XmlComplexType childParent = getXmlComplexType(node);

            StringBuffer stringBuffer = new StringBuffer();
            getXmlPath(node, stringBuffer);

            if (node.hasAttributes()) {
                mapAttributes(node, childParent);
            }
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node e = childNodes.item(i);
                if (e.getNodeType() == Node.ELEMENT_NODE) {
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
            }
            mapCollectionType(childParent, (Element) node);
            parent.getXmlFields().getXmlField().add(childParent);
        }
    }

    private void mapAttributes(Node node, XmlComplexType xmlComplexType) {
        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
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
    }

    private void mapNodeToXmlField(Node node, XmlComplexType parentComplexType) {
        XmlField xmlField = AtlasXmlModelFactory.createXmlField();
        StringBuffer sb = new StringBuffer(1024);
        getXmlPath(node, sb);
        xmlField.setPath(sb.toString());
        xmlField.setValue(node.getTextContent());
        xmlField.setFieldType(FieldType.STRING);
        xmlField.setName(node.getNodeName());
        xmlField.setStatus(FieldStatus.SUPPORTED);
        parentComplexType.getXmlFields().getXmlField().add(xmlField);
    }

    private void mapCollectionType(XmlComplexType childParent, Element e) {
        if (e.hasChildNodes()) {
            NodeList children = e.getChildNodes();
            // immediate child element
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList childElements = e.getElementsByTagName(child.getNodeName());
                    if (childElements.getLength() > 1) {
                        childParent.setCollectionType(CollectionType.LIST);
                    }
                    break;
                }
            }
        }
    }

    private void getXmlPath(Node node, StringBuffer sb) {
        int index;
        if (node.getParentNode() != null && node.getParentNode().getNodeType() == Node.ELEMENT_NODE) {
            getXmlPath(node.getParentNode(), sb);
        }
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            Node owner = ((Attr) node).getOwnerElement();
            if (owner.getParentNode() != null && owner.getParentNode().getNodeType() == Node.ELEMENT_NODE) {
                getXmlPath(owner.getParentNode(), sb);
            }
            index = getNodeIndex(owner);
            sb.append("/").append(owner.getNodeName());
            if (index > 0) {
                sb.append("[").append(index).append("]");
            }
            sb.append("/");
            if (node.getPrefix() != null) {
                sb.append(node.getPrefix()).append(":");
            }
            sb.append("@").append(node.getLocalName());
        } else {
            index = getNodeIndex(node);
            sb.append("/").append(node.getNodeName());
            if (index > 0) {
                sb.append("[").append(index).append("]");
            }
        }
    }

    private int getNodeIndex(Node node) {
        if (node.getParentNode().getNodeType() == Node.ELEMENT_NODE) {
            Element parent = (Element) node.getParentNode();
            // find my index
            NodeList siblings = parent.getElementsByTagName(node.getNodeName());
            if (siblings != null) {
                for (int i = 0; i < siblings.getLength(); i++) {
                    Node nextSibling = siblings.item(i);
                    if (nextSibling.isSameNode(node)) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    private XmlComplexType getXmlComplexType(Node childNode) {
        XmlComplexType childComplexType = XmlComplexTypeFactory.createXmlComlexField();
        childComplexType.setXmlFields(new XmlFields());
        childComplexType.setName(childNode.getNodeName());
        StringBuffer stringBuffer = new StringBuffer();
        getXmlPath(childNode, stringBuffer);
        childComplexType.setPath(stringBuffer.toString());
        return childComplexType;
    }

}
