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
package io.atlasmap.kafkaconnect.module;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.kafka.connect.data.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.kafkaconnect.core.KafkaConnectFieldReader;
import io.atlasmap.kafkaconnect.core.KafkaConnectFieldWriter;
import io.atlasmap.kafkaconnect.core.KafkaConnectUtil;
import io.atlasmap.kafkaconnect.v2.AtlasKafkaConnectModelFactory;
import io.atlasmap.kafkaconnect.v2.KafkaConnectConstants;
import io.atlasmap.kafkaconnect.v2.KafkaConnectEnumField;
import io.atlasmap.kafkaconnect.v2.KafkaConnectField;
import io.atlasmap.kafkaconnect.v2.KafkaConnectSchemaType;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.DocumentMetadata;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.Validations;

/**
 * The {@link io.atlasmap.spi.AtlasModule} implementation for Kafka Connect.
 */
@AtlasModuleDetail(name = "KafkaConnectModule", uri = "atlas:kafkaconnect", modes = { "SOURCE", "TARGET" }, dataFormats = {
        "kafkaconnect" }, configPackages = { "io.atlasmap.kafkaconnect.v2" })
public class KafkaConnectModule extends BaseAtlasModule {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectModule.class);

    private boolean isKey;
    private Type rootSchemaType;

    @Override
    public void init() throws AtlasException {
        super.init();
        String typeStr = AtlasUtil.unescapeFromUri(AtlasUtil.getUriParameterValue(getUri(), "rootSchemaType"));
        rootSchemaType = typeStr != null ? Type.valueOf(typeStr) : Type.STRUCT;
        String isKeyStr = AtlasUtil.unescapeFromUri(AtlasUtil.getUriParameterValue(getUri(), "isKey"));
        isKey = isKeyStr != null ? Boolean.parseBoolean(isKeyStr) : false;
    }

    @Override
    public void processPreValidation(AtlasInternalSession session) throws AtlasException {
        if (session == null || session.getMapping() == null) {
            throw new AtlasValidationException("Invalid session: Session and AtlasMapping must be specified");
        }

        Validations validations = session.getValidations();
        KafkaConnectValidationService kafkaConnectValidationService = new KafkaConnectValidationService(getConversionService(), getFieldActionService());
        kafkaConnectValidationService.setMode(getMode());
        kafkaConnectValidationService.setDocId(getDocId());
        List<Validation> kafkaConnectValidations = kafkaConnectValidationService.validateMapping(session.getMapping());
        if (kafkaConnectValidations != null && !kafkaConnectValidations.isEmpty()) {
            validations.getValidation().addAll(kafkaConnectValidations);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Detected " + kafkaConnectValidations.size() + " json validation notices");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreValidation completed", getDocId());
        }
    }

    @Override
    public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
        Object sourceDocument = session.getSourceDocument(getDocId());
        KafkaConnectFieldReader fieldReader = new KafkaConnectFieldReader(getConversionService());
        fieldReader.setDocument(sourceDocument);
        fieldReader.setSchema(extractSchema(getDocumentMetadata(), getDocumentSpecificationFile()));
        session.setFieldReader(getDocId(), fieldReader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} processPreSourceExcution completed", getDocId());
        }
    }

    private org.apache.kafka.connect.data.Schema extractSchema(DocumentMetadata meta, File specFile) throws AtlasException {
        if (specFile == null || !specFile.exists()) {
            return null;
        }
        String typeStr = meta.getInspectionParameters().get(KafkaConnectConstants.OPTIONS_SCHEMA_TYPE);
        KafkaConnectSchemaType type = KafkaConnectSchemaType.valueOf(typeStr);
        HashMap<String, Object> options = KafkaConnectUtil.repackParserOptions(meta.getInspectionParameters());
        try {
            switch (type) {
                case JSON:
                    return KafkaConnectUtil.parseJson(new FileInputStream(specFile), options);
                case AVRO:
                    return KafkaConnectUtil.parseAvro(new FileInputStream(specFile), options);
                default:
                    LOG.warn("Ignoring unsupported KafkaConnect schema type '{}'", type);
                    return null;
            }
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    @Override
    public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
        KafkaConnectFieldWriter writer = new KafkaConnectFieldWriter(getConversionService());
        writer.setSchema(extractSchema(getDocumentMetadata(), getDocumentSpecificationFile()));
        session.setFieldWriter(getDocId(), writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} processPreTargetExcution completed", getDocId());
        }
        
    }

    @Override
    public void readSourceValue(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        KafkaConnectFieldReader reader = session.getFieldReader(getDocId(), KafkaConnectFieldReader.class);
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
                KafkaConnectField targetSubField = AtlasKafkaConnectModelFactory.createKafkaConnectField();
                AtlasKafkaConnectModelFactory.copyField(targetField, targetSubField, false);
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
            KafkaConnectField targetSubField = new KafkaConnectField();
            AtlasKafkaConnectModelFactory.copyField(targetField, targetSubField, false);
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

    @Override
    public void writeTargetValue(AtlasInternalSession session) throws AtlasException {
        KafkaConnectFieldWriter writer = session.getFieldWriter(getDocId(), KafkaConnectFieldWriter.class);
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
        KafkaConnectFieldWriter writer = session.getFieldWriter(getDocId(), KafkaConnectFieldWriter.class);
        if (writer != null && writer.getDocument() != null) {
            Object outputBody = writer.getDocument();
            session.setTargetDocument(getDocId(), outputBody);
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
        return field instanceof KafkaConnectField || field instanceof KafkaConnectEnumField;
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return AtlasKafkaConnectModelFactory.cloneField((KafkaConnectField)field, true);
    }

    @Override
    public Field createField() {
        return AtlasKafkaConnectModelFactory.createKafkaConnectField();
    }

}
