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
package io.atlasmap.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.v2.Json;

/**
 * The base class for all design time REST services.
 * @see AtlasService
 * @see ModuleService
 */
public abstract class BaseAtlasService {
    private static final Logger LOG = LoggerFactory.getLogger(BaseAtlasService.class);

    private AtlasLibraryLoader libraryLoader;

    /**
     * Gets the library loader.
     * @return loader
     */
    public AtlasLibraryLoader getLibraryLoader() {
        return this.libraryLoader;
    }

    /**
     * Sets the {@link AtlasLibraryLoader}.
     * @param loader loader
     */
    protected void setLibraryLoader(AtlasLibraryLoader loader) {
        this.libraryLoader = loader;
    }

    /**
     * Serializes the given object into JSON.
     * @param value object
     * @return serialized
     */
    protected byte[] toJson(Object value) {
        try {
            return getMapper().writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deserializes the given JSON into the object of the given type.
     * @param <T> type
     * @param value JSON
     * @param clazz type
     * @return deserialized
     */
    protected <T> T fromJson(InputStream value, Class<T> clazz) {
        try {
            if (LOG.isDebugEnabled()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(value));
                StringBuffer buf = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    buf.append(line);
                }
                LOG.debug(buf.toString());
                return getMapper().readValue(buf.toString(), clazz);
            }
            return getMapper().readValue(value, clazz);
        } catch (IOException e) {
            throw new WebApplicationException(e, Status.BAD_REQUEST);
        }
    }

    private ObjectMapper getMapper() {
        return this.libraryLoader != null
            ? Json.withClassLoader(getLibraryLoader())
            : Json.mapper();
    }

}
