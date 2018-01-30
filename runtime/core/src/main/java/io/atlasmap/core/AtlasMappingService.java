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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

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
    private static final String CONFIG_V2_PACKAGE = "io.atlasmap.v2";
    private transient JAXBContext ctx = null;
    private transient ObjectMapper jsonMapper = null;

    public enum AtlasMappingFormat {
        XML("xml"), JSON("json");

        private String value;

        AtlasMappingFormat(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public AtlasMappingService() {
        try {
            List<String> tmp = new ArrayList<String>();
            tmp.add(CONFIG_V2_PACKAGE);
            initialize(tmp);
        } catch (Exception e) {
            LOG.error("Error initializing JAXB: " + e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public AtlasMappingService(List<String> modulePackages) {
        try {
            initialize(modulePackages);
        } catch (Exception e) {
            LOG.error("Error initializing JAXB: " + e.getMessage(), e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected void initialize(List<String> packages) throws JAXBException {

        if (getJAXBContext() == null) {
            setJAXBContext(JAXBContext.newInstance(stringListToColonSeparated(packages)));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Initialized JAXBContext: " + stringListToColonSeparated(packages));
            }
        }

        jsonMapper = Json.mapper();
    }

    public AtlasMapping loadMapping(File file) throws AtlasValidationException {
        return loadMapping(file, AtlasMappingFormat.XML);
    }

    public AtlasMapping loadMapping(File file, AtlasMappingFormat format) throws AtlasValidationException {
        try {
            AtlasMapping atlasMapping;
            switch (format) {
            case XML:
                StreamSource streamSource = new StreamSource(file);
                atlasMapping = createUnmarshaller().unmarshal(streamSource, AtlasMapping.class).getValue();
                break;
            case JSON:
                atlasMapping = jsonMapper.readValue(file, AtlasMapping.class);
                break;
            default:
                throw new AtlasValidationException("Unsupported mapping format: " + format.value);
            }
            validate(atlasMapping);
            return atlasMapping;
        } catch (Exception e) {
            throw new AtlasValidationException(e.getMessage(), e);
        }
    }

    public AtlasMapping loadMapping(Reader reader) throws AtlasValidationException {
        return loadMapping(reader, AtlasMappingFormat.XML);
    }

    public AtlasMapping loadMapping(Reader reader, AtlasMappingFormat format) throws AtlasValidationException {
        try {
            AtlasMapping atlasMapping;
            switch (format) {
            case XML:
                StreamSource streamSource = new StreamSource(reader);
                atlasMapping = createUnmarshaller().unmarshal(streamSource, AtlasMapping.class).getValue();
                break;
            case JSON:
                atlasMapping = jsonMapper.readValue(reader, AtlasMapping.class);
                break;
            default:
                throw new AtlasValidationException("Unsupported mapping format: " + format.value);
            }
            validate(atlasMapping);
            return atlasMapping;
        } catch (Exception e) {
            throw new AtlasValidationException(e.getMessage(), e);
        }
    }

    public AtlasMapping loadMapping(String fileName, AtlasMappingFormat format) throws AtlasValidationException {
        return loadMapping(new File(fileName), format);
    }

    public AtlasMapping loadMapping(String fileName) throws AtlasValidationException {
        return loadMapping(fileName, AtlasMappingFormat.XML);
    }

    public AtlasMapping loadMapping(InputStream inputStream) throws AtlasValidationException {
        return loadMapping(inputStream, AtlasMappingFormat.XML);
    }

    public AtlasMapping loadMapping(InputStream inputStream, AtlasMappingFormat format)
            throws AtlasValidationException {
        return loadMapping(new InputStreamReader(inputStream), format);
    }

    public AtlasMapping loadMapping(URI uri) throws AtlasValidationException {
        return loadMapping(uri, AtlasMappingFormat.XML);
    }

    public AtlasMapping loadMapping(URI uri, AtlasMappingFormat format) throws AtlasValidationException {
        return loadMapping(new File(uri), format);
    }

    public AtlasMapping loadMapping(URL url) throws AtlasValidationException {
        return loadMapping(url, AtlasMappingFormat.XML);
    }

    public AtlasMapping loadMapping(URL url, AtlasMappingFormat format) throws AtlasValidationException {
        try {
            return loadMapping(new File(url.toURI()), format);
        } catch (URISyntaxException e) {
            throw new AtlasValidationException(e.getMessage(), e);
        }
    }

    public void saveMappingAsFile(AtlasMapping atlasMapping, File file) throws AtlasException {
        saveMappingAsFile(atlasMapping, file, AtlasMappingFormat.XML);
    }

    public void saveMappingAsFile(AtlasMapping atlasMapping, File file, AtlasMappingFormat format)
            throws AtlasException {
        switch (format) {
        case JSON:
            saveMappingAsJsonFile(atlasMapping, file);
            break;
        case XML:
            saveMappingAsXmlFile(atlasMapping, file);
            break;
        default:
            saveMappingAsXmlFile(atlasMapping, file);
            break;
        }
    }

    protected void saveMappingAsJsonFile(AtlasMapping atlasMapping, File file) throws AtlasException {
        try {
            jsonMapper.writeValue(file, atlasMapping);
        } catch (Exception e) {
            throw new AtlasValidationException(e.getMessage(), e);
        }
    }

    protected void saveMappingAsXmlFile(AtlasMapping atlasMapping, File file) throws AtlasException {
        try {
            createMarshaller().marshal(atlasMapping, file);
        } catch (JAXBException e) {
            throw new AtlasValidationException(e.getMessage(), e);
        }
    }

    public void validate(AtlasMapping atlasMapping) throws AtlasValidationException {
        // if(atlasMapping == null || atlasMapping.getName() == null) {
        // throw new AtlasValidationException("AtlasMapping and name must be
        // specified");
        // }
    }

    public JAXBContext getJAXBContext() {
        return ctx;
    }

    public void setJAXBContext(JAXBContext ctx) {
        this.ctx = ctx;
    }

    public Marshaller createMarshaller() throws JAXBException {
        Marshaller marshaller = getJAXBContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }

    public Unmarshaller createUnmarshaller() throws JAXBException {
        return getJAXBContext().createUnmarshaller();
    }

    public ObjectMapper getObjectMapper() {
        return jsonMapper;
    }

    public void setObjectMapper(ObjectMapper mapper) {
        this.jsonMapper = mapper;
    }

    private String stringListToColonSeparated(List<String> items) {
        StringBuilder buffer = new StringBuilder(CONFIG_V2_PACKAGE);

        if (items == null) {
            return null;
        }

        if (items.isEmpty()) {
            return buffer.toString();
        }

        boolean first = true;
        for (int i = 0; i < items.size(); i++) {
            if (!CONFIG_V2_PACKAGE.equals(items.get(i))) {
                if (first) {
                    buffer.append(":");
                    first = false;
                }
                buffer.append(items.get(i));

                if (i < items.size() - 1) {
                    buffer.append(":");
                }
            }
        }

        return buffer.toString();
    }
}
