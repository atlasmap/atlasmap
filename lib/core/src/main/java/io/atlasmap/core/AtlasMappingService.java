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
package io.atlasmap.core;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Json;


public class AtlasMappingService implements Serializable {

    private static final long serialVersionUID = 1668362984516180517L;
    private static final Logger LOG = LoggerFactory.getLogger(AtlasMappingService.class);
    private transient ObjectMapper jsonMapper = null;

    public AtlasMappingService() {
        this(AtlasMapping.class.getClassLoader());
    }

    public AtlasMappingService(ClassLoader classLoader) {
        initialize(classLoader);
    }

    private void initialize(ClassLoader classLoader) {
        jsonMapper = Json.withClassLoader(classLoader);
    }

    public AtlasMapping loadMapping(File file) throws AtlasValidationException {
        try {
            AtlasMapping atlasMapping = jsonMapper.readValue(file, AtlasMapping.class);
            validate(atlasMapping);
            return atlasMapping;
        } catch (Exception e) {
            throw new AtlasValidationException(e.getMessage(), e);
        }
    }

    public AtlasMapping loadMapping(Reader reader) throws AtlasValidationException {
        try {
            AtlasMapping atlasMapping = jsonMapper.readValue(reader, AtlasMapping.class);
            validate(atlasMapping);
            return atlasMapping;
        } catch (Exception e) {
            throw new AtlasValidationException(e.getMessage(), e);
        }
    }

    public AtlasMapping loadMapping(String fileName) throws AtlasValidationException {
        return loadMapping(new File(fileName));
    }

    public AtlasMapping loadMapping(InputStream inputStream) throws AtlasValidationException {
        return loadMapping(new InputStreamReader(inputStream));
    }

    public AtlasMapping loadMapping(URI uri) throws AtlasValidationException {
        return loadMapping(new File(uri));
    }

    public AtlasMapping loadMapping(URL url) throws AtlasValidationException {
        try {
            return loadMapping(new File(url.toURI()));
        } catch (URISyntaxException e) {
            throw new AtlasValidationException(e.getMessage(), e);
        }
    }

    public void saveMappingAsFile(AtlasMapping atlasMapping, File file) throws AtlasException {
        try {
            jsonMapper.writeValue(file, atlasMapping);
        } catch (Exception e) {
            throw new AtlasValidationException(e.getMessage(), e);
        }
    }

    public void validate(AtlasMapping atlasMapping) {
        // if(atlasMapping == null || atlasMapping.getName() == null) {
        // throw new AtlasValidationException("AtlasMapping and name must be
        // specified");
        // }
    }

    public ObjectMapper getObjectMapper() {
        return jsonMapper;
    }

    public void setObjectMapper(ObjectMapper mapper) {
        this.jsonMapper = mapper;
    }

}
