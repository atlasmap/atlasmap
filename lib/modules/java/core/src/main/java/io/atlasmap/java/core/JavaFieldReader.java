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
package io.atlasmap.java.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.java.core.accessor.JavaChildAccessor;
import io.atlasmap.java.core.accessor.RootAccessor;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;

public class JavaFieldReader implements AtlasFieldReader {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaFieldReader.class);

    private AtlasConversionService conversionService;
    private Object sourceDocument;

    @Override
    public Field read(AtlasInternalSession session) throws AtlasException {
        try {
            Field field = session.head().getSourceField();
            if (sourceDocument == null) {
                AtlasUtil.addAudit(session, field, String.format(
                    "Unable to read sourceField (path=%s),  document (docId=%s) is null",
                    field.getPath(), field.getDocId()),
                    AuditStatus.ERROR, null);
            }

            AtlasPath path = new AtlasPath(field.getPath());

            List<Field> fields = getFieldsForPath(session, sourceDocument, field, path, 0);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processed input field sPath=" + field.getPath() + " sV=" + field.getValue()
                    + " sT=" + field.getFieldType() + " docId: " + field.getDocId());
            }

            if (path.hasCollection() && !path.isIndexedCollection()) {
                FieldGroup fieldGroup = AtlasModelFactory.createFieldGroupFrom(field, true);
                fieldGroup.getField().addAll(fields);
                session.head().setSourceField(fieldGroup);
                return fieldGroup;
            } else if (fields.size() == 1) {
                field.setValue(fields.get(0).getValue());
                return field;
            } else {
                return field;
            }
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    private List<Field> getFieldsForPath(AtlasInternalSession session, Object source, Field field, AtlasPath path, int depth) throws AtlasException {
        List<Field> fields = new ArrayList<>();
        List<SegmentContext> segments = path.getSegments(true);
        if (source == null) {
            return fields;
        }

        if (segments.size() < depth) {
            throw new AtlasException(String.format("depth '%s' exceeds segment size '%s'", depth, segments.size()));
        }
        if (segments.size() == depth) {
            Field newField;
            if (field instanceof FieldGroup && field.getFieldType() == FieldType.COMPLEX) {
                FieldGroup group = (FieldGroup) field;
                populateChildFields(source, group, path);
                newField = group;
            } else {
                newField = AtlasJavaModelFactory.cloneJavaField(field, true);
                if (source != null && (conversionService.isPrimitive(source.getClass())
                    || conversionService.isBoxedPrimitive(source.getClass()))) {
                    source = conversionService.copyPrimitive(source);
                }
                newField.setValue(source);
                newField.setIndex(null); //reset index for subfields
            }
            fields.add(newField);
            return fields;
        }

        // segments.size() > depth) {
        SegmentContext segmentContext = segments.get(depth);
        if (depth == 0 && segments.size() > 1 && segmentContext.getCollectionType() == CollectionType.NONE) {
            depth++;
            segmentContext = segments.get(depth);
        }

        JavaChildAccessor childAccessor = getAccessorForSegment(session, source, field, path, segmentContext);
        if (childAccessor == null || childAccessor.getRawValue() == null) {
            return fields;
        }

        if (segmentContext.getCollectionType() == CollectionType.NONE) {
            List<Field> childFields = getFieldsForPath(session, childAccessor.getValue(), field, path, depth + 1);
            fields.addAll(childFields);
            return fields;
        }

        // collection
        if (segmentContext.getCollectionIndex() != null) {
            Object indexItem = childAccessor.getValueAt(segmentContext.getCollectionIndex());
            List<Field> childFields = getFieldsForPath(session, indexItem, field, path, depth + 1);
            fields.addAll(childFields);
        } else {
            List<?> items = childAccessor.getCollectionValues();
            for (int i = 0; i < items.size(); i++) {
                //include the array index within the path
                Field itemField;
                if (field instanceof FieldGroup) {
                    itemField = AtlasJavaModelFactory.cloneFieldGroup((FieldGroup)field);
                    AtlasPath.setCollectionIndexRecursively((FieldGroup)itemField, depth, i);
                } else {
                    itemField = AtlasJavaModelFactory.cloneJavaField(field, false);
                    AtlasPath itemPath = new AtlasPath(field.getPath());
                    itemPath.setCollectionIndex(depth, i);
                    itemField.setPath(itemPath.toString());
                }
                List<Field> arrayFields = getFieldsForPath(
                    session, items.get(i), itemField, new AtlasPath(itemField.getPath()), depth + 1);
                fields.addAll(arrayFields);
            }
        }
        return fields;
    }

    private JavaChildAccessor getAccessorForSegment(AtlasInternalSession session, Object source, Field field, AtlasPath path, SegmentContext segmentContext) throws AtlasException {
        JavaChildAccessor accessor = null;
        if (segmentContext.isRoot()) {
            accessor = new RootAccessor(source);
        } else {
            accessor = ClassHelper.lookupAccessor(source, segmentContext.getName());
        }
        if (accessor == null) {
            AtlasUtil.addAudit(session, field, String.format(
                "Field '%s' not found on object '%s'", segmentContext.getName(), source),
                AuditStatus.ERROR, null);
            return null;
        }

        try {
            accessor.getValue();
        } catch (Exception e) {
            AtlasUtil.addAudit(session, field, String.format(
                "Cannot access field '%s' on object '%s': %s", segmentContext.getName(), source, e.getMessage()),
                AuditStatus.ERROR, null);
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
            return null;
        }

        if (path.getLastSegment() == segmentContext && field.getFieldType() == null
            && (field instanceof JavaField || field instanceof JavaEnumField)) {
            detectFieldType(session, accessor, field);
        }
        return accessor;
    }

    private void detectFieldType(AtlasInternalSession session, JavaChildAccessor accessor, Field field) throws AtlasException {
        Class<?> returnType = null;
        try {
            returnType = accessor.getRawClass();
        } catch (Exception e) {
            AtlasUtil.addAudit(session, field, String.format(
                "Cannot access the type of field '%s' on object '%s': %s",
                    accessor.getName(), accessor.getRawValue(), e.getMessage()),
                AuditStatus.ERROR, null);
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
        }

        if (returnType != null) {
            field.setFieldType(conversionService.fieldTypeFromClass(returnType));
            if (LOG.isTraceEnabled()) {
                LOG.trace("Auto-detected sourceField type p=" + field.getPath() + " t="
                    + field.getFieldType());
            }
        } else {
            AtlasUtil.addAudit(session, field, String.format(
                "Unable to auto-detect sourceField type path=%s docId=%s",
                field.getPath(), field.getDocId()),
                AuditStatus.WARN, null);
        }
    }

    private void populateChildFields(Object source, FieldGroup fieldGroup, AtlasPath path) throws AtlasException {
        List<Field> newChildren = new ArrayList<>();
        for (Field child : fieldGroup.getField()) {
            AtlasPath childPath = new AtlasPath(child.getPath());
            JavaChildAccessor accessor = ClassHelper.lookupAccessor(source, childPath.getLastSegment().getName());
            if (childPath.getLastSegment().getCollectionType() != CollectionType.NONE) {
                FieldGroup childGroup = populateCollectionItems(accessor, child);
                newChildren.add(childGroup);
            } else {
                if (child instanceof FieldGroup) {
                    populateChildFields(accessor.getValue(), (FieldGroup)child, childPath);
                } else {
                    child.setValue(accessor.getValue());
                }
                newChildren.add(child);
            }
        }
        fieldGroup.getField().clear();
        fieldGroup.getField().addAll(newChildren);
    }

    private FieldGroup populateCollectionItems(JavaChildAccessor accessor, Field field) throws AtlasException {
        if (accessor == null || accessor.getCollectionType() == CollectionType.NONE) {
            throw new AtlasException(String.format("Couldn't find a collection object for field %s:%s",
                field.getDocId(), field.getPath()));
        }
        FieldGroup group = field instanceof FieldGroup ?
         (FieldGroup)field : AtlasModelFactory.createFieldGroupFrom(field, true);
        for (int i=0; i<accessor.getCollectionValues().size(); i++) {
            AtlasPath itemPath = new AtlasPath(group.getPath());
            List<SegmentContext> segments = itemPath.getSegments(true);
            itemPath.setCollectionIndex(segments.size() - 1, i);
            if (field instanceof FieldGroup) {
                FieldGroup itemGroup = AtlasJavaModelFactory.cloneFieldGroup((FieldGroup)field);
                AtlasPath.setCollectionIndexRecursively(itemGroup, segments.size(), i);
                populateChildFields(accessor.getValueAt(i), itemGroup, itemPath);
                group.getField().add(itemGroup);
            } else {
                Field itemField = AtlasJavaModelFactory.cloneJavaField(field, false);
                itemField.setPath(itemPath.toString());
                itemField.setValue(accessor.getValueAt(i));
                group.getField().add(itemField);
            }
        }
        return group;
    }

    public void setDocument(Object sourceDocument) {
        this.sourceDocument = sourceDocument;
    }

    public void setConversionService(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
