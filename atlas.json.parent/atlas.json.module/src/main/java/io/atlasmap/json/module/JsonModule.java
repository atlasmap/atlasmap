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
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.core.DefaultAtlasMappingValidator;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Validations;
import io.atlasmap.json.v2.JsonField;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AtlasModuleDetail(name = "JsonModule", uri = "atlas:json", modes = { "SOURCE", "TARGET" }, dataFormats = { "json" }, configPackages = { "io.atlasmap.json.v2" })
public class JsonModule extends BaseAtlasModule {
    private static final Logger logger = LoggerFactory.getLogger(JsonModule.class);
    
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
        return null;
    }

    @Override
    public void setMode(AtlasModuleMode atlasModuleMode) {
        
    }

    @Override
    public List<AtlasModuleMode> listSupportedModes() {
        return null;
    }

    @Override
    public Boolean isStatisticsSupported() {
        return null;
    }

    @Override
    public Boolean isStatisticsEnabled() {
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setConversionService(AtlasConversionService arg0) {
        // TODO Auto-generated method stub
        
    }
}
