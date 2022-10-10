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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
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
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.core.XmlPath.XmlSegmentContext;
import io.atlasmap.xml.v2.AtlasXmlModelFactory;
import io.atlasmap.xml.v2.XmlDataSource;
import io.atlasmap.xml.v2.XmlEnumField;
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
            AtlasUtil.addAudit(session, field,
                    String.format("Cannot read field '%s' of document '%s', document is null",
                            field.getPath(), field.getDocId()),
                    AuditStatus.ERROR, null);
            return field;
        }
        if (field == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'field' cannot be null"));
        }
        if (!(field instanceof XmlField) && !(field instanceof FieldGroup)
                && !(field instanceof XmlEnumField)) {
            throw new AtlasException(String.format("Unsupported field type '%s'", field.getClass()));
        }

        seedDocumentNamespaces(document);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading source value for field: " + field.getPath());
        }
        Optional<XmlNamespaces> xmlNamespaces = getSourceNamespaces(session, field);
        XmlPath path = new XmlPath(field.getPath());
        List<Field> fields = getFieldsForPath(session, xmlNamespaces, document.getDocumentElement(), field, path, 0);

        if (path.hasCollection() && !path.isIndexedCollection()) {
            FieldGroup fieldGroup = AtlasModelFactory.createFieldGroupFrom(field, true);
            fieldGroup.getField().addAll(fields);
            if (fields.size() == 0) {
                fieldGroup.setStatus(FieldStatus.NOT_FOUND);
            }
            session.head().setSourceField(fieldGroup);
            return fieldGroup;
        } else if (fields.size() == 1) {
            field.setValue(fields.get(0).getValue());
            return field;
        } else {
            if (fields.size() == 0) {
                field.setStatus(FieldStatus.NOT_FOUND);
            }
            return field;
        }
    }

    private List<Field> getFieldsForPath(AtlasInternalSession session, Optional<XmlNamespaces> xmlNamespaces,
     Element node, Field field, XmlPath path, int depth) throws AtlasException {
        List<Field> fields = new ArrayList<>();
        List<XmlSegmentContext> segments = path.getXmlSegments(false);
        if (segments.size() < depth) {
            throw new AtlasException(String.format("depth '%s' exceeds segment size '%s'", depth, segments.size()));
        }

        if (segments.size() == depth) {
            if (!(field instanceof XmlEnumField) && field.getFieldType() == FieldType.COMPLEX) {
                FieldGroup group = (FieldGroup) field;
                populateChildFields(session, xmlNamespaces, node, group, path);
                fields.add(group);
            } else {
                XmlField xmlField = new XmlField();
                AtlasXmlModelFactory.copyField(field, xmlField, true);
                if (field instanceof XmlEnumField && xmlField.getFieldType() == FieldType.COMPLEX) {
                    xmlField.setFieldType(FieldType.STRING); // enum has COMPLEX by default
                }
                copyValue(session, xmlNamespaces, segments.get(depth - 1), node, xmlField);
                xmlField.setIndex(null); //reset index for subfields
                fields.add(xmlField);
            }
            return fields;
        }

        // segments.size() > depth
        XmlSegmentContext segment = segments.get(depth);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Now processing segment: " + segment.getName());
        }
        if (depth == 0) {
            if (segment.getName().startsWith(XmlIOHelper.getNodeNameWithoutNamespaceAlias(node))) {
                Optional<String> rootNamespace = Optional.empty();
                if (segment.getNamespace() != null) {
                    rootNamespace = getNamespace(xmlNamespaces, segment.getNamespace());
                }
                if (!rootNamespace.isPresent() || rootNamespace.get().equals(node.getNamespaceURI())) {
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

        if (segment.isAttribute() && segments.size() == depth + 1) {
            //if last segment is attribute
            List<Field> attrFields = getFieldsForPath(session, xmlNamespaces, node, field, path, depth + 1);
            fields.addAll(attrFields);
            return fields;
        }

        String fieldName = segment.getName();
        String fieldNamespace = segment.getNamespace();
        Optional<String> namespace = getNamespace(xmlNamespaces, fieldNamespace);
        List<Element> children = XmlIOHelper.getChildrenWithNameStripAlias(fieldName, namespace, node);
        if (children == null || children.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping source value set, couldn't find children with name '" + fieldName
                        + "', for segment: " + segment);
            }
            return fields;
        }

        if (segment.getCollectionType() == CollectionType.NONE) {
            List<Field> childFields = getFieldsForPath(session, xmlNamespaces, children.get(0), field, path, depth + 1);
            fields.addAll(childFields);
            return fields;
        }

        // collection
        Integer index = segment.getCollectionIndex();
        if (index != null) {
            if (index < children.size()) {
                List<Field> arrayFields = getFieldsForPath(session, xmlNamespaces, children.get(index), field, path,
                        depth + 1);
                fields.addAll(arrayFields);
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping source value set, children list can't fit index " + index + ", children list size: "
                    + children.size());
            }
        } else {
            //if index not included, iterate over all
            for (int i=0; i<children.size(); i++) {
                Field itemField;
                if (field instanceof FieldGroup) {
                    itemField = AtlasXmlModelFactory.cloneFieldGroup((FieldGroup)field);
                    AtlasPath.setCollectionIndexRecursively((FieldGroup)itemField, depth + 1, i);
                 } else {
                    itemField = AtlasXmlModelFactory.cloneField((XmlField)field, false);
                    AtlasPath itemPath = new AtlasPath(field.getPath());
                    itemPath.setCollectionIndex(depth + 1, i);
                    itemField.setPath(itemPath.toString());
                 }
                List<Field> arrayFields = getFieldsForPath(
                    session, xmlNamespaces, children.get(i), itemField, new XmlPath(itemField.getPath()), depth + 1);
                fields.addAll(arrayFields);
            }
        }
        return fields;
    }

    private void populateChildFields(AtlasInternalSession session, Optional<XmlNamespaces> xmlNamespaces,
     Element node, FieldGroup fieldGroup, AtlasPath path) throws AtlasException {
        List<Field> newChildren = new ArrayList<>();
        for (Field child : fieldGroup.getField()) {
            XmlPath childPath = new XmlPath(child.getPath());
            String fieldNamespace = childPath.getLastSegment().getNamespace();
            Optional<String> namespace = getNamespace(xmlNamespaces, fieldNamespace);
            List<Element> children = XmlIOHelper.getChildrenWithNameStripAlias(childPath.getLastSegment().getName(), namespace, node);
            if (childPath.getLastSegment().getCollectionType() != CollectionType.NONE) {
                FieldGroup childGroup = populateCollectionItems(session, xmlNamespaces, children, child);
                newChildren.add(childGroup);
            } else {
                if (child instanceof FieldGroup) {
                    populateChildFields(session, xmlNamespaces, children.get(0), (FieldGroup)child, childPath);
                } else {
                    copyValue(session, xmlNamespaces, childPath.getLastSegment(), children.get(0), (XmlField)child);
                }
                newChildren.add(child);
            }
        }
        fieldGroup.getField().clear();
        fieldGroup.getField().addAll(newChildren);
    }

    private FieldGroup populateCollectionItems(AtlasInternalSession session, Optional<XmlNamespaces> xmlNamespaces,
     List<Element> elements, Field field) throws AtlasException {
        FieldGroup group = field instanceof FieldGroup ?
         (FieldGroup)field : AtlasModelFactory.createFieldGroupFrom(field, true);
        for (int i=0; i<elements.size(); i++) {
            XmlPath itemPath = new XmlPath(group.getPath());
            List<SegmentContext> segments = itemPath.getSegments(true);
            itemPath.setCollectionIndex(segments.size() - 1, i);
            if (field instanceof FieldGroup) {
                FieldGroup itemGroup = AtlasXmlModelFactory.cloneFieldGroup((FieldGroup)field);
                AtlasPath.setCollectionIndexRecursively(itemGroup, segments.size(), i);
                populateChildFields(session, xmlNamespaces, elements.get(i), itemGroup, itemPath);
                group.getField().add(itemGroup);
            } else {
                XmlField itemField = (XmlField) AtlasXmlModelFactory.cloneField((XmlField)field, false);
                itemField.setPath(itemPath.toString());
                copyValue(session, xmlNamespaces, itemPath.getLastSegment(), elements.get(i), itemField);
                group.getField().add(itemField);
            }
        }
        return group;
    }

    private void copyValue(AtlasInternalSession session, Optional<XmlNamespaces> xmlNamespaces,
            XmlSegmentContext sc, Element node, XmlField xmlField) {
        if (xmlField.getFieldType() == null) {
            xmlField.setFieldType(FieldType.STRING);
        }

        String value;
        if (sc.isAttribute()) {
            if (sc.getNamespace() != null && !sc.getNamespace().isEmpty()) {
                if (getNamespace(xmlNamespaces, sc.getNamespace()).isPresent()) {
                    value = node.getAttributeNS(
                        getNamespace(xmlNamespaces, sc.getNamespace()).get(), sc.getName());
                } else {
                    String attributeName = sc.getQName();
                    value = node.getAttribute(attributeName);
                }
            } else {
                value = node.getAttribute(sc.getName());
            }
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
                AtlasUtil.addAudit(session, xmlField,
                        String.format("Failed to convert field value '%s' into type '%s'", value,
                            xmlField.getFieldType()),
                    AuditStatus.ERROR, value);
            }
        }
    }

    public void setDocument(Document document) throws AtlasException {
        this.document = document;
    }

    private Optional<XmlNamespaces> getSourceNamespaces(AtlasInternalSession session, Field field) {
        DataSource dataSource = null;
        AtlasMapping mapping = session.getMapping();
        // this is to simplify tests which uses mocks
        if (mapping == null || mapping.getDataSource() == null || field.getDocId() == null) {
            return Optional.empty();
        }
        List<DataSource> dataSources = mapping.getDataSource();
        for (DataSource source : dataSources) {
            if (!source.getDataSourceType().equals(DataSourceType.SOURCE)) {
                continue;
            }
            if (field.getDocId().equals(source.getId())) {
                dataSource = source;
                break;
            }
        }
        if (dataSource == null || !XmlDataSource.class.isInstance(dataSource)) {
            return Optional.empty();
        }
        XmlDataSource xmlDataSource = XmlDataSource.class.cast(dataSource);
        return xmlDataSource.getXmlNamespaces() != null ? Optional.of(xmlDataSource.getXmlNamespaces()) : Optional.empty();
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
