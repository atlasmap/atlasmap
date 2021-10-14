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
package io.atlasmap.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasPreviewContext;
import io.atlasmap.spi.AtlasCollectionHelper;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.FieldDirection;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Json;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.SimpleField;

/**
 * Limited version of AtlasMap context dedicated for preview processing.
 * Since preview exchanges field values via {@code Field} object, It doesn't interact with
 * actual {@code AtlasModule} which handles data format specific work, but read the values
 * from {@code Field} object in the mapping directly.
 */
class DefaultAtlasPreviewContext extends DefaultAtlasContext implements AtlasPreviewContext {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasPreviewContext.class);

    private Mapping originalMapping;
    private ObjectMapper jsonMapper;
    private PreviewModule previewModule = new PreviewModule();
    private AtlasCollectionHelper collectionHelper;


    DefaultAtlasPreviewContext(DefaultAtlasContextFactory factory) {
        super(factory, new AtlasMapping());
        this.jsonMapper = Json.withClassLoader(factory.getClassLoader());
        this.collectionHelper = new DefaultAtlasCollectionHelper(factory.getFieldActionService());
    }

    /**
     * Process single mapping entry in preview mode. Since modules don't participate
     * in preview mode, any document format specific function won't be applied.
     *
     * @param mapping A @link{Mapping} entry to process
     */
    @Override
    public Audits processPreview(Mapping mapping) throws AtlasException {
        DefaultAtlasSession session = new DefaultAtlasSession(this);
        this.originalMapping = mapping;
        Mapping cloned;
        try {
            byte[] serialized = jsonMapper.writeValueAsBytes(mapping);
            cloned = jsonMapper.readValue(serialized, Mapping.class);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
        session.head().setMapping(cloned);
        MappingType mappingType = cloned.getMappingType();
        String expression = cloned.getExpression();
        FieldGroup sourceFieldGroup = cloned.getInputFieldGroup();
        List<Field> sourceFields = cloned.getInputField();
        List<Field> targetFields = cloned.getOutputField();

        targetFields.forEach(tf -> tf.setValue(null));
        if ((sourceFieldGroup == null && sourceFields.isEmpty()) || targetFields.isEmpty()) {
            return session.getAudits();
        }
        if (sourceFieldGroup != null) {
            sourceFields = sourceFieldGroup.getField();
        }
        for (Field sf : sourceFields) {
            if (sf.getFieldType() == null || sf.getValue() == null) {
                continue;
            }
            if (sf.getValue() instanceof String && ((String)sf.getValue()).isEmpty()) {
                continue;
            }
            if (!restoreSourceFieldType(session, sf)) {
                return session.getAudits();
            }
        }

        processSourceFieldMapping(session);
        if (session.hasErrors()) {
            return session.getAudits();
        }

        Field sourceField = session.head().getSourceField();
        Field targetField;

        if (mappingType == null || mappingType == MappingType.MAP) {
            sourceFieldGroup = sourceField instanceof FieldGroup ? (FieldGroup) sourceField : null;
            for (int i=0; i<targetFields.size(); i++) {
                targetField = targetFields.get(i);
                session.head().setTargetField(targetField);
                if (sourceFieldGroup != null) {
                    if (sourceFieldGroup.getField().size() == 0) {
                        AtlasUtil.addAudit(session, targetField, String.format(
                                "Skipping empty source group field '%s:%s'",
                                sourceField.getDocId(), sourceField.getPath()),
                                AuditStatus.INFO, null);
                        continue;
                    }
                    Integer index = targetField.getIndex();
                    AtlasPath targetPath = new AtlasPath(targetField.getPath());
                    if (targetPath.hasCollection() && !targetPath.isIndexedCollection()) {
                        if (targetFields.size() > 1) {
                            AtlasUtil.addAudit(session, targetField,
                                    "It's not yet supported to have a collection field as a part of multiple target fields in a same mapping",
                                    AuditStatus.ERROR, null);
                            session.getAudits().getAudit().addAll(session.head().getAudits());
                            return session.getAudits();
                        }
                        if (index != null) {
                            LOG.warn("Field index '{}' is detected on target field '{}:{}' while there's only one target field, ignoring",
                                    index, targetField.getDocId(), targetField.getPath());
                            targetField.setIndex(null);
                        }
                        FieldGroup targetFieldGroup = targetField instanceof FieldGroup
                                ? (FieldGroup)targetField
                                : AtlasModelFactory.createFieldGroupFrom(targetField, true);
                        targetFields.set(i, targetFieldGroup);
                        Field previousTargetField = null;
                        for (Field subSourceField : sourceFieldGroup.getField()) {
                            Field subTargetField = AtlasModelFactory.cloneFieldToSimpleField(targetFieldGroup);
                            targetFieldGroup.getField().add(subTargetField);
                            collectionHelper.copyCollectionIndexes(sourceFieldGroup, subSourceField, subTargetField, previousTargetField);
                            previousTargetField = subTargetField;
                            if (!convertSourceToTarget(session, subSourceField, subTargetField)) {
                                session.getAudits().getAudit().addAll(session.head().getAudits());
                                return session.getAudits();
                            };
                            Field processed = subTargetField;
                            if (expression == null || expression.isEmpty()) {
                                processed = applyFieldActions(session, subTargetField);
                            }
                            subTargetField.setValue(processed.getValue());
                        }
                        continue;
                    } else if (index == null) {
                        session.head().setSourceField(sourceFieldGroup.getField().get(sourceFieldGroup.getField().size()-1));
                    } else {
                        if (sourceFieldGroup.getField().size() > index) {
                            session.head().setSourceField(sourceFieldGroup.getField().get(index));
                        } else {
                            AtlasUtil.addAudit(session, targetField, String.format(
                                    "The number of source fields '%s' is fewer than expected via target field index '%s'",
                                    sourceFieldGroup.getField().size(), targetField.getIndex()),
                                    AuditStatus.WARN, null);
                            continue;
                        }
                    }
                }
                if (session.hasErrors()) {
                    session.getAudits().getAudit().addAll(session.head().getAudits());
                    return session.getAudits();
                }
                if (!convertSourceToTarget(session, session.head().getSourceField(), targetField)) {
                    session.getAudits().getAudit().addAll(session.head().getAudits());
                    return session.getAudits();
                }
                Field processed = targetField;
                if (expression == null || expression.isEmpty()) {
                    processed = applyFieldActions(session, targetField);
                }
                targetField.setValue(processed.getValue());
            }

        } else if (mappingType == MappingType.COMBINE) {
            targetField = targetFields.get(0);
            Field combined = processCombineField(session, cloned, sourceFields, targetField);
            if (!convertSourceToTarget(session, combined, targetField)) {
                session.getAudits().getAudit().addAll(session.head().getAudits());
                return session.getAudits();
            }
            applyFieldActions(session, targetField);

        } else if (mappingType == MappingType.SEPARATE) {
            List<Field> separatedFields;
            try {
                separatedFields = processSeparateField(session, cloned, sourceField);
            } catch (AtlasException e) {
                AtlasUtil.addAudit(session, sourceField, String.format(
                        "Failed to separate field: %s", AtlasUtil.getChainedMessage(e)),
                        AuditStatus.ERROR, null);
                if (LOG.isDebugEnabled()) {
                    LOG.error("", e);
                }
                session.getAudits().getAudit().addAll(session.head().getAudits());
                return session.getAudits();
            }
            if (separatedFields == null) {
                session.getAudits().getAudit().addAll(session.head().getAudits());
                return session.getAudits();
            }
            for (Field f : targetFields) {
                targetField = f;
                if (targetField.getIndex() == null || targetField.getIndex() < 0) {
                    AtlasUtil.addAudit(session, targetField, String.format(
                            "Separate requires zero or positive Index value to be set on targetField targetField.path=%s",
                            targetField.getPath()), AuditStatus.WARN, null);
                    continue;
                }
                if (separatedFields.size() <= targetField.getIndex()) {
                    String errorMessage = String.format(
                            "Separate returned fewer segments count=%s when targetField.path=%s requested index=%s",
                            separatedFields.size(), targetField.getPath(), targetField.getIndex());
                    AtlasUtil.addAudit(session, targetField, errorMessage, AuditStatus.WARN, null);
                    break;
                }
                if (!convertSourceToTarget(session, separatedFields.get(targetField.getIndex()), targetField)) {
                    break;
                }
                applyFieldActions(session, targetField);
            }

        } else {
            AtlasUtil.addAudit(session, (String)null, String.format(
                    "Unsupported mappingType=%s detected", cloned.getMappingType()),
                    AuditStatus.ERROR, null);
        }
        mapping.getOutputField().clear();
        mapping.getOutputField().addAll(cloned.getOutputField());
        session.getAudits().getAudit().addAll(session.head().getAudits());
        return session.getAudits();
    }

    private boolean restoreSourceFieldType(DefaultAtlasSession session, Field sourceField) throws AtlasException {
        try {
            Object sourceValue = getContextFactory().getConversionService().convertType(
                    sourceField.getValue(), null, sourceField.getFieldType(), null);
            sourceField.setValue(sourceValue);
        } catch (AtlasConversionException e) {
            AtlasUtil.addAudit(session, sourceField, String.format(
                    "Wrong format for source value : %s", AtlasUtil.getChainedMessage(e)),
                    AuditStatus.ERROR, null);
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
            return false;
        }
        return true;
    }

    private boolean convertSourceToTarget(DefaultAtlasSession session, Field sourceField, Field targetField)
            throws AtlasException {
        Object targetValue = null;
        if (sourceField.getFieldType() != null && sourceField.getFieldType().equals(targetField.getFieldType())) {
            targetValue = sourceField.getValue();
        } else if (sourceField.getValue() != null) {
            try {
                targetValue = getContextFactory().getConversionService().convertType(sourceField.getValue(), sourceField.getFormat(),
                        targetField.getFieldType(), targetField.getFormat());
            } catch (AtlasConversionException e) {
                AtlasUtil.addAudit(session, targetField, String.format(
                        "Failed to convert source value to target type: %s", AtlasUtil.getChainedMessage(e)),
                        AuditStatus.ERROR, null);
                if (LOG.isDebugEnabled()) {
                    LOG.error("", e);
                }
                return false;
            }
        }
        targetField.setValue(targetValue);
        return true;
    }

    private class PreviewModule extends BaseAtlasModule {

        @Override
        public void readSourceValue(AtlasInternalSession session) throws AtlasException {
            Field sourceField = session.head().getSourceField();
            Mapping mapping = session.head().getMapping();
            FieldGroup sourceFieldGroup = mapping.getInputFieldGroup();
            if (sourceFieldGroup != null) {
                if (matches(sourceField, sourceFieldGroup)) {
                    session.head().setSourceField(sourceFieldGroup);
                    return;
                }
                 Field f = readFromGroup(sourceFieldGroup, sourceField);
                 session.head().setSourceField(f);
                 return;
            }
            for (Field f : mapping.getInputField()) {
                if (matches(sourceField, f)) {
                    session.head().setSourceField(f);
                    return;
                }
            }
        }

        private boolean matches(Field f1, Field f2) {
            if ((f1.getDocId() == null && f2.getDocId() != null)
                    || (f1.getDocId() != null && f2.getDocId() == null)
                    || (f1.getDocId() != null && !f1.getDocId().equals(f2.getDocId()))) {
                return false;
            }
            if (f2.getPath() != null && f2.getPath().equals(f1.getPath())) {
                return true;
            }
            return false;
        }

        private Field readFromGroup(FieldGroup group, Field field) {
            if (group.getField() == null) {
                return null;
            }
            for (Field f : group.getField()) {
                if (matches(field, f)) {
                    return f;
                }
                if (f instanceof FieldGroup) {
                    Field deeper = readFromGroup((FieldGroup)f, field);
                    if (deeper != null) {
                        return deeper;
                    }
                }
            }
            return null;
        }

        @Override
        public Boolean isSupportedField(Field field) {
            // The field type doesn't matter for preview
            return true;
        }

        @Override
        public void processPreValidation(AtlasInternalSession session) throws AtlasException {
        }

        @Override
        public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
        }

        @Override
        public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
        }

        @Override
        public void writeTargetValue(AtlasInternalSession session) throws AtlasException {
        }

        @Override
        public void processPostSourceExecution(AtlasInternalSession session) throws AtlasException {
        }

        @Override
        public void processPostTargetExecution(AtlasInternalSession session) throws AtlasException {
        }

        @Override
        public Field cloneField(Field field) throws AtlasException {
            return null;
        }

        @Override
        public String getDocName() {
            return "Preview";
        }

        @Override
        public String getDocId() {
            return "Preview";
        }

        @Override
        public SimpleField createField() {
            return new SimpleField();
        }

    };

    @Override
    public Map<String, AtlasModule> getSourceModules() {
        return new HashMap<String, AtlasModule>() {
            private static final long serialVersionUID = 1L;

            @Override
            public AtlasModule get(Object key) {
                return previewModule;
            }
        };
    }

    @Override
    protected AtlasModule resolveModule(FieldDirection direction, Field field) {
        return previewModule;
    }

}
