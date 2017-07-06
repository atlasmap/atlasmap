package io.atlasmap.json.v2;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.api.AtlasException;

import java.io.IOException;

/**
 */
public class DocumentJsonFieldReader {

    public DocumentJsonFieldReader() {
    }

    public void read(final String document, final JsonField jsonField) throws AtlasException {
        if (document == null || document.isEmpty()) {
            throw new AtlasException(new IllegalArgumentException("Argument 'document' cannot be null nor empty"));
        }
        if (jsonField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'jsonField' cannot be null"));
        }
        //make this a JSON document
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonParser parser;
        JsonNode rootNode;
        JsonNode valueNode = null;
        try {
            parser = jsonFactory.createParser(document);
            rootNode = objectMapper.readTree(parser);
            String[] nodes = jsonField.getPath().replaceFirst("/", "").split("/");
            int index = 0;
            if (nodes.length == 1) {
                valueNode = rootNode.findValue(nodes[0]);
            } else if (nodes.length > 1) {
                valueNode = rootNode;
                // need to walk the path....
                for (String nodeName : nodes) {
                    //are we looking for an array?
                    if (nodeName.contains("[")) {
                        index = Integer.parseInt(nodeName.substring(nodeName.indexOf("[") + 1, nodeName.indexOf("]")));
                        nodeName = nodeName.substring(0, nodeName.indexOf("["));
                    }
                    if (valueNode.isArray() && index > 0) {
                        valueNode = valueNode.get(index);
                        //reset for possible indexed child nodes
                        index = 0;
                    }
                    if (valueNode != null) {
                        valueNode = valueNode.findValue(nodeName);
                    } else {
                        System.out.println("----> should we log this miss?");
                        valueNode = null;
                        break;
                    }
                }
            }
            if (valueNode != null) {
                if (valueNode.isTextual()) {
                    jsonField.setValue(valueNode.textValue());
                } else if (valueNode.isNumber()) {
                    if (valueNode.isInt()) {
                        jsonField.setValue(valueNode.intValue());
                    } else if (valueNode.isDouble()) {
                        jsonField.setValue(valueNode.doubleValue());
                    } else if (valueNode.isBigDecimal()) {
                        jsonField.setValue(valueNode.decimalValue());
                    } else if (valueNode.isFloat()) {
                        jsonField.setValue(valueNode.floatValue());
                    } else if (valueNode.isLong()) {
                        jsonField.setValue(valueNode.longValue());
                    } else if (valueNode.isShort()) {
                        jsonField.setValue(valueNode.shortValue());
                    } else if (valueNode.isBoolean()) {
                        jsonField.setValue(valueNode.binaryValue());
                    } else if (valueNode.isBigInteger()) {
                        jsonField.setValue(valueNode.bigIntegerValue());
                    } else {
                        jsonField.setValue(valueNode.numberValue());
                    }
                }
            }
        } catch (IOException e) {
            throw new AtlasException(e);
        }
    }
}
