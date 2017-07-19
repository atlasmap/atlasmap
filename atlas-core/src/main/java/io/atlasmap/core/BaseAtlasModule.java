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
package io.atlasmap.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldActionService;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasPropertyStrategy;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.PropertyField;

public abstract class BaseAtlasModule implements AtlasModule {

    private static final Logger logger = LoggerFactory.getLogger(BaseAtlasModule.class);
    
    @Override
    public void processInputActions(AtlasSession atlasSession, Mapping mapping) throws AtlasException {
        AtlasFieldActionService fieldActionService = atlasSession.getAtlasContext().getContextFactory().getFieldActionService();
        switch(mapping.getMappingType()) {
            case MAP: for(Field field : mapping.getInputField()) { processFieldActions(fieldActionService, field); }
            case COLLECTION: return; // TODO support field actions on sub-field collections
            case SEPARATE: for(Field field : mapping.getInputField()) { processFieldActions(fieldActionService, field); }
            case COMBINE: for(Field field : mapping.getInputField()) { processFieldActions(fieldActionService, field); }
            case LOOKUP:  for(Field field : mapping.getInputField()) { processFieldActions(fieldActionService, field); }
            default: return; // ALL NONE are not supported here
        }
    }

    @Override
    public void processOutputActions(AtlasSession atlasSession, Mapping mapping) throws AtlasException {
        AtlasFieldActionService fieldActionService = atlasSession.getAtlasContext().getContextFactory().getFieldActionService();
        switch(mapping.getMappingType()) {
            case MAP: for(Field field : mapping.getOutputField()) { processFieldActions(fieldActionService, field); }
            case COLLECTION: return; // TODO support field actions on sub-field collections
            case SEPARATE: for(Field field : mapping.getOutputField()) { processFieldActions(fieldActionService, field); }
            case COMBINE: for(Field field : mapping.getOutputField()) { processFieldActions(fieldActionService, field); }
            case LOOKUP:  for(Field field : mapping.getOutputField()) { processFieldActions(fieldActionService, field); }
            default: return; // ALL NONE are not supported here
        }    
    }
    
    protected void processPropertyField(AtlasSession atlasSession, Mapping mapping, AtlasPropertyStrategy atlasPropertyStrategy) throws AtlasException {
        for(Field f : mapping.getInputField()) {
            if(f instanceof PropertyField) {
                atlasPropertyStrategy.processPropertyField(atlasSession.getMapping(), (PropertyField)f, atlasSession.getProperties());
            }
        }
        
        for(Field f : mapping.getOutputField()) {
            if(f instanceof PropertyField) {
                atlasPropertyStrategy.processPropertyField(atlasSession.getMapping(), (PropertyField)f, atlasSession.getProperties());
            }
        }
    }
    
    protected void processLookupField(AtlasSession session, Mapping mapping) throws AtlasException {
        
        if(mapping == null || mapping.getMappingType() == null || MappingType.LOOKUP.equals(mapping.getMappingType()) || 
                mapping.getLookupTableName() == null || mapping.getLookupTableName().trim().length() == 0) {
            throw new AtlasException("Lookup mapping must have lookupTableName specified");
        }
        
        if(session == null || session.getMapping() == null) {
            throw new AtlasException("AtlasSession must be initialized");
        }
        
        if(session.getMapping().getLookupTables() == null || session.getMapping().getLookupTables().getLookupTable() == null 
                || session.getMapping().getLookupTables().getLookupTable().size() == 0) {
            logger.warn(String.format("No lookup table found for specified lookupTableName=%s", mapping.getLookupTableName()));
            return;
        }

        LookupTable currentTable = null;
        for(LookupTable lookupTable : session.getMapping().getLookupTables().getLookupTable()) {
            if(lookupTable.getName() != null && lookupTable.getName().equals(mapping.getLookupTableName())) {
                currentTable = lookupTable;
            }
        }
        
        if(currentTable.getLookupEntry() == null || currentTable.getLookupEntry().isEmpty()) {
            logger.warn(String.format("Lookup table lookupTableName=%s does not contain any entries", mapping.getLookupTableName()));
            return;
        }
        
        for(LookupEntry entry : currentTable.getLookupEntry()) {
            for(Field inputField : mapping.getInputField()) {
                if(entry.getSourceValue().equals(inputField.getValue())) {
                    inputField.setValue(entry.getTargetValue());
                    if(logger.isDebugEnabled()) {
                        logger.debug(String.format("Processing lookup value for iP=%s iV=%s lksV=%s lksT=%s lktV=%s lktT=%s", inputField.getPath(), inputField.getValue(), entry.getSourceValue(), entry.getSourceType(), entry.getTargetValue(), entry.getTargetType()));
                    }
                }
            }
        }
        
    }

    protected void processFieldActions(AtlasFieldActionService fieldActionService, Field field) throws AtlasException {
        field.setValue(fieldActionService.processActions(field.getActions(), field.getValue()));
    }
}
