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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class Json {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new AtlasJsonModule())
        .enable(MapperFeature.BLOCK_UNSAFE_POLYMORPHIC_BASE_TYPES)
        .enable(SerializationFeature.INDENT_OUTPUT)
        .configure(SerializationFeature.WRAP_ROOT_VALUE, true)
        .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true)
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .setSerializationInclusion(Include.NON_NULL);

    private Json() {
    }

    public static ObjectMapper mapper() {
        return OBJECT_MAPPER;
    }

    public static ObjectMapper withClassLoader(ClassLoader classLoader) {
        OBJECT_MAPPER.setTypeFactory(TypeFactory.defaultInstance().withClassLoader(classLoader));
        OBJECT_MAPPER.setHandlerInstantiator(new AtlasHandlerInstantiator(classLoader));
        return OBJECT_MAPPER;
    }

}
