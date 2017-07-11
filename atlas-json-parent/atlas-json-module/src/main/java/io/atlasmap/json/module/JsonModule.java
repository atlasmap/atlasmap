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

import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.core.DefaultAtlasMappingValidator;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Validations;
import io.atlasmap.json.v2.DocumentJsonFieldReader;
import io.atlasmap.json.v2.JsonField;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AtlasModuleDetail(name = "JsonModule", uri = "atlas:json", modes = { "SOURCE", "TARGET" }, dataFormats = { "json" }, configPackages = { "io.atlasmap.json.v2" })
public class JsonModule extends BaseAtlasModule {
    private static final Logger logger = LoggerFactory.getLogger(JsonModule.class);
    private AtlasConversionService atlasConversionService = null;
    private AtlasModuleMode atlasModuleMode = AtlasModuleMode.UNSET;
    
    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void processPreExecution(AtlasSession arg0) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPreExcution completed");
        }
    }

    @Override
    public void processPreValidation(AtlasSession atlasSession) throws AtlasException {
        
        if(atlasSession == null || atlasSession.getMapping() == null) {
            logger.error("Invalid session: Session and AtlasMapping must be specified");
            throw new AtlasValidationException("Invalid session");
        }
        
        DefaultAtlasMappingValidator defaultValidator = new DefaultAtlasMappingValidator(atlasSession.getMapping());
        Validations validations = defaultValidator.validateAtlasMappingFile();
        
        if(logger.isDebugEnabled()) {
            logger.debug("Detected " + validations.getValidation().size() + " core validation notices");
        }
        
        JsonMappingValidator jsonValidator = new JsonMappingValidator(atlasSession.getMapping(), validations);
        validations = jsonValidator.validateAtlasMappingFile();
        
        if(logger.isDebugEnabled()) {
            logger.debug("Detected " + validations.getValidation().size() + " json validation notices");
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("processPreValidation completed");
        }
    }
    
    @Override
    public void processInputMapping(AtlasSession session, Mapping mapping) throws AtlasException {
        if(mapping.getInputField() == null || mapping.getInputField().isEmpty() || mapping.getInputField().size() != 1) {
            Audit audit = new Audit();
            audit.setStatus(AuditStatus.WARN);
            audit.setMessage(String.format("Mapping does not contain exactly one input field alias=%s desc=%s", mapping.getAlias(), mapping.getDescription()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        Field field = mapping.getInputField().get(0);
        
        if(!(field instanceof JsonField)) {
            Audit audit = new Audit();
            audit.setDocId(field.getDocId());
            audit.setPath(field.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Unsupported input field type=%s", field.getClass().getName()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        JsonField inputField = (JsonField)field;
        
        Object sourceObject = null;
        if(field.getDocId() != null) {
            sourceObject = session.getInput(field.getDocId());
        } else {
            sourceObject = session.getInput();
        }
        
        if(session.getInput() == null || !(session.getInput() instanceof String)) {
            Audit audit = new Audit();
            audit.setDocId(field.getDocId());
            audit.setPath(field.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Unsupported input object type=%s", field.getClass().getName()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        String document = (String)sourceObject;
                
        Map<String,String> sourceUriParams = AtlasUtil.getUriParameters(session.getMapping().getDataSource().get(0).getUri());
                          
        DocumentJsonFieldReader djfr = new DocumentJsonFieldReader();

        djfr.read(document, inputField);

        // NOTE: This shouldn't happen
        if(inputField.getFieldType() == null) {
            logger.warn("FieldType detection was unsuccessful, falling back to type STRING");
            inputField.setFieldType(FieldType.STRING);
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("Processed p=" + inputField.getPath() + " t=" + inputField.getFieldType().value() + " v=" + inputField.getValue());
        }       
    }
    
    @Override
    public void processInputCollection(AtlasSession session, Collection mapping) throws AtlasException {
       
    }
    
    @Override
    public void processOutputMapping(AtlasSession session, Mapping mapping) throws AtlasException {
       
    }
    
    @Override
    public void processOutputCollection(AtlasSession session, Collection mapping) throws AtlasException {
       
    }

    @Override
    public void processPostExecution(AtlasSession arg0) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostExecution completed");
        }
    }

    @Override
    public void processPostValidation(AtlasSession arg0) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostValidation completed");
        }
    }
    
    @Override
    public AtlasModuleMode getMode() {
        return this.atlasModuleMode;
    }

    @Override
    public void setMode(AtlasModuleMode atlasModuleMode) {
        this.atlasModuleMode = atlasModuleMode;
    }

    @Override
    public List<AtlasModuleMode> listSupportedModes() {
        return null;
    }

    @Override
    public Boolean isStatisticsSupported() {
        return false;
    }

    @Override
    public Boolean isStatisticsEnabled() {
        return false;
    }

    @Override
    public Boolean isSupportedField(Field field) {
        if (field instanceof JsonField) {
            return true;
        }
        return false;
    }

    @Override
    public AtlasConversionService getConversionService() {
        return this.atlasConversionService;
    }

    @Override
    public void setConversionService(AtlasConversionService atlasConversionService) {
        this.atlasConversionService = atlasConversionService;
    }
}
