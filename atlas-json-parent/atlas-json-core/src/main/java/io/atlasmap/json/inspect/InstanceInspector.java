package io.atlasmap.json.inspect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.json.v2.JsonFields;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 */
public class InstanceInspector implements JsonInspector {


    private static final Logger LOG = LoggerFactory.getLogger(InstanceInspector.class);
    private static InstanceInspector myself = new InstanceInspector();

    private InstanceInspector() {
    }

    public static InstanceInspector instance() {
        return myself;
    }

    public JsonDocument inspect(String instance) throws JsonInspectionException {
        if (instance == null || instance.isEmpty()) {
            throw new IllegalArgumentException("JSON instance cannot be null");
        }
        try {
            JsonDocument jsonDocument = AtlasJsonModelFactory.createJsonDocument();
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode rootNode = objectMapper.readTree(instance);
            if (rootNode.isArray()) {
                // TODO how do we handle a topmost array
                JsonComplexType field = new JsonComplexType();
                field.setJsonFields(new JsonFields());
                field.setStatus(FieldStatus.UNSUPPORTED);
                field.setCollectionType(CollectionType.ARRAY);
                field.setValue(rootNode.toString());
                jsonDocument.getFields().getField().add(field);
            } else if (rootNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> nodes = rootNode.fields();
                while (nodes.hasNext()) {
                    Map.Entry<String, JsonNode> entry = nodes.next();
                    if (entry.getValue().isObject()) {
                        LOG.trace("NODE IS AN OBJECT --> " + entry.getKey() + " WITH ---> " + entry.getValue().size()
                                + " FIELDS");
                        // this is a complex type
                        JsonComplexType parent = getJsonComplexTypeFromEntry(entry);
                        jsonDocument.getFields().getField().add(parent);
                        handleObjectNode(jsonDocument, entry.getValue(), parent, 0);
                    } else if (entry.getValue().isArray()) {
                        // this is a complex type as an ARRAY
                        LOG.trace("NODE IS AN ARRAY --> " + entry.getKey() + " WITH ---> " + entry.getValue().size()
                                + " CHILDREN");
                        JsonComplexType parent = getJsonComplexTypeFromEntry(entry);
                        parent.setCollectionType(CollectionType.ARRAY);
                        jsonDocument.getFields().getField().add(parent);
                        handleArrayNode(jsonDocument, (ArrayNode) entry.getValue(), parent, entry.getKey(), 0);
                    } else if (entry.getValue().isValueNode()) {
                        LOG.trace("NODE IS A VALUE --> " + entry.getKey() + " WITH ---> " + entry.getValue().size()
                                + " CHILDREN");
                        handleValueEntry(jsonDocument, entry, null, 0);
                    }
                }
            }
            return jsonDocument;
        } catch (IOException e) {
            throw new JsonInspectionException(e);
        }
    }

    private void handleArrayNode(JsonDocument jsonDocument, ArrayNode aNode, JsonComplexType parent, String aKey, int index) throws IOException {
        if (aNode.get(0).isObject()) {
            LOG.trace("ARRAY OF OBJECTS WITH PARENT ---> " + parent.getName().concat(String.valueOf(index))
                    + " WITH KEY ----> " + aKey + " AND SIZE OF ---> " + aNode.size());
            int childIndex = 0;
            JsonComplexType childObject = null;
            if (!aKey.equals(parent.getName())) {
                childObject = getJsonComplexType(parent, aKey, index);
                childObject.setCollectionType(CollectionType.LIST);
            }
            for (JsonNode jsonNode : aNode) {
                if (childObject != null) {
                    // rest for child fields...
                    handleObjectNode(jsonDocument, jsonNode, childObject, childIndex);
                } else {
                    handleObjectNode(jsonDocument, jsonNode, parent, index);
                }
                childIndex++;
                index++;
            }
        } else if (aNode.get(0).isArray()) {
            LOG.trace("**TODO** > HANDLE ARRAY OF AN ARRAY WITH PARENT ---> " + parent.getName() + " WITH KEY ----> "
                    + aKey);
        } else if (aNode.get(0).isValueNode()) {
            LOG.trace("**TODO** > HANDLE ARRAY OF A VALUES WITH PARENT ---> " + parent.getName() + " WITH KEY ----> "
                    + aKey);
        }
    }

