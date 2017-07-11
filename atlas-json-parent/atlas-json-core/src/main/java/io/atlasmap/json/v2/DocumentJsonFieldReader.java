package io.atlasmap.json.v2;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.FieldType;

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
                    jsonField.setFieldType(FieldType.STRING);
                } else if (valueNode.isNumber()) {
                    if (valueNode.isInt()) {
                        jsonField.setValue(valueNode.intValue());
                        jsonField.setFieldType(FieldType.INTEGER);
                    } else if (valueNode.isDouble()) {
                        jsonField.setValue(valueNode.doubleValue());
                        jsonField.setFieldType(FieldType.DOUBLE);
                    } else if (valueNode.isBigDecimal()) {
                        jsonField.setValue(valueNode.decimalValue());
                        jsonField.setFieldType(FieldType.DECIMAL);
                    } else if (valueNode.isFloat()) {
                        jsonField.setValue(valueNode.floatValue());
                        jsonField.setFieldType(FieldType.DOUBLE);
                    } else if (valueNode.isLong()) {
                        jsonField.setValue(valueNode.longValue());
                        jsonField.setFieldType(FieldType.LONG);
                    } else if (valueNode.isShort()) {
                        jsonField.setValue(valueNode.shortValue());
                        jsonField.setFieldType(FieldType.SHORT);
                    } else if (valueNode.isBigInteger()) {
                        jsonField.setValue(valueNode.bigIntegerValue());
                        jsonField.setFieldType(FieldType.NUMBER);
                    } else {
                        jsonField.setValue(valueNode.numberValue());
                        jsonField.setFieldType(FieldType.NUMBER);
                    }
                } else if (valueNode.isBoolean()) {
                    jsonField.setValue(valueNode.booleanValue());
                    jsonField.setFieldType(FieldType.BOOLEAN);
                }
            }
        } catch (IOException e) {
            throw new AtlasException(e);
        }
    }
}
