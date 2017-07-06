package io.atlasmap.v2;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class ActionsJsonDeserializer extends JsonDeserializer<Actions> {
    
    @Override
    public Actions deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Actions actions = null;
        if(jp != null && jp.isExpectedStartArrayToken()) {
            actions = new Actions();
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                JsonToken jsonToken = jp.nextToken();
                
                if(jsonToken == JsonToken.END_ARRAY) {
                    break;
                }
                
                Action action = processActionJsonToken(jp);
                if(action != null) {
                    actions.getActions().add(action);
                }
            }
        } else {
            throw new IOException("Invalid JSON where array expected: " + jp.getCurrentToken().asString());
        }
        
        return actions;
    }
    
    protected Action processActionJsonToken(JsonParser jsonToken) throws IOException {
        
        Action action = null;
        if(jsonToken.getCurrentName() == null) {
            return null;
        }

        switch(jsonToken.getCurrentName()) {
        case "Camelize": action = new Camelize(); return action;
        case "Capitalize": action = new Capitalize(); return action;
        case "Lowercase": action = new Lowercase(); return action;
        case "SeparateByDash": action = new SeparateByDash(); return action;
        case "SeparateByUnderscore": action = new SeparateByUnderscore(); return action;
        case "StringLength": action = new StringLength(); return action;
        case "Trim": action = new Trim(); return action;
        case "TrimLeft": action = new TrimLeft(); return action;
        case "TrimRight": action = new TrimRight(); return action;
        case "Uppercase": action = new Uppercase(); return action;
        default: //logger.warn("Unsupported action named: " + jsonToken.getCurrentName());
            // System.out.println("n=" + jsonToken.getCurrentName() + "f=" + jsonToken.getText() + " v=" + jsonToken.getValueAsString());
        }
        
        return null;
    }
}
