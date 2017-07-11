package io.atlasmap.json.v2;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 */
public class DocumentJsonFieldWriter {
    //    public static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DocumentJsonFieldWriter.class);

    private ObjectMapper m = new ObjectMapper();

    public DocumentJsonFieldWriter() {
    }


    public JsonNode write(JsonField jsonField) throws AtlasException {
        if (jsonField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'jsonField' cannot be null"));
        }
        m.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
        ObjectNode rootNode = m.createObjectNode();
        if (jsonField.getFieldType() != null && jsonField.getFieldType().compareTo(FieldType.COMPLEX) == 0) {
            createObjectFromComplexType(jsonField, rootNode);
        } else {
            String[] pathTokens = jsonField.getPath().replaceFirst("/", "").split("/");
            if (pathTokens.length == 1) {
                putFieldValue(jsonField, rootNode);
            } else {
                ObjectNode parentNode = rootNode.putObject(pathTokens[0]);
                putFieldValue(jsonField, parentNode);
            }
        }
        return rootNode;
    }

    public void write(JsonField jsonField, JsonNode root) throws AtlasException {
        if (jsonField == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'jsonField' cannot be null"));
        }
        if (root == null) {
            throw new AtlasException(new IllegalArgumentException("Argument 'root' cannot be null"));
        }
        String[] pathTokens = jsonField.getPath().replaceFirst("/", "").split("/");
        if (pathTokens.length > 1) {
            int index = 0;

            String grandParent = null;
            if (pathTokens.length >= 4) {
                grandParent = pathTokens[pathTokens.length - 3];
            }
            String parent = pathTokens[pathTokens.length - 2];
            String arrayParent = pathTokens[pathTokens.length - 1];

            if(grandParent != null && grandParent.contains("[")){
                index = findIndex(grandParent);
                parent = grandParent.substring(0, grandParent.indexOf("["));
            }else if (parent.contains("[")) {
                index = findIndex(parent);
                parent = parent.substring(0, parent.indexOf("["));
            } else if (!parent.contains("[") && arrayParent.contains("[")) {
                index = findIndex(arrayParent);
            }
            JsonNode rootNode = root.get(parent);
            if (rootNode == null) {
                rootNode = root.findParent(parent);
                rootNode = rootNode.get(parent);
            }

            if (rootNode.getNodeType().compareTo(JsonNodeType.ARRAY) == 0) {
                JsonNode node = rootNode.get(index);
                if (node == null) {
                    node = m.createObjectNode();
                    ((ArrayNode) rootNode).add(node);
                }
                if (jsonField.getFieldType().compareTo(FieldType.COMPLEX) == 0) {
                    createObjectFromComplexType(jsonField, node);
                } else {
                    putFieldValue(jsonField, node);
                }
            } else {
                if (jsonField.getFieldType().compareTo(FieldType.COMPLEX) == 0) {
                    createObjectFromComplexType(jsonField, rootNode);
                } else {
                    putFieldValue(jsonField, rootNode);
                }
            }
        } else {
            if (jsonField.getFieldType() != null && jsonField.getFieldType().compareTo(FieldType.COMPLEX) == 0) {
                createObjectFromComplexType(jsonField, root);
            } else {
                putFieldValue(jsonField, root);
            }
        }
    }

