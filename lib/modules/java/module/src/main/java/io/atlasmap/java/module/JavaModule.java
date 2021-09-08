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
package io.atlasmap.java.module;

import java.lang.reflect.Array;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.java.core.JavaFieldReader;
import io.atlasmap.java.core.JavaFieldWriter;
import io.atlasmap.java.core.JavaFieldWriterUtil;
import io.atlasmap.java.core.TargetValueConverter;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Validation;

@AtlasModuleDetail(name = "JavaModule", uri = "atlas:java", modes = { "SOURCE", "TARGET" }, dataFormats = {
        "java" }, configPackages = { "io.atlasmap.java.v2" })
public class JavaModule extends BaseAtlasModule {
    public static final String DEFAULT_LIST_CLASS = "java.util.ArrayList";
    private static final Logger LOG = LoggerFactory.getLogger(JavaModule.class);

    private TargetValueConverter targetValueConverter = null;
    private JavaFieldWriterUtil writerUtil = null;

    public JavaModule() {
        this.setAutomaticallyProcessOutputFieldActions(false);
    }

    @Override
    public void init() {
        writerUtil = new JavaFieldWriterUtil(getClassLoader(), getConversionService());
        targetValueConverter = new TargetValueConverter(getClassLoader(), getConversionService(), writerUtil);
    }

    @Override
    public void destroy() {
        targetValueConverter = null;
        writerUtil = null;
    }

