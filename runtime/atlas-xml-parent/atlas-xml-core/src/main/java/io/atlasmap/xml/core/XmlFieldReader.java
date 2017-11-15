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
package io.atlasmap.xml.core;

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import io.atlasmap.core.PathUtil;
import io.atlasmap.core.PathUtil.SegmentContext;

public class XmlFieldReader extends XmlFieldTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(XmlFieldReader.class);

    public XmlFieldReader() {
    }

    public XmlFieldReader(Map<String, String> namespaces) {
        super(namespaces);
    }

    public void readNew(final Document document, final XmlField xmlField) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading input value for field: " + xmlField.getPath());
        }
        Element parentNode = document.getDocumentElement();
        for (SegmentContext sc : new PathUtil(xmlField.getPath()).getSegmentContexts(false)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Now processing segment: " + sc.getSegment());
                LOG.debug("Parent element is currently: " + XmlFieldWriter.writeDocumentToString(true, parentNode));
            }
            if (sc.getPrev() == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping root segment: " + sc);
                }
                // processing root node part of path such as the "XOA" part of
                // "/XOA/contact<>/firstName", skip.
                continue;
            }

            if (!PathUtil.isAttributeSegment(sc.getSegment())) {
                String childrenElementName = PathUtil.cleanPathSegment(sc.getSegment());
                String namespaceAlias = PathUtil.getNamespace(sc.getSegment());
                if (namespaceAlias != null && !"".equals(namespaceAlias)) {
                    childrenElementName = namespaceAlias + ":" + childrenElementName;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Looking for children elements with name: " + childrenElementName);
                }
                List<Element> children = XmlFieldWriter.getChildrenWithName(childrenElementName, parentNode);
                if (children == null || children.isEmpty()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skipping input value set, couldn't find children with name '"
                                + childrenElementName + "', for segment: " + sc);
                    }
                    return;
                }
                parentNode = children.get(0);
                if (PathUtil.isCollectionSegment(sc.getSegment())) {
                    int index = PathUtil.indexOfSegment(sc.getSegment());
                    if (index >= children.size()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Skipping input value set, children list can't fit index " + index
                                    + ", children list size: " + children.size());
                        }
                        return;
                    }
                    parentNode = children.get(index);
                }
            }
            if (sc.getNext() == null) { // last segment.
                String value = parentNode.getTextContent();
                if (PathUtil.isAttributeSegment(sc.getSegment())) {
                    String attributeName = PathUtil.cleanPathSegment(sc.getSegment());
                    value = parentNode.getAttribute(attributeName);
                }
                if (xmlField.getFieldType() == null || FieldType.STRING.equals(xmlField.getFieldType())) {
                    xmlField.setValue(value);
                    xmlField.setFieldType(FieldType.STRING);
                } else if (FieldType.CHAR.equals(xmlField.getFieldType())) {
                    xmlField.setValue(value.charAt(0));
                }

                if (value != null) {
                    if (FieldType.BOOLEAN.equals(xmlField.getFieldType())) {
                        xmlField.setValue(processXmlStringAsBoolean(value));
                    } else {
                        LOG.warn(String.format("Unsupported FieldType for text data t=%s p=%s docId=%s",
                                xmlField.getFieldType().value(), xmlField.getPath(), xmlField.getDocId()));
                    }
                }
                return;
            }
        }
    }

    public void read(final Document document, final XmlField xmlField) throws AtlasException {
        if (document == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'document' cannot be null"));
        }
        if (xmlField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'xmlField' cannot be null"));
        }

        // check to see if the document has namespaces
        seedDocumentNamespaces(document);
        // read the Document using xmlPath field to set the value on the XmlField
        String xmlPath = xmlField.getPath();
        String attr = null;
        LinkedList<String> elements = getElementsInXmlPath(xmlPath);
        LinkedList<XmlPathCoordinate> xmlPathCoordinates = (LinkedList<XmlPathCoordinate>) createXmlPathCoordinates(
                elements);
        // is the last coordinate an attribute?
        if (xmlPathCoordinates.getLast().getElementName().startsWith("@")) {
            attr = xmlPathCoordinates.getLast().getElementName().replace("@", "").trim();
            xmlPathCoordinates.removeLast();
        }
        // the first coordinate sets the 'root' node including the namespace we are
        // working with...
        XmlPathCoordinate root = xmlPathCoordinates.getFirst();
        NodeList nodes = getNodeList(document, root);
        Node node = nodes.item(root.getIndex());
        if (node != null) {
            // find the (grand)child node that has the value to set
            for (XmlPathCoordinate xmlPathCoordinate : xmlPathCoordinates.subList(1, xmlPathCoordinates.size())) {
                node = getChildNodeForRead(node, xmlPathCoordinate);
            }
            if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;
                String value;
                if (attr != null) {
                    if (attr.contains(":")) {
                        String[] attrSplit = attr.split(":");
                        String namespace = findNamespaceURIFromPrefix(attrSplit[0]);
                        value = e.getAttributeNS(namespace, attrSplit[1]);
                    } else {
                        value = e.getAttribute(attr);
                    }
                } else {
                    value = e.getTextContent();
                }
                if (xmlField.getFieldType() == null || FieldType.STRING.equals(xmlField.getFieldType())) {
                    xmlField.setValue(value);
                    xmlField.setFieldType(FieldType.STRING);
                } else if (FieldType.CHAR.equals(xmlField.getFieldType())) {
                    xmlField.setValue(value.charAt(0));
                }

                if (value != null) {
                    if (FieldType.BOOLEAN.equals(xmlField.getFieldType())) {
                        xmlField.setValue(processXmlStringAsBoolean(value));
                    } else {
                        LOG.warn(String.format("Unsupported FieldType for text data t=%s p=%s docId=%s",
                                xmlField.getFieldType().value(), xmlField.getPath(), xmlField.getDocId()));
                    }
                }
            }
        }
    }

    public static Boolean processXmlStringAsBoolean(String value) {
        if (value == null) {
            return null;
        }

        if ("true".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value)) {
            return Boolean.TRUE;
        }

        if ("false".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value)) {
            return Boolean.FALSE;
        }

        return null;
    }

    public void read(final Document document, final List<XmlField> xmlFields) throws AtlasException {
        if (xmlFields == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'xmlFields' cannot be null"));
        }
        // check to see if the document has namespaces
        seedDocumentNamespaces(document);
        for (XmlField xmlField : xmlFields) {
            read(document, xmlField);
        }
    }

    private Node getChildNodeForRead(final Node node, final XmlPathCoordinate xmlPathCoordinate) {
        if (!node.hasChildNodes() || node.getNodeType() != Node.ELEMENT_NODE) {
            return node;
        }
        Node returnNode;
        Map.Entry<String, String> namespace = null;
        Element element = (Element) node;
        String childName = xmlPathCoordinate.getElementName();
        if (xmlPathCoordinate.getNamespace() != null) {
            namespace = xmlPathCoordinate.getNamespace().entrySet().iterator().next();
        }
        if (childName.contains(":")) {
            childName = childName.substring(childName.indexOf(":") + 1, childName.length());
        }
        if (namespace != null) {
            returnNode = element.getElementsByTagNameNS(namespace.getKey(), childName)
                    .item(xmlPathCoordinate.getIndex());
        } else {
            returnNode = element.getElementsByTagName(childName).item(xmlPathCoordinate.getIndex());
        }
        return returnNode;
    }

    private NodeList getNodeList(Document document, XmlPathCoordinate root) {
        NodeList nodes = null;
        // if the document does not have namespacing but the paths do....
        if ((namespaces != null && namespaces.isEmpty())
                && (root.getElementName().contains(":") && root.getNamespace() == null)) {
            // strip out the namespace from the root coordinate path
            String correctedElement = root.getElementName().substring(root.getElementName().indexOf(":") + 1,
                    root.getElementName().length());
            nodes = document.getElementsByTagName(correctedElement);
        } else if ((namespaces != null && !namespaces.isEmpty())
                && (!root.getElementName().contains(":") && root.getNamespace() == null)) {
            // if the document has namespaces but the paths don't
            for (Map.Entry<String, String> namespaceEntry : namespaces.entrySet()) {
                root.setNamespace(namespaceEntry.getKey(), namespaceEntry.getValue());
                nodes = document.getElementsByTagNameNS(namespaceEntry.getKey(), root.getElementName());
                // we found the element with the namespace
                if (nodes.getLength() > 0) {
                    break;
                }
            }
        } else {
            // default to find nodes without namespaces
            nodes = document.getElementsByTagName(root.getElementName());
        }
        return nodes;
    }

    public Integer getCollectionCount(Document document, XmlField xmlField, String collectionSegment)
            throws AtlasException {
        if (document == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'document' cannot be null"));
        }
        if (xmlField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'xmlField' cannot be null"));
        }

        // check to see if the document has namespaces
        seedDocumentNamespaces(document);
        // read the Document using xmlPath field to set the value on the XmlField
        String xmlPath = xmlField.getPath();
        LinkedList<String> elements = getElementsInXmlPath(xmlPath);
        LinkedList<XmlPathCoordinate> xmlPathCoordinates = (LinkedList<XmlPathCoordinate>) createXmlPathCoordinates(
                elements);
        // is the last coordinate an attribute?
        if (xmlPathCoordinates.getLast().getElementName().startsWith("@")) {
            xmlPathCoordinates.removeLast();
        }
        // the first coordinate sets the 'root' node including the namespace we are
        // working with...
        for (XmlPathCoordinate xc : xmlPathCoordinates) {
            if (xc.getElementName().equals(collectionSegment)) {
                NodeList nodes = getNodeList(document, xc);
                return Integer.valueOf(nodes.getLength());
            }
        }

        return null;
    }
}
