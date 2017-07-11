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
package io.atlasmap.v2;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ActionsJsonSerializer extends JsonSerializer<Actions>{

    @Override
    public void serialize(Actions actions, JsonGenerator gen, SerializerProvider provider) throws IOException {
        
        gen.writeStartArray();
        
        if(actions == null || actions.getActions() == null || actions.getActions().isEmpty()) {
            gen.writeEndArray();
            return;
        }
                
        for(Action a : actions.getActions()) {
            writeStringField(gen, a);
        }
        
        gen.writeEndArray();
    }
    
    protected void writeStringField(JsonGenerator gen, Action action) throws IOException {
        
        switch(action.getClass().getSimpleName()) {
        case "SubString": writeSubString(gen, (SubString)action); break;
        case "SubStringAfter": writeSubStringAfter(gen, (SubStringAfter)action); break;
        case "SubStringBefore": writeSubStringBefore(gen, (SubStringBefore)action); break;
        default:
            gen.writeStartObject();
            gen.writeNullField(action.getClass().getSimpleName());
            gen.writeEndObject();
            break;
        }
    }
    
    protected void writeSubString(JsonGenerator gen, SubString subString) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubString");
        gen.writeStartObject();
        gen.writeNumberField("startIndex", subString.getStartIndex());
        gen.writeNumberField("endIndex", subString.getEndIndex());
        gen.writeEndObject();
        gen.writeEndObject();
    }
    
    protected void writeSubStringAfter(JsonGenerator gen, SubStringAfter subStringAfter) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubStringAfter");
        gen.writeStartObject();
        gen.writeStringField("match", subStringAfter.getMatch());
        gen.writeNumberField("startIndex", subStringAfter.getStartIndex());
        gen.writeNumberField("endIndex", subStringAfter.getEndIndex());
        gen.writeEndObject();
        gen.writeEndObject();
    }
    
    protected void writeSubStringBefore(JsonGenerator gen, SubStringBefore subStringBefore) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("SubStringBefore");
        gen.writeStartObject();
        gen.writeStringField("match", subStringBefore.getMatch());
        gen.writeNumberField("startIndex", subStringBefore.getStartIndex());
        gen.writeNumberField("endIndex", subStringBefore.getEndIndex());
        gen.writeEndObject();
        gen.writeEndObject();
    }
}
