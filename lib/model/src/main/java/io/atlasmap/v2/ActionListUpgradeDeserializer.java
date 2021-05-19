/*
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @deprecated This was introduced for backward compatibility. Remove this in v2.0.
 * https://github.com/atlasmap/atlasmap/pull/877
 */
@Deprecated
public class ActionListUpgradeDeserializer extends JsonDeserializer<ArrayList<Action>> {

    private ClassLoader classLoader;

    public ActionListUpgradeDeserializer() {
        this.classLoader = getClass().getClassLoader();
    }

    public ActionListUpgradeDeserializer(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ArrayList<Action> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper()
            .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setTypeFactory(TypeFactory.defaultInstance().withClassLoader(classLoader));
        objectMapper.setHandlerInstantiator(new AtlasHandlerInstantiator(classLoader));
        JsonNode node = (JsonNode) objectMapper.readTree(jp);

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

                    result.add(objectMapper.readerFor(Action.class).readValue(objNode));
                }
            }
        }
        return result;
    }
}
