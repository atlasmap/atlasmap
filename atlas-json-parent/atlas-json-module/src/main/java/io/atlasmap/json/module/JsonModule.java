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

import io.atlasmap.api.AtlasConversionException;
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
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.Validations;
import io.atlasmap.json.core.DocumentJsonFieldReader;
import io.atlasmap.json.core.DocumentJsonFieldWriter;
import io.atlasmap.json.v2.JsonField;
import java.util.ArrayList;
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
    public void processPreInputExecution(AtlasSession session) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPreInputExcution completed");
        }
    }

    @Override
    public void processPreOutputExecution(AtlasSession session) throws AtlasException {
        DocumentJsonFieldWriter writer = new DocumentJsonFieldWriter();
        session.setOutput(writer);
        
        if(logger.isDebugEnabled()) {
            logger.debug("processPreOutputExcution completed");
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
        
        if(!isSupportedField(field)) {
            Audit audit = new Audit();
            audit.setDocId(field.getDocId());
            audit.setPath(field.getPath());
            audit.setStatus(AuditStatus.ERROR);
            audit.setMessage(String.format("Unsupported input field type=%s", field.getClass().getName()));
            session.getAudits().getAudit().add(audit);
            return;
        }
        
        if(field instanceof PropertyField) {
            processPropertyField(session, mapping, session.getAtlasContext().getContextFactory().getPropertyStrategy());
            if(logger.isDebugEnabled()) {
                logger.debug("Processed input propertyField sPath=" + field.getPath() + " sV=" + field.getValue() + " sT=" + field.getFieldType() + " docId: " + field.getDocId());
            }
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
            logger.warn(String.format("FieldType detection was unsuccessful for p=%s falling back to type UNSUPPORTED", inputField.getPath()));
            inputField.setFieldType(FieldType.UNSUPPORTED);
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("Processed input field sPath=" + field.getPath() + " sV=" + field.getValue() + " sT=" + field.getFieldType() + " docId: " + field.getDocId());
        }    
    }
    
    @Override
    public void processInputCollection(AtlasSession session, Collection mapping) throws AtlasException {
       
    }
    
    @Override
    public void processOutputMapping(AtlasSession session, Mapping mapping) throws AtlasException {        
        switch(mapping.getMappingType()) {
        case MAP: processMapOutputMapping(session, mapping); break;
        case COMBINE: break;
        case SEPARATE: break;
        default: logger.warn(String.format("Unsupported mapping type=%s", mapping.getMappingType())); return;
        }
        
        
    }
    
    protected void processMapOutputMapping(AtlasSession session, Mapping mapping) throws AtlasException {
        Field inField = mapping.getInputField().get(0);
        Field outField = mapping.getOutputField().get(0);
        if(!(outField instanceof JsonField)) {
            logger.error(String.format("Unsupported field type %s", outField.getClass().getName()));
            return;
        }
        
        if(inField.getValue() == null) {
            return;
        }
        
        JsonField outputField = (JsonField)outField;
        Object outputValue = null;
        
        if(outputField.getFieldType() == null) {
            outputField.setFieldType(getConversionService().fieldTypeFromClass(inField.getValue().getClass()));
        }
        
        if(inField.getFieldType() != null && inField.getFieldType().equals(outputField.getFieldType())) {
            outputValue = inField.getValue();
        } else {
            try {
                outputValue = getConversionService().convertType(inField.getValue(), inField.getFieldType(), outputField.getFieldType());
            } catch (AtlasConversionException e) {
                logger.error(String.format("Unable to auto-convert for iT=%s oT=%s oF=%s msg=%s", inField.getFieldType(),  outputField.getFieldType(), outputField.getPath(), e.getMessage()), e);
                return;
            }
        }
        
        outputField.setValue(outputValue);        
        
        if(session.getOutput() != null && session.getOutput() instanceof DocumentJsonFieldWriter) {
            DocumentJsonFieldWriter writer = (DocumentJsonFieldWriter) session.getOutput();
            writer.write(outputField);
        } else {
            //TODO: add error handler to detect if the output writer isn't there or is wrong class instance
        }        
        
        if(logger.isDebugEnabled()) {
            logger.debug(String.format("Processed output field oP=%s oV=%s oT=%s docId: %s", outputField.getPath(), outputField.getValue(), outputField.getFieldType(), outputField.getDocId()));
        }
    }
    
    @Override
    public void processOutputCollection(AtlasSession session, Collection mapping) throws AtlasException {
       
    }

    @Override
    public void processPostInputExecution(AtlasSession session) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("processPostInputExecution completed");
        }
    }
    
    @Override
    public void processPostOutputExecution(AtlasSession session) throws AtlasException {
        
        List<String> docIds = new ArrayList<String>();
        for(DataSource ds : session.getMapping().getDataSource()) {
            if(DataSourceType.TARGET.equals(ds.getDataSourceType()) && ds.getUri().startsWith("atlas:json")) {
                docIds.add(ds.getId());
            }
        }
        
//        for(String docId : docIds) {
            //Object output = session.getOutput(docId);
            Object output = session.getOutput();
            if(output instanceof DocumentJsonFieldWriter) {
            	if (((DocumentJsonFieldWriter)output).getRootNode() != null) {
	                String outputBody = ((DocumentJsonFieldWriter)output).getRootNode().toString();
	                session.setOutput(outputBody);
	                if(logger.isDebugEnabled()) {
	                    logger.debug(String.format("processPostOutputExecution converting JsonNode to string size=%s", outputBody.length()));
	                }
            	} else {
            		//TODO: handle error where rootnode on DocumentJsonFieldWriter is set to null (which should never happen).
            	}
            } else {
                logger.error("DocumentJsonFieldWriter object expected for Json output data source");
            }
  //      }
        
        if(logger.isDebugEnabled()) {
            logger.debug("processPostOutputExecution completed");
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
        } else if (field instanceof PropertyField) {
            return true;
        } else if (field instanceof ConstantField) {
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