    private void createObjectFromComplexType(JsonField jsonField, JsonNode root) {
        if (jsonField.getFieldType() != null && jsonField.getFieldType().compareTo(FieldType.COMPLEX) == 0) {
            logger.trace("handle this complex field " + jsonField.getName() + " with a root of " + root.toString());
        } else {
            logger.trace("handle this value field " + jsonField.getName() + " with a root of " + root.toString());
        }
        String[] pathTokens = jsonField.getPath().replaceFirst("/", "").split("/");
        JsonNode parentNode = null;
        if (pathTokens.length > 1) {
            if (jsonField.getCollectionType() != null && jsonField.getCollectionType().compareTo(CollectionType.ARRAY) == 0) {
                parentNode = root.get(pathTokens[pathTokens.length - 1]);
            } else {
                parentNode = root.get(pathTokens[pathTokens.length - 2]);
            }
        }

        if (jsonField.getCollectionType() != null &&
            (jsonField.getCollectionType().compareTo(CollectionType.LIST) == 0 ||
                jsonField.getCollectionType().compareTo(CollectionType.ARRAY) == 0)) {
            JsonNode anode = root.path(jsonField.getName());
            ArrayNode arrayNode;
            if (anode != null && anode.getNodeType().compareTo(JsonNodeType.MISSING) != 0) {
                arrayNode = (ArrayNode) anode;
            } else {
                arrayNode = m.createArrayNode();
            }
            if (jsonField.getFieldType().compareTo(FieldType.COMPLEX) == 0) {
                ObjectNode objectNode = m.createObjectNode();
                arrayNode.add(objectNode);
                if (parentNode != null) {
                    ((ObjectNode) parentNode).set(jsonField.getName(), arrayNode);
                } else {
                    ((ObjectNode) root).set(jsonField.getName(), arrayNode);
                }
                if (((JsonComplexType) jsonField).getJsonFields() != null) {
                    int index = 0;
                    for (JsonField field : ((JsonComplexType) jsonField).getJsonFields().getJsonField()) {
                        //ok we need an index.....
                        String[] fieldPathTokens = field.getPath().replaceFirst("/", "").split("/");
                        int currentIndex = findIndex(fieldPathTokens);
                        if (currentIndex != index) {
                            index = currentIndex;
                            objectNode = m.createObjectNode();
                            arrayNode.add(objectNode);
                        }
                        objectNode.putObject(field.getName());
                        if (field.getFieldType().compareTo(FieldType.COMPLEX) != 0) {
                            putFieldValue(field, objectNode);
                        } else {
                            createObjectFromComplexType(field, objectNode);
                        }
                    }
                }
            }
        } else {
            parentNode = ((ObjectNode) root).putObject(jsonField.getName());
            if (jsonField.getFieldType().compareTo(FieldType.COMPLEX) == 0) {
                handleComplexTypeChildren((JsonComplexType) jsonField, parentNode);
            }
        }
    }

    private void handleComplexTypeChildren(JsonComplexType jsonField, JsonNode parentNode) {
        if (jsonField.getJsonFields() != null && !jsonField.getJsonFields().getJsonField().isEmpty()) {
            for (JsonField field : jsonField.getJsonFields().getJsonField()) {
                if (field instanceof JsonComplexType) {
                    createObjectFromComplexType(field, parentNode);
                } else {
                    putFieldValue(field, parentNode);
                }
            }
        }
    }

    private void putFieldValue(JsonField jsonField, JsonNode rootAsObject) {
        if (rootAsObject.getNodeType().compareTo(JsonNodeType.OBJECT) == 0) {
            if (jsonField.getFieldType().compareTo(FieldType.STRING) == 0) {
                ((ObjectNode) rootAsObject).put(jsonField.getName(), (String) jsonField.getValue());
            } else if (jsonField.getFieldType().compareTo(FieldType.INTEGER) == 0) {
                ((ObjectNode) rootAsObject).put(jsonField.getName(), (Integer) jsonField.getValue());
            } else if (jsonField.getFieldType().compareTo(FieldType.BOOLEAN) == 0) {
                ((ObjectNode) rootAsObject).put(jsonField.getName(), (Boolean) jsonField.getValue());
            } else if (jsonField.getFieldType().compareTo(FieldType.CHAR) == 0) {
                ((ObjectNode) rootAsObject).put(jsonField.getName(), (String) jsonField.getValue());
            } else if (jsonField.getFieldType().compareTo(FieldType.DOUBLE) == 0 || jsonField.getFieldType().compareTo(FieldType.FLOAT) == 0) {
                ((ObjectNode) rootAsObject).put(jsonField.getName(), new BigDecimal(String.valueOf(jsonField.getValue())));
            } else if (jsonField.getFieldType().compareTo(FieldType.SHORT) == 0) {
                ((ObjectNode) rootAsObject).put(jsonField.getName(), Short.valueOf(String.valueOf(jsonField.getValue())));
            } else if (jsonField.getFieldType().compareTo(FieldType.LONG) == 0) {
                ((ObjectNode) rootAsObject).put(jsonField.getName(), Long.valueOf(String.valueOf(jsonField.getValue())));
            } else {
                logger.trace("Cannot set value for " + jsonField.getName() + " --> " + jsonField.getValue() + " for field type " + jsonField.getFieldType().name());
            }
        }
    }

    private int findIndex(String[] pathTokens) {
        int index = 0;
        for (String token : pathTokens) {
            if (token.contains("[")) {
                index = findIndex(token);
            }
        }
        return index;
    }

    private int findIndex(String token) {
        int index = 0;
        if (token.contains("[")) {
            index = Integer.parseInt(token.substring(token.indexOf("[") + 1, token.indexOf("]")));
        }
        return index;
    }

}
