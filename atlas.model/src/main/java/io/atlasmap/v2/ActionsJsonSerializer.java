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
            writeStringField(gen, a.getClass().getSimpleName());
        }
        
        gen.writeEndArray();
    }
    
    protected void writeStringField(JsonGenerator gen, String fieldName) throws IOException {
        gen.writeStartObject();
        gen.writeNullField(fieldName);
        gen.writeEndObject();
        
    }
    
    protected void writeComplexField(JsonGenerator gen, Action action) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("Substring");
        gen.writeStartObject();
        gen.writeNumberField("startIndex", 0);
        gen.writeNumberField("endIndex", 3);
        gen.writeEndObject();
        gen.writeEndObject();
    }
}
