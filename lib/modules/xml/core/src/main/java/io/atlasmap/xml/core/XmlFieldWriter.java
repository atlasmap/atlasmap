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
package io.atlasmap.xml.core;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasFieldWriter;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.core.XmlPath.XmlSegmentContext;

public class XmlFieldWriter extends XmlFieldTransformer implements AtlasFieldWriter {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(XmlFieldWriter.class);

    private Document document = null;
    private boolean enableElementNamespaces = true;
    private boolean enableAttributeNamespaces = true;
    private boolean ignoreMissingNamespaces = true;

    public XmlFieldWriter() throws AtlasException {
        this(XmlFieldWriter.class.getClassLoader(), new HashMap<>(), null);
    }

    public XmlFieldWriter(ClassLoader classLoader, Map<String, String> namespaces, String seedDocument) throws AtlasException {
        super(classLoader, namespaces);
        this.classLoader = classLoader;
        this.document = createDocument(namespaces, seedDocument);
        // check to see if the seed document has namespaces
        seedDocumentNamespaces(document);
    }

    @Override
    public void write(AtlasInternalSession session) throws AtlasException {
        Field targetField = session.head().getTargetField();
        if (targetField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'field' cannot be null"));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Now processing field path={} type={} value={}", targetField.getPath(), targetField.getFieldType(),
                    targetField.getValue());
        }

