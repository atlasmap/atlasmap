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
package io.atlasmap.dfdl.core;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import io.atlasmap.api.AtlasException;
import io.atlasmap.dfdl.core.DfdlSchemaGenerator;

/**
 * DFDL schema resolver resolves DFDL schema file to be used to process DFDL document.
 * It first looks for a static DFDL schema file with specified name. If no schema file is found,
 * then it looks for a {@code DfdlSchemaGenerator} and delegate to it to generate custom schema.
 */
public class DfdlSchemaResolver {

    private ClassLoader classLoader;
    private Map<String, DfdlSchemaGenerator> dfdlGenerators;

    public DfdlSchemaResolver(ClassLoader loader) {
        this.classLoader = loader;
        this.dfdlGenerators = new HashMap<>();
        ServiceLoader<DfdlSchemaGenerator> schemaGenLoader = ServiceLoader.load(DfdlSchemaGenerator.class, this.classLoader);
        for (DfdlSchemaGenerator generator : schemaGenLoader) {
            this.dfdlGenerators.put(generator.getName(), generator);
        }
    }

    public URI resolve(String dfdlSchemaName, Map<String, String> options) throws Exception {
        if (dfdlSchemaName == null) {
            throw new AtlasException("DFDL schema name must be specified");
        }
        URL url = null;
        try {
            url = classLoader.getResource(dfdlSchemaName + ".dfdl.xsd");
        } catch (Exception e) {}
        if (url != null) {
            return url.toURI();
        }

        DfdlSchemaGenerator generator = this.dfdlGenerators.get(dfdlSchemaName);
        if (generator != null) {
            Document xsd = generator.generate(classLoader, options);
            File f = File.createTempFile(dfdlSchemaName, ".dfdl.xsd");
            f.deleteOnExit();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(xsd);
            StreamResult result = new StreamResult(f);
            transformer.transform(source, result);
            return f.toURI();
        }

        throw new AtlasException(String.format("DFDL schema not found for '%s'", dfdlSchemaName));
    }

}