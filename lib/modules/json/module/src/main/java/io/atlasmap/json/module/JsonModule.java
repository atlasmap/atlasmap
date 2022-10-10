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
package io.atlasmap.json.module;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.json.core.JsonFieldReader;
import io.atlasmap.json.core.JsonFieldWriter;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonEnumField;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.Validations;

@AtlasModuleDetail(name = "JsonModule", uri = "atlas:json", modes = { "SOURCE", "TARGET" }, dataFormats = {
        "json" }, configPackages = { "io.atlasmap.json.v2" })
public class JsonModule extends BaseAtlasModule {
    private static final Logger LOG = LoggerFactory.getLogger(JsonModule.class);

    @Override
    public void processPreValidation(AtlasInternalSession atlasSession) throws AtlasException {
        if (atlasSession == null || atlasSession.getMapping() == null) {
            throw new AtlasValidationException("Invalid session: Session and AtlasMapping must be specified");
        }

        Validations validations = atlasSession.getValidations();
        JsonValidationService jsonValidationService = new JsonValidationService(getConversionService(), getFieldActionService());
        jsonValidationService.setMode(getMode());
        jsonValidationService.setDocId(getDocId());
        List<Validation> jsonValidations = jsonValidationService.validateMapping(atlasSession.getMapping());
        if (jsonValidations != null && !jsonValidations.isEmpty()) {
            validations.getValidation().addAll(jsonValidations);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Detected " + jsonValidations.size() + " json validation notices");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreValidation completed", getDocId());
        }
    }

    @Override
    public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
        Object sourceDocument = session.getSourceDocument(getDocId());
        String sourceDocumentString = null;
        if (sourceDocument == null || !(sourceDocument instanceof String)) {
            AtlasUtil.addAudit(session, getDocId(), String.format(
                    "Null or non-String source document: docId='%s'", getDocId()),
                    AuditStatus.WARN, null);
        } else {
            sourceDocumentString = String.class.cast(sourceDocument);
        }
        JsonFieldReader fieldReader = new JsonFieldReader(getConversionService());
        fieldReader.setDocument(sourceDocumentString);
        session.setFieldReader(getDocId(), fieldReader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} processPreSourceExcution completed", getDocId());
        }
    }

    @Override
    public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
        JsonFieldWriter writer = new JsonFieldWriter();
        session.setFieldWriter(getDocId(), writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} processPreTargetExcution completed", getDocId());
        }
    }

    @Override
    public void readSourceValue(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        JsonFieldReader reader = session.getFieldReader(getDocId(), JsonFieldReader.class);
        if (reader == null) {
            AtlasUtil.addAudit(session, sourceField, String.format(
                    "Source document '%s' doesn't exist", getDocId()),
                    AuditStatus.ERROR, null);
            return;
        }
        reader.read(session);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processSourceFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}]",
                    getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(),
                    sourceField.getValue());
        }
    }

    @Override
    public void populateTargetField(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();
        AtlasPath path = new AtlasPath(targetField.getPath());
        FieldGroup targetFieldGroup = null;
        if (path.hasCollection() && !path.isIndexedCollection()) {
            targetFieldGroup = AtlasModelFactory.createFieldGroupFrom(targetField, true);
            targetFieldGroup.setStatus(sourceField.getStatus());
            session.head().setTargetField(targetFieldGroup);
        }

        // Attempt to Auto-detect field type based on input value
        if (targetField.getFieldType() == null && sourceField.getValue() != null) {
            targetField.setFieldType(getConversionService().fieldTypeFromClass(sourceField.getValue().getClass()));
        }

        if (targetFieldGroup == null) {
            if (sourceField instanceof FieldGroup) {
                List<Field> subFields = ((FieldGroup)sourceField).getField();
                if (subFields != null && subFields.size() > 0) {
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
            }
            super.populateTargetField(session);
        } else if (sourceField instanceof FieldGroup) {
            Field previousTargetSubField = null;
            for (int i=0; i<((FieldGroup)sourceField).getField().size(); i++) {
                Field sourceSubField = ((FieldGroup)sourceField).getField().get(i);
                JsonField targetSubField = new JsonField();
                AtlasJsonModelFactory.copyField(targetField, targetSubField, false);
                getCollectionHelper().copyCollectionIndexes(sourceField, sourceSubField, targetSubField, previousTargetSubField);
                previousTargetSubField = targetSubField;
                targetFieldGroup.getField().add(targetSubField);
                session.head().setSourceField(sourceSubField);
                session.head().setTargetField(targetSubField);
                super.populateTargetField(session);
            }
            session.head().setSourceField(sourceField);
            session.head().setTargetField(targetFieldGroup);
        } else {
            JsonField targetSubField = new JsonField();
            AtlasJsonModelFactory.copyField(targetField, targetSubField, false);
            path.setVacantCollectionIndex(0);
            targetSubField.setPath(path.toString());
            targetFieldGroup.getField().add(targetSubField);
            session.head().setTargetField(targetSubField);
            super.populateTargetField(session);
            session.head().setTargetField(targetFieldGroup);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "{}: processTargetFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}], TargetField:[docId={}, path={}, type={}, value={}]",
                    getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(),
                    sourceField.getValue(), targetField.getDocId(), targetField.getPath(), targetField.getFieldType(),
                    targetField.getValue());
        }
    }

    public void writeTargetValue(AtlasInternalSession session) throws AtlasException {
        JsonFieldWriter writer = session.getFieldWriter(getDocId(), JsonFieldWriter.class);
        if (session.head().getTargetField() instanceof FieldGroup) {
            FieldGroup targetFieldGroup = (FieldGroup) session.head().getTargetField();
            if (targetFieldGroup.getField().size() > 0) {
                for (Field f : targetFieldGroup.getField()) {
                    session.head().setTargetField(f);
                    writer.write(session);
                }
                return;
            }
        }
        writer.write(session);
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
        JsonFieldWriter writer = session.getFieldWriter(getDocId(), JsonFieldWriter.class);
        if (writer != null && writer.getRootNode() != null) {
            String outputBody = writer.getRootNode().toString();
            session.setTargetDocument(getDocId(), outputBody);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("processPostTargetExecution converting JsonNode to string size=%s",
                        outputBody.length()));
            }
        } else {
            AtlasUtil.addAudit(session, getDocId(), String
                    .format("No target document created for DataSource:[id=%s, uri=%s]", getDocId(), this.getUri()),
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
        return field instanceof JsonField || field instanceof JsonEnumField;
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return AtlasJsonModelFactory.cloneField((JsonField)field, true);
    }

    @Override
    public JsonField createField() {
        return AtlasJsonModelFactory.createJsonField();
    }

}
