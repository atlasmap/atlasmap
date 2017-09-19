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
package io.atlasmap.java.service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class AtlasJsonProvider implements ContextResolver<ObjectMapper> {

    private ObjectMapper objectMapper;

    public AtlasJsonProvider() {
        objectMapper = createObjectMapper();
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        om.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        // Causes errors in serialization .. NPE in
        // FilteringJacksonJaxbJsonProvider.writeTo(FilteringJacksonJaxbJsonProvider.java:130)
        om.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        om.setSerializationInclusion(Include.NON_NULL);
        return om;
    }

    @Override
    public ObjectMapper getContext(Class<?> objectType) {
        return objectMapper;
    }

}
