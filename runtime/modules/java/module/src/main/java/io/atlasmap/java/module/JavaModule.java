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
package io.atlasmap.java.module;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasModuleSupport;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.java.core.DocumentJavaFieldReader;
import io.atlasmap.java.core.DocumentJavaFieldWriter;
import io.atlasmap.java.core.TargetValueConverter;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.inspect.JavaConstructService;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Validation;

@AtlasModuleDetail(name = "JavaModule", uri = "atlas:java", modes = { "SOURCE", "TARGET" }, dataFormats = {
        "java" }, configPackages = { "io.atlasmap.java.v2" })
public class JavaModule extends BaseAtlasModule {
    public static final String DEFAULT_LIST_CLASS = "java.util.ArrayList";
    private static final Logger LOG = LoggerFactory.getLogger(JavaModule.class);

    private ClassInspectionService javaInspectionService = null;
    private JavaConstructService javaConstructService = null;
    private TargetValueConverter targetValueConverter = null;
    private ClassLoader classLoader;

    public JavaModule() {
        this.setAutomaticallyProcessOutputFieldActions(false);
    }

    @Override
    public void init() {
        // TODO support non-flat class loader
        this.classLoader = Thread.currentThread().getContextClassLoader();
        javaInspectionService = new ClassInspectionService();
        javaInspectionService.setConversionService(getConversionService());
        setJavaInspectionService(javaInspectionService);

        javaConstructService = new JavaConstructService();
        javaConstructService.setConversionService(getConversionService());
        setJavaConstructService(javaConstructService);

        targetValueConverter = new TargetValueConverter(classLoader, getConversionService());
    }

    @Override
    public void destroy() {
        javaInspectionService = null;
        javaConstructService = null;
    }

    @Override
    public void processPreValidation(AtlasInternalSession atlasSession) throws AtlasException {
        if (atlasSession == null || atlasSession.getMapping() == null) {
            LOG.error("Invalid session: Session and AtlasMapping must be specified");
            throw new AtlasValidationException("Invalid session");
        }

        JavaValidationService javaValidator = new JavaValidationService(getConversionService());
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

        if (javaInspectionService == null) {
            javaInspectionService = new ClassInspectionService();
            javaInspectionService.setConversionService(getConversionService());
        }

        Object sourceDocument = atlasSession.getSourceDocument(getDocId());
        if (sourceDocument == null) {
            AtlasUtil.addAudit(atlasSession, getDocId(), String.format(
                    "Null source document: docId='%s'", getDocId()),
                    null, AuditStatus.WARN, null);
        } else {
            DocumentJavaFieldReader reader = new DocumentJavaFieldReader();
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

        if (javaInspectionService == null) {
            javaInspectionService = new ClassInspectionService();
            javaInspectionService.setConversionService(getConversionService());
        }

        List<BaseMapping> mapping = atlasSession.getMapping().getMappings().getMapping();
        Object rootObject;
        String targetClassName = AtlasUtil.getUriParameterValue(getUri(), "className");
        JavaClass inspectClass = getJavaInspectionService().inspectClass(targetClassName);
        merge(inspectClass, mapping);
        List<String> targetPaths = AtlasModuleSupport.listTargetPaths(mapping);
        try {
            rootObject = getJavaConstructService().constructClass(inspectClass, targetPaths);
        } catch (Exception e) {
            throw new AtlasException(e);
        }

        DocumentJavaFieldWriter writer = new DocumentJavaFieldWriter(getConversionService());
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
        DocumentJavaFieldReader reader = session.getFieldReader(getDocId(), DocumentJavaFieldReader.class);
        if (reader == null) {
            AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                    "Source document '%s' doesn't exist", getDocId()),
                    sourceField.getPath(), AuditStatus.ERROR, null);
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
            targetFieldGroup = AtlasModelFactory.createFieldGroupFrom(targetField);
            session.head().setTargetField(targetFieldGroup);
        }