    @Override
    public void processPreValidation(AtlasInternalSession atlasSession) throws AtlasException {
        if (atlasSession == null || atlasSession.getMapping() == null) {
            LOG.error("Invalid session: Session and AtlasMapping must be specified");
            throw new AtlasValidationException("Invalid session");
        }

        JavaValidationService javaValidator = new JavaValidationService(getConversionService(), getFieldActionService());
        javaValidator.setMode(getMode());
        javaValidator.setDocId(getDocId());
        List<Validation> javaValidations = javaValidator.validateMapping(atlasSession.getMapping());
        atlasSession.getValidations().getValidation().addAll(javaValidations);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Detected " + javaValidations.size() + " java validation notices");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreValidation completed", getDocId());
        }
    }

    @Override
    public void processPreSourceExecution(AtlasInternalSession atlasSession) throws AtlasException {
        if (atlasSession == null || atlasSession.getMapping() == null || atlasSession.getMapping().getMappings() == null
                || atlasSession.getMapping().getMappings().getMapping() == null) {
            throw new AtlasException("AtlasSession not properly intialized with a mapping that contains field mappings");
        }

        Object sourceDocument = atlasSession.getSourceDocument(getDocId());
        if (sourceDocument == null) {
            AtlasUtil.addAudit(atlasSession, getDocId(), String.format(
                    "Null source document: docId='%s'", getDocId()),
                    AuditStatus.WARN, null);
        } else {
            JavaFieldReader reader = new JavaFieldReader();
            reader.setConversionService(getConversionService());
            reader.setDocument(sourceDocument);
            atlasSession.setFieldReader(getDocId(), reader);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreSourceExcution completed", getDocId());
        }
    }

    @Override
    public void processPreTargetExecution(AtlasInternalSession atlasSession) throws AtlasException {
        if (atlasSession == null || atlasSession.getMapping() == null || atlasSession.getMapping().getMappings() == null
                || atlasSession.getMapping().getMappings().getMapping() == null) {
            throw new AtlasException("AtlasSession not properly intialized with a mapping that contains field mappings");
        }

        Object rootObject;
        String targetClassName = AtlasUtil.unescapeFromUri(AtlasUtil.getUriParameterValue(getUri(), "className"));
        String collectionTypeStr = AtlasUtil.unescapeFromUri(AtlasUtil.getUriParameterValue(getUri(), "collectionType"));
        CollectionType collectionType = collectionTypeStr != null ? CollectionType.fromValue(collectionTypeStr) : CollectionType.NONE;
        String collectionClassName = AtlasUtil.unescapeFromUri(AtlasUtil.getUriParameterValue(getUri(), "collectionClassName"));

        JavaFieldWriter writer = new JavaFieldWriter(this.writerUtil);
        Class<?> clazz = writerUtil.loadClass(targetClassName);
        if (collectionType == CollectionType.ARRAY) {
            rootObject = Array.newInstance(clazz, 0);
        } else if (collectionType != CollectionType.NONE) {
            if (collectionClassName != null) {
                rootObject = writerUtil.instantiateObject(writerUtil.loadClass(collectionClassName));
            } else {
                rootObject = writerUtil.instantiateObject(writerUtil.getDefaultCollectionImplClass(collectionType));
            }
            writer.setCollectionItemClass(clazz);
        } else {
            rootObject = writerUtil.instantiateObject(clazz);
        }
        writer.setRootObject(rootObject);
        writer.setTargetValueConverter(targetValueConverter);
        atlasSession.setFieldWriter(getDocId(), writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreTargetExcution completed", getDocId());
        }
    }

    @Override
    public void readSourceValue(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        JavaFieldReader reader = session.getFieldReader(getDocId(), JavaFieldReader.class);
        if (reader == null) {
            AtlasUtil.addAudit(session, sourceField, String.format(
                    "Source document '%s' doesn't exist", getDocId()),
                    AuditStatus.ERROR, null);
            return;
        }
        reader.read(session);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processSourceFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}]",
                    getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(), sourceField.getValue());
        }
    }

    @Override
    public void populateTargetField(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();
        AtlasPath path = new AtlasPath(targetField.getPath());
        FieldGroup targetFieldGroup = null;
        if  (path.hasCollection() && !path.isIndexedCollection()) {
            targetFieldGroup = AtlasModelFactory.createFieldGroupFrom(targetField, true);
            session.head().setTargetField(targetFieldGroup);
        }

        JavaFieldWriter writer = session.getFieldWriter(getDocId(), JavaFieldWriter.class);
        if (targetFieldGroup == null) {
            if (sourceField instanceof FieldGroup) {
                List<Field> subFields = ((FieldGroup)sourceField).getField();
                if (subFields == null || subFields.size() == 0) {
                    return;
                }
                Integer index = targetField.getIndex();
                if (index != null) {
                    if (subFields.size() > index) {
                        sourceField = subFields.get(index);
                    } else {
                        AtlasUtil.addAudit(session, getDocId(), String.format(
                                "The number of source fields (%s) is smaller than target index (%s) - ignoring",
                                subFields.size(), index),
                                AuditStatus.WARN, null);
                        return;
                    }
                } else {
                    // The last one wins for compatibility
                    sourceField = subFields.get(subFields.size() - 1);
                }
                session.head().setSourceField(sourceField);
            }
            /* Lazy parent instantiation, unless specific mapping defined for a complex type (Example json -> java)
                Only instantiate the parent if there is a child value to avoid null src class -> empty dst class mapping
                This will ensure null src class maps to null destination class
            */
            Object parentObject = null;
            if (null != sourceField.getValue() || (null == sourceField.getValue() && targetField.getFieldType() == FieldType.COMPLEX && !(targetField instanceof JavaEnumField))) {
                parentObject = writer.prepareParentObject(session);
            }
            if (parentObject != null) {
                writer.populateTargetFieldValue(session, parentObject);
                writer.enqueueFieldAndParent(targetField, parentObject);
            }
        } else if (sourceField instanceof FieldGroup) {
            if (((FieldGroup)sourceField).getField().size() == 0) {
                Object parentObject = writer.prepareParentObject(session);
                if (parentObject != null) {
                    writer.enqueueFieldAndParent(targetFieldGroup, parentObject);
                }
            }

            Field previousTargetSubField = null;
            for (int i=0; i<((FieldGroup)sourceField).getField().size(); i++) {
                Field sourceSubField = ((FieldGroup)sourceField).getField().get(i);
                Field targetSubField = targetField instanceof JavaEnumField ? new JavaEnumField() : new JavaField();
                AtlasJavaModelFactory.copyField(targetField, targetSubField, false);
                getCollectionHelper().copyCollectionIndexes(sourceField, sourceSubField, targetSubField, previousTargetSubField);
                previousTargetSubField = targetSubField;
                targetFieldGroup.getField().add(targetSubField);
                session.head().setSourceField(sourceSubField);
                session.head().setTargetField(targetSubField);
                Object parentObject = writer.prepareParentObject(session);
                if (parentObject != null) {
                    writer.populateTargetFieldValue(session, parentObject);
                    writer.enqueueFieldAndParent(targetSubField, parentObject);
                }
            }
            session.head().setSourceField(sourceField);
            session.head().setTargetField(targetFieldGroup);
        } else {
            Field targetSubField = targetField instanceof JavaEnumField ? new JavaEnumField() : new JavaField();
            AtlasJavaModelFactory.copyField(targetField, targetSubField, false);
            path.setVacantCollectionIndex(0);
            targetSubField.setPath(path.toString());
            targetFieldGroup.getField().add(targetSubField);
            session.head().setTargetField(targetSubField);
            Object parentObject = writer.prepareParentObject(session);
            if (parentObject != null) {
                writer.populateTargetFieldValue(session, parentObject);
                writer.enqueueFieldAndParent(targetSubField, parentObject);
            }
            session.head().setTargetField(targetFieldGroup);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processTargetFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}], TargetField:[docId={}, path={}, type={}, value={}]",
                    getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(), sourceField.getValue(),
                    targetField.getDocId(), targetField.getPath(), targetField.getFieldType(), targetField.getValue());
        }
    }

    @Override
    public void writeTargetValue(AtlasInternalSession session) throws AtlasException {
        JavaFieldWriter writer = session.getFieldWriter(getDocId(), JavaFieldWriter.class);
        writer.commitWriting(session);
    }

    @Override
    public void processPostSourceExecution(AtlasInternalSession session) throws AtlasException {
        session.removeFieldReader(getDocId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPostSourceExecution completed", getDocId());
        }
    }

    @Override
    public void processPostTargetExecution(AtlasInternalSession session) throws AtlasException {
        JavaFieldWriter writer = session.getFieldWriter(getDocId(), JavaFieldWriter.class);
        if (writer != null && writer.getRootObject() != null) {
            session.setTargetDocument(getDocId(), writer.getRootObject());
        } else {
            AtlasUtil.addAudit(session, getDocId(),
                    String.format("No target document created for DataSource '%s'", getDocId()),
                    AuditStatus.WARN, null);
        }
        session.removeFieldWriter(getDocId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPostTargetExecution completed", getDocId());
        }
    }

    @Override
    public Boolean isSupportedField(Field field) {
        if (super.isSupportedField(field)) {
            return true;
        }
        return field instanceof JavaField || field instanceof JavaEnumField;
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return AtlasJavaModelFactory.cloneJavaField(field, true);
    }

    @Override
    public JavaField createField() {
        return AtlasJavaModelFactory.createJavaField();
    }

}
