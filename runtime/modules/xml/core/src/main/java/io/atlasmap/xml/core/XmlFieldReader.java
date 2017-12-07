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

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlField;

public class XmlFieldReader extends XmlFieldTransformer implements AtlasFieldReader {

    private static final Logger LOG = LoggerFactory.getLogger(XmlFieldReader.class);

    private AtlasConversionService conversionService;
    private Document document;

    public XmlFieldReader(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public XmlFieldReader(Map<String, String> namespaces) {
        super(namespaces);
    }

    public void read(AtlasInternalSession session) throws AtlasException {
        if (document == null) {
            throw new AtlasException(new IllegalArgumentException("'document' cannot be null"));
        }
        Field field = session.head().getSourceField();
        if (field == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'field' cannot be null"));
        }

        seedDocumentNamespaces(document);
        XmlField xmlField = XmlField.class.cast(field);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading source value for field: " + xmlField.getPath());
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
                LOG.debug("Parent element is currently: " + XmlIOHelper.writeDocumentToString(true, parentNode));
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
                List<Element> children = XmlIOHelper.getChildrenWithName(childrenElementName, parentNode);
                if (children == null || children.isEmpty()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skipping source value set, couldn't find children with name '" + childrenElementName
                                + "', for segment: " + sc);
                    }
                    return;
                }
                parentNode = children.get(0);
                if (XmlPath.isCollectionSegment(sc.getSegment())) {
                    int index = XmlPath.indexOfSegment(sc.getSegment());
                    if (index >= children.size()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Skipping source value set, children list can't fit index " + index
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

                if (value == null) {
                    return;
                }

                if (xmlField.getFieldType() == null) {
                    xmlField.setValue(value);
                    xmlField.setFieldType(FieldType.STRING);
                } else {
                    Object convertedValue;
                    try {
                        convertedValue = conversionService.convertType(value, FieldType.STRING,
                                xmlField.getFieldType());
                        xmlField.setValue(convertedValue);
                    } catch (AtlasConversionException e) {
                        AtlasUtil.addAudit(session, xmlField.getDocId(),
                                String.format("Failed to convert field value '%s' into type '%s'", value,
                                        xmlField.getFieldType()),
                                xmlField.getPath(), AuditStatus.ERROR, value);
                    }
                }
            }
        }
    }

    public void setDocument(String docString, boolean namespaced) throws AtlasException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(namespaced); // this must be done to use namespaces
            DocumentBuilder b = dbf.newDocumentBuilder();
            this.document = b.parse(new ByteArrayInputStream(docString.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

}
