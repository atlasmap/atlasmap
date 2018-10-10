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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlDataSource;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlNamespace;
import io.atlasmap.xml.v2.XmlNamespaces;

public class XmlFieldReader extends XmlFieldTransformer implements AtlasFieldReader {

    private static final Logger LOG = LoggerFactory.getLogger(XmlFieldReader.class);

    private AtlasConversionService conversionService;
    private Document document;

    @SuppressWarnings("unused")
    private XmlFieldReader() {
    }

    public XmlFieldReader(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public XmlFieldReader(AtlasConversionService conversionService, Map<String, String> namespaces) {
        super(namespaces);
        this.conversionService = conversionService;
    }

    public Field read(AtlasInternalSession session) throws AtlasException {
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
        Optional<XmlNamespaces> xmlNamespaces = getSourceNamespaces(session, xmlField);
        SegmentContext lastSegment = null;
        List<Element> parentNodes = Arrays.asList(document.getDocumentElement());
        for (SegmentContext sc : new XmlPath(xmlField.getPath()).getSegmentContexts(false)) {
            lastSegment = sc;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Now processing segment: " + sc.getSegment());
            }
            if (sc.getPrev() == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping root segment: " + sc);
                }
                // processing root node part of path such as the "XOA" part of
                // "/XOA/contact<>/firstName", skip.
                continue;
            }
            parentNodes = extractSegment(parentNodes, sc, xmlNamespaces);
        }
        if (lastSegment != null) {
            readValue(session, parentNodes, lastSegment, xmlField);
        }
        return session.head().getSourceField();
    }

    private List<Element> extractSegment(List<Element> parentNodes, SegmentContext sc, Optional<XmlNamespaces> xmlNamespaces) throws AtlasException {
        List<Element> answer = new LinkedList<>();
        for (Element parentNode : parentNodes) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Parent element is currently: " + XmlIOHelper.writeDocumentToString(true, parentNode));
            }
            if (XmlPath.isAttributeSegment(sc.getSegment())) {
                answer.add(parentNode);
                continue;
            }

            String childrenElementName = XmlPath.cleanPathSegment(sc.getSegment());
            String namespaceAlias = XmlPath.getNamespace(sc.getSegment());
            Optional<String> namespace = getNamespace(xmlNamespaces, namespaceAlias);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Looking for children elements with name: " + childrenElementName);
            }
            List<Element> children = XmlIOHelper.getChildrenWithNameStripAlias(childrenElementName, namespace, parentNode);
            if (children == null || children.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping source value set, couldn't find children with name '" + childrenElementName
                            + "', for segment: " + sc);
                }
                continue;
            }
            if (XmlPath.isCollectionSegment(sc.getSegment())) {
                Integer index = XmlPath.indexOfSegment(sc.getSegment());
                if (index == null) {
                    // TODO process collection
                    answer.addAll(children);
                    continue;
                }
                if (index >= children.size()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skipping source value set, children list can't fit index " + index
                                + ", children list size: " + children.size());
                    }
                    continue;
                }
                answer.add(children.get(index));
            } else {
                answer.add(children.get(0));
            }
        }
        return answer;
    }

    private void readValue(AtlasInternalSession session, List<Element> parentNodes, SegmentContext sc, XmlField xmlField) {
        if (xmlField.getFieldType() == null) {
            xmlField.setFieldType(FieldType.STRING);
        }

        XmlPath path = new XmlPath(xmlField.getPath());
        FieldGroup fieldGroup = null;
        if (path.hasCollection() && !path.isIndexedCollection()) {
            fieldGroup = AtlasModelFactory.createFieldGroupFrom(xmlField);
            session.head().setSourceField(fieldGroup);
        }

        for (int i=0; i<parentNodes.size(); i++) {
            Element parentNode = parentNodes.get(i);
            XmlField targetField = xmlField;
            if (fieldGroup != null) {
                targetField = new XmlField();
                AtlasModelFactory.copyField(xmlField, targetField, false);
                XmlPath subPath = new XmlPath(targetField.getPath());
                subPath.setVacantCollectionIndex(i);
                targetField.setPath(subPath.toString());
                fieldGroup.getField().add(targetField);
            }

            String value = parentNode.getTextContent();
            if (XmlPath.isAttributeSegment(sc.getSegment())) {
                String attributeName = XmlPath.getAttribute(sc.getSegment());
                value = parentNode.getAttribute(attributeName);
            }

            if (value == null) {
                return;
            }

            if (targetField.getFieldType() == FieldType.STRING) {
                targetField.setValue(value);
            } else {
                Object convertedValue;
                try {
                    convertedValue = conversionService.convertType(value, targetField.getFormat(),
                            targetField.getFieldType(), null);
                    targetField.setValue(convertedValue);
                } catch (AtlasConversionException e) {
                    AtlasUtil.addAudit(session, targetField.getDocId(),
                            String.format("Failed to convert field value '%s' into type '%s'", value,
                                    targetField.getFieldType()),
                            targetField.getPath(), AuditStatus.ERROR, value);
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

    private Optional<XmlNamespaces> getSourceNamespaces(AtlasInternalSession session, XmlField xmlField) {
        DataSource dataSource = null;
        AtlasMapping mapping = session.getMapping();
        // this is to simplify tests which uses mocks
        if (mapping == null || mapping.getDataSource() == null || xmlField.getDocId() == null) {
            return Optional.empty();
        }
        List<DataSource> dataSources = mapping.getDataSource();
        for (DataSource source : dataSources) {
            if (!source.getDataSourceType().equals(DataSourceType.SOURCE)) {
                continue;
            }
            if (xmlField.getDocId().equals(source.getId())) {
                dataSource = source;
                break;
            }
        }
        if (dataSource == null || !XmlDataSource.class.isInstance(dataSource)) {
            return Optional.empty();
        }
        XmlDataSource xmlDataSource = XmlDataSource.class.cast(dataSource);
        return Optional.of(xmlDataSource.getXmlNamespaces());
    }

    private Optional<String> getNamespace(Optional<XmlNamespaces> xmlNamespaces, String namespaceAlias) {
        Optional<String> namespace = Optional.empty();
        if (xmlNamespaces.isPresent()) {
            for (XmlNamespace xmlNamespace : xmlNamespaces.get().getXmlNamespace()) {
                if (xmlNamespace.getAlias().equals(namespaceAlias)) {
                    namespace = Optional.of(xmlNamespace.getUri());
                    break;
                }
            }
        }
        return namespace;
    }

}
