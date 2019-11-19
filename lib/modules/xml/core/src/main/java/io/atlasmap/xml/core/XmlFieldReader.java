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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.atlasmap.core.AtlasPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.core.XmlPath.XmlSegmentContext;
import io.atlasmap.xml.v2.XmlDataSource;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlNamespace;
import io.atlasmap.xml.v2.XmlNamespaces;

public class XmlFieldReader extends XmlFieldTransformer implements AtlasFieldReader {

    private static final Logger LOG = LoggerFactory.getLogger(XmlFieldReader.class);

    private AtlasConversionService conversionService;
    private Document document;

    public XmlFieldReader(ClassLoader cl, AtlasConversionService conversionService) {
        super(cl);
        this.conversionService = conversionService;
    }

    public XmlFieldReader(ClassLoader cl, AtlasConversionService conversionService, Map<String, String> namespaces) {
        super(cl, namespaces);
        this.conversionService = conversionService;
    }

    public Field read(AtlasInternalSession session) throws AtlasException {
        Field field = session.head().getSourceField();
        if (document == null) {
            AtlasUtil.addAudit(session, field.getDocId(),
                    String.format("Cannot read field '%s' of document '%s', document is null",
                            field.getPath(), field.getDocId()),
                    field.getPath(), AuditStatus.ERROR, null);
            return field;
        }
        if (field == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'field' cannot be null"));
        }

        seedDocumentNamespaces(document);
        XmlField xmlField = XmlField.class.cast(field);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading source value for field: " + xmlField.getPath());
        }
        Optional<XmlNamespaces> xmlNamespaces = getSourceNamespaces(session, xmlField);
        XmlPath path = new XmlPath(xmlField.getPath());
        List<XmlField> fields = getFieldsForPath(session, xmlNamespaces, document.getDocumentElement(), field, path, 0);

        if (path.hasCollection() && !path.isIndexedCollection()) {
            FieldGroup fieldGroup = AtlasModelFactory.createFieldGroupFrom(field);
            fieldGroup.getField().addAll(fields);
            session.head().setSourceField(fieldGroup);
            return fieldGroup;
        } else if (fields.size() == 1) {
            field.setValue(fields.get(0).getValue());
            return field;
        } else {
            return field;
        }
    }

    private List<XmlField> getFieldsForPath(AtlasInternalSession session, Optional<XmlNamespaces> xmlNamespaces, Element node, Field field, XmlPath path, int depth) {
        List<XmlField> xmlFields = new ArrayList<>();
        List<XmlSegmentContext> segments = path.getXmlSegments(false);

        if (segments.size() == depth) {
            XmlField xmlField = new XmlField();
            AtlasModelFactory.copyField(field, xmlField, false);
            XmlSegmentContext lastSegment = segments.get(depth - 1);
            copyValue(session, lastSegment, node, xmlField);
            xmlFields.add(xmlField);
        } else if (segments.size() > depth) {
            XmlSegmentContext segment = segments.get(depth);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Now processing segment: " + segment.getName());
            }
            if (depth == 0) {
                if (segment.getName().startsWith(XmlIOHelper.getNodeNameWithoutNamespaceAlias(node))) {
                    Optional<String> namespace = Optional.empty();
                    if (segment.getNamespace() != null) {
                        namespace = getNamespace(xmlNamespaces, segment.getNamespace());
                    }
                    if (!namespace.isPresent() || namespace.get().equals(node.getNamespaceURI())) {
                        // processing root node part of path such as the "XOA" part of
                        // "/XOA/contact<>/firstName", skip.
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Skipping root segment: " + segment);
                        }
                        if (segments.size() > 1) {
                            depth = 1;
                            segment = segments.get(depth);
                        }
                    }
                }
            }
            String fieldName = segment.getName();
            String fieldNamespace = segment.getNamespace();
            Optional<String> namespace = getNamespace(xmlNamespaces, fieldNamespace);

            if (segment.isAttribute() && segments.size() == depth + 1) {
                //if last segment is attribute
                List<XmlField> fields = getFieldsForPath(session, xmlNamespaces, node, field, path, depth + 1);
                xmlFields.addAll(fields);
            } else {
                List<Element> children = XmlIOHelper.getChildrenWithNameStripAlias(fieldName, namespace, node);
                if (children == null || children.isEmpty()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skipping source value set, couldn't find children with name '" + fieldName
                            + "', for segment: " + segment);
                    }
                    return xmlFields;
                }

                if (segment.getCollectionType() != CollectionType.NONE) {
                    Integer index = segment.getCollectionIndex();
                    if (index == null) {
                        //if index not included, iterate over all
                        int arrayIndex = 0;
                        for (Element arrayItem : children) {
                            List<XmlField> arrayFields = getFieldsForPath(session, xmlNamespaces, arrayItem, field, path, depth + 1);
                            for (XmlField arrayField : arrayFields) {
                                AtlasPath subPath = new AtlasPath(arrayField.getPath());
                                //include the array index within the path
                                subPath.setCollectionIndex(depth + 1, arrayIndex);
                                arrayField.setPath(subPath.toString());
                            }
                            xmlFields.addAll(arrayFields);
                            arrayIndex++;
                        }
                    } else if (index < children.size()) {
                        List<XmlField> fields = getFieldsForPath(session, xmlNamespaces, children.get(index), field, path, depth + 1);
                        xmlFields.addAll(fields);
                    } else if (LOG.isDebugEnabled()) {
                        LOG.debug("Skipping source value set, children list can't fit index " + index
                            + ", children list size: " + children.size());
                    }
                } else {
                    List<XmlField> fields = getFieldsForPath(session, xmlNamespaces, children.get(0), field, path, depth + 1);
                    xmlFields.addAll(fields);
                }
            }
        }

        return xmlFields;
    }

    private void copyValue(AtlasInternalSession session, XmlSegmentContext sc, Element node, XmlField xmlField) {
        if (xmlField.getFieldType() == null) {
            xmlField.setFieldType(FieldType.STRING);
        }

        String value;
        if (sc.isAttribute()) {
            String attributeName = sc.getQName();
            value = node.getAttribute(attributeName);
        } else {
            value = node.getTextContent();
        }

        if (value == null) {
            return;
        }

        if (xmlField.getFieldType() == FieldType.STRING) {
            xmlField.setValue(value);
        } else {
            Object convertedValue;
            try {
                convertedValue = conversionService.convertType(value, xmlField.getFormat(),
                    xmlField.getFieldType(), null);
                xmlField.setValue(convertedValue);
            } catch (AtlasConversionException e) {
                AtlasUtil.addAudit(session, xmlField.getDocId(),
                        String.format("Failed to convert field value '%s' into type '%s'", value,
                            xmlField.getFieldType()),
                    xmlField.getPath(), AuditStatus.ERROR, value);
            }
        }
    }

    public void setDocument(Document document) throws AtlasException {
        this.document = document;
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
                if ((xmlNamespace.getAlias() == null && namespaceAlias == null)
                    || xmlNamespace.getAlias().equals(namespaceAlias)) {
                    namespace = Optional.of(xmlNamespace.getUri());
                    break;
                }
            }
        }
        return namespace;
    }

}
