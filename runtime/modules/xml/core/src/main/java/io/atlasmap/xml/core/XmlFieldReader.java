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

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlField;

public class XmlFieldReader extends XmlFieldTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(XmlFieldReader.class);

    public XmlFieldReader() {
    }

    public XmlFieldReader(Map<String, String> namespaces) {
        super(namespaces);
    }

    public void read(final Document document, final XmlField xmlField) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading input value for field: " + xmlField.getPath());
        }
        if (document == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'document' cannot be null"));
        }
        if (xmlField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'xmlField' cannot be null"));
        }
        Element parentNode = document.getDocumentElement();
        for (SegmentContext sc : new XmlPath(xmlField.getPath()).getSegmentContexts(false)) {
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

            if (!XmlPath.isAttributeSegment(sc.getSegment())) {
                String childrenElementName = XmlPath.cleanPathSegment(sc.getSegment());
                String namespaceAlias = XmlPath.getNamespace(sc.getSegment());
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
                if (XmlPath.isCollectionSegment(sc.getSegment())) {
                    int index = XmlPath.indexOfSegment(sc.getSegment());
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
                if (XmlPath.isAttributeSegment(sc.getSegment())) {
                    String attributeName = XmlPath.getAttribute(sc.getSegment());
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

}
