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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ActionsJsonSerializer extends JsonSerializer<Actions> {

    @Override
    public void serialize(Actions actions, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeStartArray();

        if (actions == null || actions.getActions() == null || actions.getActions().isEmpty()) {
            gen.writeEndArray();
            return;
        }

        for (Action a : actions.getActions()) {
            writeActionField(gen, a);
        }

        gen.writeEndArray();
    }

    protected void writeActionField(JsonGenerator gen, Action action) throws IOException {
        // Map action's parameter fields to their names
        Map<String, java.lang.reflect.Field> parameters = ActionUtil.mapActionParametersByName(action.getClass());

        gen.writeStartObject();
        String className = action.getClass().getPackage().getName().equals("io.atlasmap.v2") ? action.getClass().getSimpleName() : action.getClass().getName();
        if (parameters.isEmpty()) {
            gen.writeNullField(className);
        } else {
            gen.writeFieldName(className);
            gen.writeStartObject();
            for (java.lang.reflect.Field parameter : parameters.values()) {
                try {
                    Object val = parameter.get(action);
                    Class<?> typeClass = parameter.getType();
                    if (val != null) {
                        if (typeClass == byte.class || typeClass == short.class || typeClass == int.class || typeClass == long.class) {
                            gen.writeNumberField(parameter.getName(), parameter.getLong(action));
                        } else if (typeClass == Byte.class || typeClass == Short.class || typeClass == Integer.class || typeClass == Long.class) {
                            gen.writeNumberField(parameter.getName(), ((Number)parameter.get(action)).longValue());
                        } else if (typeClass == float.class || typeClass == double.class) {
                            gen.writeNumberField(parameter.getName(), parameter.getDouble(action));
                        } else if (typeClass == Float.class || typeClass == Double.class) {
                            gen.writeNumberField(parameter.getName(), ((Number)parameter.get(action)).doubleValue());
                        } else if (typeClass == boolean.class) {
                            gen.writeBooleanField(parameter.getName(), parameter.getBoolean(action));
                        } else if (typeClass == Boolean.class) {
                            gen.writeBooleanField(parameter.getName(), (Boolean)parameter.get(action));
                        } else if (typeClass.isEnum()) {
                            String[] tokens = ((Enum<?>)parameter.get(action)).name().split("_");
                            StringBuilder builder = new StringBuilder();
                            for (String token : tokens) {
                                if (builder.length() > 0) {
                                    builder.append(' ');
                                }
                                if (token.length() > 0) {
                                    builder.append(Character.toUpperCase(token.charAt(0)));
                                    builder.append(token.substring(1));
                                }
                            }
                            gen.writeStringField(parameter.getName(), builder.toString());
                        } else {
                            gen.writeStringField(parameter.getName(), parameter.get(action).toString());
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IOException(e);
                }
            }
            gen.writeEndObject();
        }
        gen.writeEndObject();
    }
}