    private void handleObjectNode(JsonDocument jsonDocument, JsonNode jsonNode, JsonComplexType parent, int index) throws IOException {
        LOG.trace("HANDLING AN OBJECT NODE " + jsonNode.fields().next().getKey() + " WITH PARENT ---> "
                + parent.getName() + " WITH INDEX OF " + index);
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();
            String key = next.getKey();
            JsonNode node = next.getValue();
            if (node.isValueNode()) {
                handleValueEntry(jsonDocument, next, parent, index);
            } else if (node.isObject()) {
                LOG.trace("FOUND AN OBJECT NODE THAT IS A CONTAINER WITH KEY --> " + key + " WITH A PARENT INDEX OF "
                        + index);
                JsonComplexType container = getJsonComplexType(parent, key, index);
                // rest index to zero when dealing with containers (we don't need an index on
                // containers)
                handleObjectNode(jsonDocument, next.getValue(), container, 0);
            } else if (node.isArray()) {
                if (node.get(0).isObject()) {
                    ArrayNode arrayNode = (ArrayNode) node;
                    // index for children
                    int innerIndex = 0;
                    JsonComplexType deeperChild = getJsonComplexType(parent, key, index);
                    if (parent.getCollectionType() == null) {
                        deeperChild.setCollectionType(CollectionType.LIST);
                    } else {
                        deeperChild.setCollectionType(CollectionType.ARRAY);
                    }
                    for (JsonNode deeperJsonNode : arrayNode) {
                        handleObjectNode(jsonDocument, deeperJsonNode, deeperChild, innerIndex);
                        innerIndex++;
                    }
                }
            }
        }
    }

    private void handleValueEntry(JsonDocument jsonDocument, Map.Entry<String, JsonNode> jsonNodeEntry, JsonComplexType parent, int index) {
        JsonNode theNode = jsonNodeEntry.getValue();
        String nodeKey = jsonNodeEntry.getKey();
        JsonField field = AtlasJsonModelFactory.createJsonField();
        if (nodeKey != null) {
            field.setName(nodeKey);
            if (parent != null) {
                LOG.trace("HANDLING AN VALUE NODE WITH PARENT ---> " + parent.getName() + " WITH INDEX OF " + index);

                if (index > 0 && (parent.getCollectionType() != null
                        && parent.getCollectionType().compareTo(CollectionType.ARRAY) == 0)) {
                    field.setPath(parent.getPath().concat("/").concat(nodeKey).concat("[").concat(String.valueOf(index))
                            .concat("]"));
                } else if (index > 0 && (parent.getCollectionType() != null
                        && parent.getCollectionType().compareTo(CollectionType.LIST) == 0)) {
                    field.setPath(
                            parent.getPath().concat("[").concat(String.valueOf(index)).concat("]/").concat(nodeKey));
                } else {
                    field.setPath(parent.getPath().concat("/").concat(nodeKey));
                }
            } else {
                LOG.trace("HANDLING AN VALUE NODE WITH NO PARENT WITH INDEX OF " + index);
                field.setPath("/".concat(nodeKey));
            }
        }
        setNodeValueOnField(theNode, field);
        if (parent == null) {
            jsonDocument.getFields().getField().add(field);
        } else {
            parent.getJsonFields().getJsonField().add(field);
        }

    }

    private void setNodeValueOnField(JsonNode valueNode, JsonField field) {
        LOG.trace("VALUE IS A " + valueNode.getNodeType().name());
        if (valueNode.isNumber()) {
            if (valueNode.isInt()) {
                field.setFieldType(FieldType.INTEGER);
                field.setStatus(FieldStatus.SUPPORTED);
                field.setValue(valueNode.intValue());
            } else if (valueNode.isBigInteger()) {
                field.setFieldType(FieldType.INTEGER);
                field.setStatus(FieldStatus.SUPPORTED);
                field.setValue(valueNode.bigIntegerValue());
            } else if (valueNode.isFloat()) {
                field.setFieldType(FieldType.FLOAT);
                field.setStatus(FieldStatus.SUPPORTED);
                field.setValue(valueNode.floatValue());
            } else if (valueNode.isDouble()) {
                field.setFieldType(FieldType.DOUBLE);
                field.setStatus(FieldStatus.SUPPORTED);
                field.setValue(valueNode.asDouble());
            } else if (valueNode.isBigDecimal()) {
                field.setFieldType(FieldType.DECIMAL);
                field.setStatus(FieldStatus.SUPPORTED);
                field.setValue(valueNode.decimalValue());
            } else if (valueNode.isShort()) {
                field.setFieldType(FieldType.SHORT);
                field.setStatus(FieldStatus.SUPPORTED);
                field.setValue(valueNode.shortValue());
            } else if (valueNode.isLong()) {
                field.setFieldType(FieldType.LONG);
                field.setStatus(FieldStatus.SUPPORTED);
                field.setValue(valueNode.longValue());
            }
        } else if (valueNode.isTextual()) {
            field.setFieldType(FieldType.STRING);
            field.setStatus(FieldStatus.SUPPORTED);
            field.setValue(valueNode.textValue());
        } else if (valueNode.isBoolean()) {
            field.setFieldType(FieldType.BOOLEAN);
            field.setStatus(FieldStatus.SUPPORTED);
            field.setValue(valueNode.booleanValue());
        } else if (valueNode.isBinary() || valueNode.isPojo()) {
            field.setFieldType(FieldType.UNSUPPORTED);
            field.setStatus(FieldStatus.UNSUPPORTED);
        }
    }

    private JsonComplexType getJsonComplexType(JsonComplexType parent, String aKey, int index) {
        JsonComplexType jsonComplexType = new JsonComplexType();
        jsonComplexType.setJsonFields(new JsonFields());
        jsonComplexType.setName(aKey);
        jsonComplexType.setFieldType(FieldType.COMPLEX);
        jsonComplexType.setStatus(FieldStatus.SUPPORTED);
        if (index > 0) {
            jsonComplexType
                    .setPath(parent.getPath().concat("[").concat(String.valueOf(index)).concat("]/").concat(aKey));
        } else {
            jsonComplexType.setPath(parent.getPath().concat("/").concat(aKey));
        }
        parent.getJsonFields().getJsonField().add(jsonComplexType);
        return jsonComplexType;
    }

    private JsonComplexType getJsonComplexTypeFromEntry(Map.Entry<String, JsonNode> entry) {
        JsonComplexType parent = new JsonComplexType();
        parent.setJsonFields(new JsonFields());
        parent.setFieldType(FieldType.COMPLEX);
        parent.setName(entry.getKey());
        parent.setStatus(FieldStatus.SUPPORTED);
        parent.setPath("/".concat(entry.getKey()));
        return parent;
    }
}
