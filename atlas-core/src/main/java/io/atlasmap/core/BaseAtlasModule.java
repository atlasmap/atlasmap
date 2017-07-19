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

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldActionService;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasPropertyStrategy;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.PropertyField;

public abstract class BaseAtlasModule implements AtlasModule {

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

    protected void processFieldActions(AtlasFieldActionService fieldActionService, Field field) throws AtlasException {
        field.setValue(fieldActionService.processActions(field.getActions(), field.getValue()));
    }
}
