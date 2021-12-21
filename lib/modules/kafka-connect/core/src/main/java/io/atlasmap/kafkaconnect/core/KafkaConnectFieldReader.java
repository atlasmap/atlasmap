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
package io.atlasmap.kafkaconnect.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.kafkaconnect.v2.AtlasKafkaConnectModelFactory;
import io.atlasmap.kafkaconnect.v2.KafkaConnectField;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;

/**
 * The {@link AtlasFieldReader} implementation for Kafka Connect.
 */
public class KafkaConnectFieldReader implements AtlasFieldReader {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectFieldReader.class);

    private AtlasConversionService conversionService;
    private Object root;
    private org.apache.kafka.connect.data.Schema schema;

    @SuppressWarnings("unused")
    private KafkaConnectFieldReader() {
    }

    /**
     * A constructor.
     * @param conversionService conversion service
     */
    public KafkaConnectFieldReader(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Field read(AtlasInternalSession session) throws AtlasException {
        Field field = session.head().getSourceField();
        if (root == null) {
            AtlasUtil.addAudit(session, field,
                String.format("Cannot read a field '%s' of KafkaConnect document '%s', document is null",
                    field.getPath(), field.getDocId()),
                AuditStatus.ERROR, null);
            return field;
        }

        AtlasPath path = new AtlasPath(field.getPath());
         List<Field> fields;
        if (path.getSegments(true).size() == 1) {
            if (field.getFieldType() == FieldType.COMPLEX) {
                FieldGroup group = (FieldGroup) field;
                if (path.isCollectionRoot()) {
                    nestComplexCollection(session, (List<Object>) root, group, 0);
                } else {
                    populateChildFields(session, (Struct)root, group);
                }
                return group;
            } else {
                fields = createValueFields(root, path.getRootSegment(), 0, (KafkaConnectField)field);
            }
        } else {
            fields = getFieldsForPath(session, root, field, path, 0);
        }
        
        if (path.hasCollection() && !path.isIndexedCollection()) {
            FieldGroup fieldGroup = AtlasModelFactory.createFieldGroupFrom(field, true);
            fieldGroup.getField().addAll(fields);
            session.head().setSourceField(fieldGroup);
            return fieldGroup;
        } else if (fields.size() == 1) {
            Field f = fields.get(0);
            session.head().setSourceField(f);
            return f;
        } else {
            return field;
        }
    }

    private void nestComplexCollection(AtlasInternalSession session, List<Object> collection, FieldGroup parent, int depth) throws AtlasException {
        AtlasPath path = new AtlasPath(parent.getPath());
        SegmentContext segment = path.getSegments(true).get(depth);
        if (segment.getCollectionIndex() != null) {
            int index = segment.getCollectionIndex();
            Struct struct = (Struct) collection.get(index);
            populateChildFields(session, struct, parent);
            return;
        }

        List<Field> processed = new LinkedList<>();
        for (int index=0; index<collection.size(); index++) {
            FieldGroup itemGroup = AtlasKafkaConnectModelFactory.cloneFieldGroup(parent);
            AtlasPath.setCollectionIndexRecursively(itemGroup, depth, index);
            processed.add(itemGroup);
            Struct struct = (Struct) collection.get(index);
            populateChildFields(session, struct, itemGroup);
        }
        parent.getField().clear();
        parent.getField().addAll(processed);
    }

    private List<Field> createValueFields(Object parent, SegmentContext segment, int segmentIndex, KafkaConnectField parentField) throws AtlasException {
        List<Field> fields = new LinkedList<>();
        if (segment.getCollectionType() == CollectionType.NONE) {
            KafkaConnectField kcField = AtlasKafkaConnectModelFactory.cloneField(parentField, true);
            Object converted = conversionService.convertType(parent, parentField.getFormat(),
                parentField.getFieldType(), null);
            kcField.setValue(converted);
            kcField.setIndex(null); //reset index for subfields
            fields.add(kcField);
        } else if (segment.getCollectionIndex() != null) {
            List<Object> collection = (List<Object>) parent;
            int i = segment.getCollectionIndex();
            KafkaConnectField kcField = AtlasKafkaConnectModelFactory.cloneField(parentField, true);
            Object converted = conversionService.convertType(collection.get(i), parentField.getFormat(),
                parentField.getFieldType(), null);
            kcField.setValue(converted);
            kcField.setIndex(null); //reset index for subfields
            fields.add(kcField);
        } else {
            List<Object> collection = (List<Object>) parent;
            for (int i=0; i<collection.size(); i++) {
                KafkaConnectField kcField = AtlasKafkaConnectModelFactory.cloneField(parentField, true);
                Object converted = conversionService.convertType(collection.get(i), parentField.getFormat(),
                    parentField.getFieldType(), null);
                kcField.setValue(converted);
                kcField.setIndex(null); //reset index for subfields
                fields.add(kcField);
                AtlasPath path = new AtlasPath(parentField.getPath());
                path.setCollectionIndex(segmentIndex, i);
                kcField.setPath(path.toString());
            }
        }
        return fields;
    }

    private List<Field> getFieldsForPath(AtlasInternalSession session, Object parent, Field field, AtlasPath path, int depth) throws AtlasException {
        List<Field> fields = new ArrayList<>();
        List<SegmentContext> segments = path.getSegments(true);
        if (parent == null) {
            return fields;
        }

        if (segments.size() < depth) {
            throw new AtlasException(String.format("depth '%s' exceeds segment size '%s'", depth, segments.size()));
        }
        if (segments.size() == depth) {
            //if traversed the entire path and found value
            if (field instanceof FieldGroup && field.getFieldType() == FieldType.COMPLEX && (parent instanceof Struct)) {
                FieldGroup group = (FieldGroup) field;
                populateChildFields(session, (Struct)parent, group);
                fields.add(group);
            } else {
                field.setValue(parent);
                fields.add(field);
            }
            return fields;
        }

        // segments.size() > depth
        SegmentContext segmentContext = null;
        Object child = null;
        List<Object> collectionChild = null;
        if (depth == 0 && path.hasCollectionRoot()) {
            collectionChild = (List<Object>) parent; //if root is a collection
            segmentContext = segments.get(depth);
        } else {
            if (depth == 0) {
                depth = 1; //skip the root, if not a collection
            }
            segmentContext = segments.get(depth);
            String fieldName = segmentContext.getName();
            child = ((Struct)parent).get(fieldName);
            if (segmentContext.getCollectionType() != CollectionType.NONE) {
                collectionChild = (List<Object>) child;
            }
        }

        if (segmentContext.getCollectionType() == CollectionType.NONE) {
            List<Field> childFields = getFieldsForPath(session, child, field, path, depth + 1);
            fields.addAll(childFields);
            return fields;
        }

        // collection
        if (segmentContext.getCollectionIndex() != null) {
            if (collectionChild.size() <= segmentContext.getCollectionIndex()) {
                //index out of range
                return fields;
            }
            List<Field> arrayFields = getFieldsForPath(session, collectionChild.get(segmentContext.getCollectionIndex()), field, path, depth + 1);
            fields.addAll(arrayFields);
        } else {
            //if index not included, iterate over all
            for (int i=0; i<collectionChild.size(); i++) {
                Field itemField;
                if (field instanceof FieldGroup) {
                    itemField = AtlasKafkaConnectModelFactory.cloneFieldGroup((FieldGroup)field);
                    AtlasPath.setCollectionIndexRecursively((FieldGroup)itemField, depth, i);
                } else {
                    itemField = AtlasKafkaConnectModelFactory.cloneField((KafkaConnectField)field, false);
                    AtlasPath itemPath = new AtlasPath(field.getPath());
                    itemPath.setCollectionIndex(depth, i);
                    itemField.setPath(itemPath.toString());
                 }
                 List<Field> arrayFields = getFieldsForPath(
                    session, collectionChild.get(i), itemField, new AtlasPath(itemField.getPath()), depth + 1);
                fields.addAll(arrayFields);
        }
        }
        return fields;

    }

    private void populateChildFields(AtlasInternalSession session, Struct parent, FieldGroup fieldGroup)
     throws AtlasException {
        List<Field> newChildren = new ArrayList<>();
        for (Field child : fieldGroup.getField()) {
            AtlasPath childPath = new AtlasPath(child.getPath());
            Object childValue = parent.get(childPath.getLastSegment().getName());
            if (childPath.getLastSegment().getCollectionType() != CollectionType.NONE) {
                FieldGroup childGroup = populateCollectionItems(session, (List<Object>)childValue, child);
                newChildren.add(childGroup);
            } else {
                if (child instanceof FieldGroup) {
                    populateChildFields(session, (Struct)childValue, (FieldGroup)child);
                } else {
                    Object converted = conversionService.convertType(childValue, child.getFormat(),
                        child.getFieldType(), null);
                    child.setValue(converted);
                }
                newChildren.add(child);
            }
        }
        fieldGroup.getField().clear();
        fieldGroup.getField().addAll(newChildren);
    }

    private FieldGroup populateCollectionItems(AtlasInternalSession session, List<Object> values, Field field)
     throws AtlasException {
        FieldGroup group = AtlasModelFactory.createFieldGroupFrom(field, true);
        for (int i=0; i<values.size(); i++) {
            AtlasPath itemPath = new AtlasPath(group.getPath());
            List<SegmentContext> segments = itemPath.getSegments(true);
            itemPath.setCollectionIndex(segments.size() - 1, i);
            if (field instanceof FieldGroup) {
                FieldGroup itemGroup = AtlasKafkaConnectModelFactory.cloneFieldGroup((FieldGroup)field);
                AtlasPath.setCollectionIndexRecursively(itemGroup, segments.size() - 1, i);
                populateChildFields(session, (Struct)values.get(i), itemGroup);
                group.getField().add(itemGroup);
            } else {
                KafkaConnectField itemField = AtlasKafkaConnectModelFactory.cloneField((KafkaConnectField)field, false);
                itemField.setPath(itemPath.toString());
                Object converted = conversionService.convertType(values.get(i), itemField.getFormat(),
                    itemField.getFieldType(), null);
                itemField.setValue(converted);
                group.getField().add(itemField);
            }
        }
        return group;
    }

    /**
     * Sets the Document.
     * @param document Document
     * @throws AtlasException none
     */
    public void setDocument(Object document) throws AtlasException {
        this.root = document;
    }

    /**
     * Sets the schema.
     * @param schema schema
     */
    public void setSchema(org.apache.kafka.connect.data.Schema schema) {
        this.schema = schema;
    }

}