        DocumentJavaFieldWriter writer = session.getFieldWriter(getDocId(), DocumentJavaFieldWriter.class);
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
                                null, AuditStatus.WARN, null);
                        return;
                    }
                } else {
                    // The last one wins for compatibility
                    sourceField = subFields.get(subFields.size() - 1);
                }
                session.head().setSourceField(sourceField);
            }
            Object parentObject = writer.getParentObject(session);
            if (parentObject != null) {
                writer.populateTargetFieldValue(session, parentObject);
                writer.enqueueFieldAndParent(targetField, parentObject);
            }
        } else if (sourceField instanceof FieldGroup) {
            for (int i=0; i<((FieldGroup)sourceField).getField().size(); i++) {
                Field sourceSubField = ((FieldGroup)sourceField).getField().get(i);
                Field targetSubField = targetField instanceof JavaEnumField ? new JavaEnumField() : new JavaField();
                AtlasJavaModelFactory.copyField(targetField, targetSubField, false);
                AtlasPath subPath = new AtlasPath(targetField.getPath());
                subPath.setVacantCollectionIndex(i);
                targetSubField.setPath(subPath.toString());
                targetFieldGroup.getField().add(targetSubField);
                session.head().setSourceField(sourceSubField);
                session.head().setTargetField(targetSubField);
                Object parentObject = writer.getParentObject(session);
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
            Object parentObject = writer.getParentObject(session);
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
        DocumentJavaFieldWriter writer = session.getFieldWriter(getDocId(), DocumentJavaFieldWriter.class);
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
        DocumentJavaFieldWriter writer = session.getFieldWriter(getDocId(), DocumentJavaFieldWriter.class);
        if (writer != null && writer.getRootObject() != null) {
            session.setTargetDocument(getDocId(), writer.getRootObject());
        } else {
            AtlasUtil.addAudit(session, getDocId(),
                    String.format("No target document created for DataSource '%s'", getDocId()),
                    null, AuditStatus.WARN, null);
        }
        session.removeFieldWriter(getDocId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPostTargetExecution completed", getDocId());
        }
    }

    private void merge(JavaClass inspectionClass, List<BaseMapping> mappings) {
        if (inspectionClass == null || inspectionClass.getJavaFields() == null
                || inspectionClass.getJavaFields().getJavaField() == null) {
            return;
        }

        if (mappings == null || mappings.size() == 0) {
            return;
        }

        for (BaseMapping fm : mappings) {
            if (fm instanceof Mapping && (((Mapping) fm).getOutputField() != null)) {
                Field f = ((Mapping) fm).getOutputField().get(0);
                if (f.getPath() != null) {
                    Field inspectField = findFieldByPath(inspectionClass, f.getPath());
                    if (inspectField != null && f instanceof JavaField && inspectField instanceof JavaField) {
                        String overrideClassName = ((JavaField) f).getClassName();
                        JavaField javaInspectField = (JavaField) inspectField;
                        // Support mapping overrides className
                        if (overrideClassName != null
                                && !overrideClassName.equals(javaInspectField.getClassName())) {
                            javaInspectField.setClassName(overrideClassName);
                        }
                    }
                }
            }
        }
    }

    private JavaField findFieldByPath(JavaClass javaClass, String javaPath) {
        if (javaClass == null || javaClass.getJavaFields() == null
                || javaClass.getJavaFields().getJavaField() == null) {
            return null;
        }

        for (JavaField jf : javaClass.getJavaFields().getJavaField()) {
            if (jf.getPath().equals(javaPath)) {
                return jf;
            }
            if (jf instanceof JavaClass) {
                JavaField childJavaField = findFieldByPath((JavaClass) jf, javaPath);
                if (childJavaField != null) {
                    return childJavaField;
                }
            }
        }

        return null;
    }

    public ClassInspectionService getJavaInspectionService() {
        return javaInspectionService;
    }

    public void setJavaInspectionService(ClassInspectionService javaInspectionService) {
        this.javaInspectionService = javaInspectionService;
    }

    public JavaConstructService getJavaConstructService() {
        return javaConstructService;
    }

    public void setJavaConstructService(JavaConstructService javaConstructService) {
        this.javaConstructService = javaConstructService;
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
        return AtlasJavaModelFactory.cloneJavaField(field);
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

}
