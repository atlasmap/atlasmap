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
package io.atlasmap.java.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasFieldWriter;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupTable;

public class JavaFieldWriter implements AtlasFieldWriter {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaFieldWriter.class);

    private Object rootObject = null;
    private JavaFieldWriterUtil writerUtil;
    private TargetValueConverter converter;
    private Map<Field, Object> fieldParentQueue = new LinkedHashMap<>();
    private CollectionType collectionType = CollectionType.NONE;
    private Class<?> collectionItemClass = null;

    public JavaFieldWriter(JavaFieldWriterUtil util) {
        this.writerUtil = util;
    }

    public Object prepareParentObject(AtlasInternalSession session) throws AtlasException {
        Field targetField = session.head().getTargetField();
        if (targetField == null) {
            throw new AtlasException("Target field cannot be null");
        }

        try {
            AtlasPath path = new AtlasPath(targetField.getPath());
            if (path.isRoot()) {
                return null;
            }
            if (rootObject == null) {
                throw new IllegalArgumentException("A root object must be set before process");
            }

            SegmentContext rootSegment = path.getRootSegment();
            Object parentObject = this.rootObject;
            if (rootSegment.getCollectionType() != CollectionType.NONE) {
                if (collectionItemClass == null) {
                    throw new AtlasException(String.format(
                        "Collection item class must be specified to handle topmost collection, path=",
                         path.toString()));
                }
                parentObject = writerUtil.getCollectionItem(rootObject, rootSegment);
                if (parentObject == null) {
                    this.rootObject = writerUtil.adjustCollectionSize(this.rootObject, rootSegment);
                    parentObject = writerUtil.createComplexCollectionItem(this.rootObject, collectionItemClass, rootSegment);
                }
            }

            for (SegmentContext segmentContext : path.getSegments(false)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Now processing segment: " + segmentContext);
                    LOG.debug("Parent object is currently: " + writeDocumentToString(false, parentObject));
                }

                if (segmentContext == path.getLastSegment()) {
                    return parentObject;
                }

                Object childObject = writerUtil.getChildObject(parentObject, segmentContext);
                if (childObject == null) {
                    childObject = writerUtil.createComplexChildObject(parentObject, segmentContext);
                }
                if (segmentContext.getCollectionType() != CollectionType.NONE) {
                    Object item = writerUtil.getCollectionItem(childObject, segmentContext);
                    if (item == null) {
                        Object adjusted = writerUtil.adjustCollectionSize(childObject, segmentContext);
                        if (adjusted != childObject) {
                            writerUtil.setChildObject(parentObject, adjusted, segmentContext);
                        }
                        item = writerUtil.createComplexCollectionItem(parentObject, adjusted, segmentContext);
                    }
                    childObject = item;
                }
                parentObject = childObject;
            }
            return null;
        } catch (Throwable t) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error occured while preparing parent object for: " + targetField.getPath(), t);
            }
            if (t instanceof AtlasException) {
                throw (AtlasException) t;
            }
            throw new AtlasException(t);
        }
    }

    public void populateTargetFieldValue(AtlasInternalSession session, Object parentObject) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();
        LookupTable lookupTable = session.head().getLookupTable();
        converter.populateTargetField(session, lookupTable, sourceField, parentObject, targetField);
    }

    public void enqueueFieldAndParent(Field field, Object parentObject) {
        this.fieldParentQueue.put(field, parentObject);
    }

    public void commitWriting(AtlasInternalSession session) throws AtlasException {
        try {
            for (Entry<Field, Object> e : this.fieldParentQueue.entrySet()) {
                Object parentObject = e.getValue();
                Field targetField = e.getKey();
                AtlasPath path = new AtlasPath(targetField.getPath());
                converter.convertTargetValue(session, parentObject, targetField);
                SegmentContext lastSegment = path.getLastSegment();
                if (path.isRoot()) {
                    if (lastSegment.getCollectionType() == CollectionType.NONE) {
                        this.rootObject = targetField.getValue();
                    } else {
                        Object adjusted = writerUtil.adjustCollectionSize(this.rootObject, lastSegment);
                        if (adjusted != this.rootObject) {
                            this.rootObject = adjusted;
                        }
                        writerUtil.setCollectionItem(adjusted, targetField.getValue(), lastSegment);
                    }
                    continue;
                }

                String targetClassName = targetField instanceof JavaField
                        ? ((JavaField)targetField).getClassName()
                        : ((JavaEnumField)targetField).getClassName();

                if (lastSegment.getCollectionType() == CollectionType.NONE) {
                    if (targetField.getFieldType() == FieldType.COMPLEX && targetField.getValue() == null) {
                        if (targetClassName != null && !targetClassName.isEmpty()) {
                            writerUtil.createComplexChildObject(parentObject, lastSegment, writerUtil.loadClass(targetClassName));
                        } else {
                            writerUtil.createComplexChildObject(parentObject, lastSegment);
                        }
                    } else {
                        writerUtil.setChildObject(parentObject, targetField.getValue(), lastSegment);
                    }
                } else {
                    Object collection = writerUtil.getChildObject(parentObject, lastSegment);
                    if (collection == null) {
                        collection = writerUtil.createComplexChildObject(parentObject, lastSegment);
                    }
                    Object adjusted = writerUtil.adjustCollectionSize(collection, lastSegment);
                    if (adjusted != collection) {
                        writerUtil.setChildObject(parentObject, adjusted, lastSegment);
                    }
                    if (targetField.getFieldType() == FieldType.COMPLEX && targetField.getValue() == null) {
                        if (targetClassName != null && !targetClassName.isEmpty()) {
                            writerUtil.createComplexChildObject(parentObject, lastSegment, writerUtil.loadClass(targetClassName));
                        } else {
                            writerUtil.createComplexChildObject(parentObject, lastSegment);
                        }
                    } else {
                        writerUtil.setCollectionItem(adjusted, targetField.getValue(), lastSegment);
                    }
                }
            }
        } finally {
            this.fieldParentQueue.clear();
        }
    }

    public void write(AtlasInternalSession session) throws AtlasException {
        Object parentObject = prepareParentObject(session);
        populateTargetFieldValue(session, parentObject);
        Field targetField = session.head().getTargetField();
        enqueueFieldAndParent(targetField, parentObject);
        commitWriting(session);
    }

    public Object getRootObject() {
        return rootObject;
    }

    public void setRootObject(Object rootObject) {
        this.rootObject = rootObject;
    }

    public void setTargetValueConverter(TargetValueConverter converter) {
        this.converter = converter;
    }

    public void setCollectionType(CollectionType type) {
        this.collectionType = type;
    }

    public void setCollectionItemClass(Class<?> clazz) {
        this.collectionItemClass = clazz;
    }

    private String writeDocumentToString(boolean stripSpaces, Object object) throws AtlasException {
        try {
            if (object == null) {
                return "";
            }

            String result = object.toString();

            if (stripSpaces) {
                result = result.replaceAll("\n|\r", "");
                result = result.replaceAll("> *?<", "><");
            }
            return result;
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

}