        XmlPath path = new XmlPath(targetField.getPath());
        XmlSegmentContext lastSegment = path.getLastSegment();
        Element parentNode = null;
        XmlSegmentContext parentSegment = null;
        for (XmlSegmentContext segment : path.getXmlSegments(false)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Now processing segment: {}", segment);
                LOG.debug("Parent element is currently: {}", xmlHelper.writeDocumentToString(true, parentNode));
            }
            if (parentNode == null) {
                // processing root node
                parentNode = document.getDocumentElement();
                if (parentNode == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Creating root element with name: {}", segment.getName());
                    }
                    // no root node exists yet, create root node with this segment name;
                    Element rootNode = createElement(segment);
                    addNamespacesToElement(rootNode, namespaces);
                    document.appendChild(rootNode);
                    parentNode = rootNode;
                } else if (!(parentNode.getNodeName().equals(segment.getQName()))) {
                    // make sure root element's name matches.
                    throw new AtlasException(String.format(
                            "Root element name '%s' does not match expected name '%s' from path: %s",
                            parentNode.getNodeName(), segment.getName(), targetField.getPath()));
                }
                parentSegment = segment;
            } else {
                if (LOG.isDebugEnabled()) {
                    if (segment.equals(lastSegment)) {
                        LOG.debug("Now processing field value segment: {}", segment);
                    } else {
                        LOG.debug("Now processing parent segment: {}", segment);
                    }
                }

                if (segment.equals(lastSegment) && targetField.getValue() == null) {
                    break;
                }

                if (!segment.isAttribute()) {
                    // if current segment of path isn't attribute, it refers to a child element,
                    // find it or create it..
                    Element childNode = getChildNode(parentNode, parentSegment, segment);
                    if (childNode == null && targetField.getStatus() != FieldStatus.NOT_FOUND) {
                        childNode = createParentNode(parentNode, parentSegment, segment);
                    }
                    if (childNode == null) {
                        return;
                    }
                    parentNode = childNode;
                    parentSegment = segment;
                }

                if (segment.equals(lastSegment)) {
                    writeValue(parentNode, segment, targetField);
                }
            }
        }
    }

    private void addNamespacesToElement(Element node, Map<String, String> namespaces) {
        for (String namespaceAlias : namespaces.keySet()) {
            String namespaceUri = namespaces.get(namespaceAlias);
            String attributeName = "xmlns";
            if (namespaceAlias != null && !namespaceAlias.equals("")) {
                attributeName += ":" + namespaceAlias;
            }
            node.setAttributeNS("http://www.w3.org/2000/xmlns/", attributeName, namespaceUri);
        }
    }

    private void writeValue(Element parentNode, XmlSegmentContext segment, Field field) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Writing field value in parent node '{}', parentNode: {}",
                    segment, xmlHelper.writeDocumentToString(true, parentNode));
        }
        String value = convertValue(field);
        if (segment.isAttribute()) {
            if (this.enableAttributeNamespaces) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Attribute namespaces are enabled, determining namespace.");
                }
                String namespaceAlias = null;
                String namespaceUri = null;
                if (segment.getNamespace() != null) {
                    namespaceAlias = segment.getNamespace();
                    namespaceUri = this.namespaces.get(namespaceAlias);
                    LOG.debug("Parsed namespace alias '{}', from segment '{}', namespaceUri: {}",
                            namespaceAlias, segment, namespaceUri);
                }
                if (!this.ignoreMissingNamespaces && namespaceUri == null) {
                    throw new AtlasException(String.format(
                            "Cannot find namespace URI for attribute: '%s', available namespaces: %s",
                            segment, this.namespaces));
                }
                if (namespaceUri != null) {
                    parentNode.setAttributeNS(namespaceUri, namespaceAlias + ":" + segment.getName(), value);
                } else {
                    parentNode.setAttribute(segment.getName(), value);
                }
            } else {
                parentNode.setAttribute(segment.getName(), value);
            }
        } else { // set element value
            parentNode.setTextContent(value);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parent node after value written: {}", xmlHelper.writeDocumentToString(true, parentNode));
        }
    }

    private Element getChildNode(Element parentNode, XmlSegmentContext parentSegment, XmlSegmentContext segment) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for child node '{}' in parent '{}': {}",
                    segment, parentSegment, xmlHelper.writeDocumentToString(true, parentNode));
        }
        if (parentNode == null) {
            return null;
        }
        String cleanedSegmentName = segment.getName();
        String namespaceAlias = segment.getNamespace();
        if (namespaceAlias != null && !namespaceAlias.isEmpty()) {
            cleanedSegmentName = namespaceAlias + ":" + cleanedSegmentName;
        }
        List<Element> children = XmlIOHelper.getChildrenWithName(cleanedSegmentName, parentNode);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found {} children in '{}' with the name '{}'",
                    children.size(), parentSegment, cleanedSegmentName);
        }
        Element childNode = children.size() > 0 ? children.get(0) : null;
        if (children.size() > 0 && segment.getCollectionType() != CollectionType.NONE) {
            Integer index = segment.getCollectionIndex();
            if(index == null) {
                // no collection entry - it will only create parent nodes of the collection
                return null;
            }
            childNode = null;
            if (children.size() > index) {
                childNode = children.get(index);
            }
        }
        if (LOG.isDebugEnabled()) {
            if (childNode == null) {
                LOG.debug("Could not find child node '{}' in parent '{}'", segment, parentSegment);
            } else {
                LOG.debug("Found child node '{}' in parent '{}', class: {}, node: {}",
                        segment, parentSegment, childNode.getClass().getName(), xmlHelper.writeDocumentToString(true, childNode));
            }
        }
        return childNode;
    }

    private Element createParentNode(Element parentNode, XmlSegmentContext parentSegment, XmlSegmentContext segment) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating parent node '{}' under previous parent '{}'.", segment, parentSegment);
        }
        Element childNode = null;
        String cleanedSegmentName = segment.getName();
        if (segment.getCollectionType() != CollectionType.NONE) {
            Integer index = segment.getCollectionIndex();
            if (index == null) {
                return null;
            }
            String namespaceAlias = segment.getNamespace();
            if (namespaceAlias != null && !"".equals(namespaceAlias)) {
                cleanedSegmentName = namespaceAlias + ":" + cleanedSegmentName;
            }

            List<Element> children = XmlIOHelper.getChildrenWithName(cleanedSegmentName, parentNode);

            if (children.size() < (index + 1)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Child Element Array is too small, resizing to accomodate index: {}, current array: {}", index, children);
                }
                // if our array doesn't have index + 1 items in it, add objects until we have
                // the index available
                while (children.size() < (index + 1)) {
                    Element child = (Element) parentNode.appendChild(createElement(segment));
                    children.add(child);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Child Element Array after resizing: {}", children);
                }
            }
            children = XmlIOHelper.getChildrenWithName(cleanedSegmentName, parentNode);
            childNode = children.get(index);
        } else {
            childNode = (Element) parentNode.appendChild(createElement(segment));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Parent Node '{}' after adding child parent node '{}': {}",
                    parentSegment, segment, xmlHelper.writeDocumentToString(true, parentNode));
        }
        return childNode;
    }

    private Element createElement(XmlSegmentContext segment) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating element for segment '{}'.", segment);
        }
        if (this.enableElementNamespaces) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Element namespaces are enabled, determining namespace.");
            }
            String namespaceAlias = null;
            String namespaceUri = null;
            if (segment.getNamespace() != null) {
                namespaceAlias = segment.getNamespace();
                namespaceUri = this.namespaces.get(namespaceAlias);
                LOG.debug("Parsed namespace alias '{}', from segment '{}', namespaceUri: {}, known namespaces: {}",
                        namespaceAlias, segment, namespaceUri, this.namespaces);
            }
            if (!this.ignoreMissingNamespaces && namespaceUri == null) {
                throw new AtlasException(String.format(
                        "Cannot find namespace URI for element: '%s', available namespaces: %s",
                        segment, this.namespaces));
            }
            if (namespaceUri != null) {
                return document.createElementNS(namespaceUri, namespaceAlias + ":" + segment.getName());
            }
        }
        return document.createElement(segment.getName());
    }

    private String convertValue(Field field) {
        FieldType type = field.getFieldType();
        Object originalValue = field.getValue();
        String value = originalValue != null ? String.valueOf(originalValue) : null;
        if (LOG.isDebugEnabled()) {
            String valueClass = originalValue == null ? "null" : originalValue.getClass().getName();
            LOG.debug("Converted field value. Type: {}, originalValue: {}({}), to: '{}",
                    type, originalValue, valueClass, value);
        }
        return value;
    }

    private Document createDocument(Map<String, String> namespaces, String seedDocument) throws AtlasException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            if (namespaces != null && !namespaces.isEmpty()) {
                documentBuilderFactory.setNamespaceAware(true);
            }
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            if (seedDocument != null && !seedDocument.isEmpty()) {
                Document document = documentBuilder.parse(new ByteArrayInputStream(seedDocument.getBytes("UTF-8")));

                Element rootNode = document.getDocumentElement();

                // extract namespaces from seed document
                NamedNodeMap attributes = rootNode.getAttributes();
                if (attributes != null) {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        Node n = attributes.item(i);
                        String nodeName = n.getNodeName();
                        if (nodeName != null && nodeName.startsWith("xmlns")) {
                            String namespaceAlias = "";
                            if (nodeName.contains(":")) {
                                namespaceAlias = nodeName.substring(nodeName.indexOf(":") + 1);
                            }
                            if (!namespaces.containsKey(namespaceAlias)) {
                                namespaces.put(namespaceAlias, n.getNodeValue());
                            }
                        }
                    }
                }

                // rewrite root element to contain user-specified namespaces
                if (namespaces.size() > 0) {
                    Element oldRootNode = rootNode;
                    rootNode = (Element) oldRootNode.cloneNode(true);
                    addNamespacesToElement(rootNode, namespaces);
                    document.removeChild(oldRootNode);
                    document.appendChild(rootNode);
                }

                return document;
            }
            return documentBuilder.newDocument();
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    public Document getDocument() {
        return document;
    }

    public boolean isEnableElementNamespaces() {
        return enableElementNamespaces;
    }

    public void setEnableElementNamespaces(boolean enableElementNamespaces) {
        this.enableElementNamespaces = enableElementNamespaces;
    }

    public boolean isEnableAttributeNamespaces() {
        return enableAttributeNamespaces;
    }

    public void setEnableAttributeNamespaces(boolean enableAttributeNamespaces) {
        this.enableAttributeNamespaces = enableAttributeNamespaces;
    }

    public boolean isIgnoreMissingNamespaces() {
        return ignoreMissingNamespaces;
    }

    public void setIgnoreMissingNamespaces(boolean ignoreMissingNamespaces) {
        this.ignoreMissingNamespaces = ignoreMissingNamespaces;
    }

}
