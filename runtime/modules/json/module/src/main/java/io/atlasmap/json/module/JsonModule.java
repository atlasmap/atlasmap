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
package io.atlasmap.json.module;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.json.core.JsonFieldReader;
import io.atlasmap.json.core.JsonFieldWriter;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.LookupTable;
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
        JsonValidationService jsonValidationService = new JsonValidationService(getConversionService());
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
        if (sourceDocument == null || !(sourceDocument instanceof String)) {
            throw new AtlasException(String.format("Incompatible Source Document '%s'", sourceDocument));
        }

        String document = (String) sourceDocument;
        JsonFieldReader fieldReader = new JsonFieldReader();
        fieldReader.setDocument(document);
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
    public void processSourceFieldMapping(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        JsonFieldReader reader = session.getFieldReader(getDocId(), JsonFieldReader.class);
        reader.read(session);

        if (sourceField.getActions() != null && sourceField.getActions().getActions() != null) {
            getFieldActionService().processActions(sourceField.getActions(), sourceField);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processSourceFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}]",
                    getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(), sourceField.getValue());
        }
    }

    @Override
    public void processTargetFieldMapping(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();
        // Attempt to Auto-detect field type based on input value
        if (targetField.getFieldType() == null && sourceField.getValue() != null) {
            targetField.setFieldType(getConversionService().fieldTypeFromClass(sourceField.getValue().getClass()));
        }

        Object targetValue = null;

        // Do auto-conversion
        if (sourceField.getFieldType() != null && sourceField.getFieldType().equals(targetField.getFieldType())) {
            targetValue = sourceField.getValue();
        } else if (sourceField.getValue() != null) {
            try {
                targetValue = getConversionService().convertType(sourceField.getValue(), sourceField.getFieldType(),
                        targetField.getFieldType());
            } catch (AtlasConversionException e) {
                AtlasUtil.addAudit(session, targetField.getDocId(),
                        String.format("Unable to auto-convert for sT=%s tT=%s tF=%s msg=%s",
                                sourceField.getFieldType(), targetField.getFieldType(), targetField.getPath(),
                                e.getMessage()), targetField.getPath(), AuditStatus.ERROR, null);
                return;
            }
        }

        targetField.setValue(targetValue);
        LookupTable lookupTable = session.head().getLookupTable();
        if (lookupTable != null) {
            processLookupField(session, lookupTable, targetField.getValue(), targetField);
        }

        if (isAutomaticallyProcessOutputFieldActions() && targetField.getActions() != null && targetField.getActions().getActions() != null) {
            getFieldActionService().processActions(targetField.getActions(), targetField);
        }

        JsonFieldWriter writer = session.getFieldWriter(getDocId(), JsonFieldWriter.class);
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
            AtlasUtil.addAudit(session, getDocId(),
                    String.format("No target document created for DataSource:[id=%s, uri=%s]",
                            getDocId(), this.getUri()), null, AuditStatus.WARN, null);
        }
        session.removeFieldWriter(getDocId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPostTargetExecution completed", getDocId());
        }
    }

    @Override
    public List<AtlasModuleMode> listSupportedModes() {
        return Arrays.asList(AtlasModuleMode.SOURCE, AtlasModuleMode.TARGET);
    }

    @Override
    public Boolean isSupportedField(Field field) {
        if (super.isSupportedField(field)) {
            return true;
        }
        return field instanceof JsonField;
    }

    @Override
    public int getCollectionSize(AtlasInternalSession session, Field field) throws AtlasException {
        // TODO could this use FieldReader?
        Object document = session.getSourceDocument(getDocId());
        // make this a JSON document
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonParser parser = jsonFactory.createParser(document.toString());
            JsonNode rootNode = objectMapper.readTree(parser);
            ObjectNode parentNode = (ObjectNode) rootNode;
            String parentSegment = "[root node]";
            for (SegmentContext sc : new AtlasPath(field.getPath()).getSegmentContexts(false)) {
                JsonNode currentNode = JsonFieldWriter.getChildNode(parentNode, parentSegment, sc.getSegment());
                if (currentNode == null) {
                    return 0;
                }
                if (AtlasPath.isCollectionSegment(sc.getSegment())) {
                    if (currentNode != null && currentNode.isArray()) {
                        return currentNode.size();
                    }
                    return 0;
                }
                parentNode = (ObjectNode) currentNode;
            }
        } catch (IOException e) {
            throw new AtlasException(e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return AtlasJsonModelFactory.cloneField(field);
    }
}
