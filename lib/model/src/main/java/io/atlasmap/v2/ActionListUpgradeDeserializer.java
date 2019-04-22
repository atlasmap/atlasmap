package io.atlasmap.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @deprecated This was introduced for backward compatibility. Remove this in v2.0.
 * https://github.com/atlasmap/atlasmap/pull/877
 */
@Deprecated
public class ActionListUpgradeDeserializer extends JsonDeserializer<ArrayList<Action>> {

    @Override
    public ArrayList<Action> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = (JsonNode) mapper.readTree(jp);

        ArrayList<Action> result = new ArrayList<Action>();
        if (node != null && node instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) node;
            Iterator nodeIterator = arrayNode.iterator();
            while (nodeIterator.hasNext()) {

                JsonNode elementNode = (JsonNode) nodeIterator.next();
                if (elementNode.isObject()) {

                    ObjectNode objNode = (ObjectNode) elementNode;
                    JsonNode type = objNode.get("@type");
                    if (type == null) {

                        // Lets convert to the new format..
                        Map.Entry<String, JsonNode> next = objNode.fields().next();
                        JsonNode original = next.getValue();

                        objNode = JsonNodeFactory.instance.objectNode();
                        objNode.set("@type", JsonNodeFactory.instance.textNode(next.getKey()));
                        if (original.isObject()) {
                            Iterator<Map.Entry<String, JsonNode>> f = original.fields();
                            while (f.hasNext()) {
                                Map.Entry<String, JsonNode> o = f.next();
                                objNode.set(o.getKey(), o.getValue());
                            }
                        }
                    }

                    result.add(mapper.readerFor(Action.class).readValue(objNode));
                }
            }
        }
        return result;
    }
}
