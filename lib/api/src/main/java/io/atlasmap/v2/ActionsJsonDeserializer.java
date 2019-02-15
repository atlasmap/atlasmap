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
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ActionsJsonDeserializer extends JsonDeserializer<Actions> {

    @Override
    public Actions deserialize(JsonParser jp, DeserializationContext context) throws IOException {
        Actions actions = null;
        if (jp != null && jp.isExpectedStartArrayToken()) {
            actions = new Actions();
            jp.nextToken();
            while (jp.nextToken() != JsonToken.END_ARRAY) {
                JsonToken jsonToken = jp.nextToken();

                if (jsonToken == JsonToken.END_ARRAY) {
                    break;
                }
                Action action = processActionJsonToken(jp, context);
                if (action != null) {
                    actions.getActions().add(action);
                }
            }
        } else {
            throw new IOException(
                "Invalid JSON structure, array expected: " + (jp != null ? jp.getCurrentToken().asString() : null));
        }

        return actions;
    }

    protected Action processActionJsonToken(JsonParser jsonToken, DeserializationContext context) throws IOException {

        if (jsonToken.getCurrentName() == null) {
            return null;
        }

        try {
            String className = jsonToken.getCurrentName().contains(".") ? jsonToken.getCurrentName() : "io.atlasmap.v2." + jsonToken.getCurrentName();
            Class<?> actionClass = context.findClass(className);
            Action action = (Action) actionClass.newInstance();
            // Return if action has no parameters
            if (JsonToken.VALUE_NULL.equals(jsonToken.currentToken())) {
                return action;
            }
            // Map action's parameter fields to their names
            Map<String, java.lang.reflect.Field> parameters = ActionUtil.mapActionParametersByName(actionClass);

            do {
                if (JsonToken.START_OBJECT.equals(jsonToken.currentToken())) {
                    jsonToken.nextToken();
                }
                if (JsonToken.END_OBJECT.equals(jsonToken.currentToken())) {
                    return action;
                }
                java.lang.reflect.Field parameter = parameters.get(jsonToken.getCurrentName());
                jsonToken.nextToken();
                if (parameter.getType() == int.class || parameter.getType() == char.class || parameter.getType() == Integer.class || parameter.getType() == Character.class) {
                    parameter.set(action, jsonToken.getValueAsInt());
                } else if (parameter.getType() == long.class || parameter.getType() == Long.class) {
                    parameter.set(action, jsonToken.getValueAsLong());
                } else if (parameter.getType() == float.class || parameter.getType() == double.class || parameter.getType() == Float.class || parameter.getType() == Double.class) {
                    parameter.set(action, jsonToken.getValueAsDouble());
                } else if (parameter.getType() == boolean.class || parameter.getType() == Boolean.class) {
                    parameter.set(action, jsonToken.getValueAsBoolean());
                } else if (parameter.getType().isEnum()) {
                    String name = jsonToken.getValueAsString().toUpperCase().replace(' ', '_');
                    for (Object constant : parameter.getType().getEnumConstants()) {
                        if (((Enum<?>)constant).name().equals(name)) {
                            parameter.set(action, constant);
                            break;
                        }
                    }
                } else {
                    parameter.set(action, jsonToken.getValueAsString());
                }
            } while (!JsonToken.END_OBJECT.equals(jsonToken.nextToken()));
            return action;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IOException("Invalid action detected: " + jsonToken.getCurrentName(), e);
        }
    }
}
