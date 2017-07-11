package io.atlasmap.core;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldActionService;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;

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
        
    protected void processFieldActions(AtlasFieldActionService fieldActionService, Field field) throws AtlasException {
        field.setValue(fieldActionService.processActions(field.getActions(), field.getValue()));
    }
}
