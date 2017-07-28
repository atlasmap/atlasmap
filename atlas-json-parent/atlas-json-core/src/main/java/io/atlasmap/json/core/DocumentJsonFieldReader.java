package io.atlasmap.json.core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.api.AtlasException;
import io.atlasmap.core.PathUtil;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentJsonFieldReader {

    private static final Logger logger = LoggerFactory.getLogger(DocumentJsonFieldReader.class);
    
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
                    //maybe a list?
                    if (nodeName.contains("<")) {
                        index = Integer.parseInt(nodeName.substring(nodeName.indexOf("<") + 1, nodeName.indexOf(">")));
                        nodeName = nodeName.substring(0, nodeName.indexOf("<"));
                    }
                    if (valueNode != null && valueNode.isArray() && index > 0) {
                        valueNode = valueNode.get(index);
                        //reset for possible indexed child nodes
                        index = 0;
                    }
                    if (valueNode != null) {
                        valueNode = valueNode.findValue(nodeName);
                    } else {
                        // TODO: JsonReader System.out.println("----> should we log this miss?");
                        valueNode = null;
                        break;
                    }
                }
            }
            if (valueNode != null) {
                if (valueNode.isTextual()) {
                    if(jsonField.getFieldType() == null || FieldType.STRING.equals(jsonField.getFieldType())) {
                        jsonField.setValue(valueNode.textValue());
                        jsonField.setFieldType(FieldType.STRING);
                    } else {
                        if(FieldType.CHAR.equals(jsonField.getFieldType())) {
                            jsonField.setValue(valueNode.textValue().charAt(0));
                        } else {
                            logger.warn(String.format("Unsupported FieldType for text data t=%s p=%s docId=%s", jsonField.getFieldType().value(), jsonField.getPath(), jsonField.getDocId()));
                        }
                    }
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
                } else if(valueNode.isContainerNode()) {
                    if(valueNode.isArray()) {
                        if(logger.isDebugEnabled()) {
                            logger.debug(String.format("Detected json array p=%s docId=%s", jsonField.getPath(), jsonField.getDocId()));
                        }
                        jsonField.setValue(valueNode.toString());
                        jsonField.setFieldType(FieldType.COMPLEX);
                        jsonField.setCollectionType(CollectionType.ARRAY);
                    } else if(valueNode.isObject()) {
                        if(logger.isDebugEnabled()) {
                            logger.debug(String.format("Detected json complex object p=%s docId=%s", jsonField.getPath(), jsonField.getDocId()));
                        }
                        jsonField.setValue(valueNode.toString());
                        jsonField.setFieldType(FieldType.COMPLEX);
                    }
                } else {
                    logger.warn(String.format("Detected unsupported json type for field p=%s docId=%s", jsonField.getPath(), jsonField.getDocId()));
                    jsonField.setValue(valueNode.toString());
                    jsonField.setFieldType(FieldType.UNSUPPORTED);
                }
            }
        } catch (IOException e) {
            throw new AtlasException(e);
        }
    }
    
    public Integer getCollectionCount(final String document, final JsonField jsonField, final String collectionSegment) throws AtlasException {
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
        try {
            parser = jsonFactory.createParser(document);
            rootNode = objectMapper.readTree(parser);            
            JsonNode collectionNode = rootNode.findValue(collectionSegment);
            if(collectionNode != null && collectionNode.isArray()) {
                return collectionNode.size();
            }
            return null;
        } catch (IOException e) {
            throw new AtlasException(e.getMessage(), e);
        }
    }
}
