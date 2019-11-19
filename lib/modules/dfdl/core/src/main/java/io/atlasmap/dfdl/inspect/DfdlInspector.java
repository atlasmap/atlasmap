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
package io.atlasmap.dfdl.inspect;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.ParseResult;
import org.apache.daffodil.japi.ProcessorFactory;
import org.apache.daffodil.japi.infoset.W3CDOMInfosetOutputter;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.dfdl.core.DfdlConstants;
import io.atlasmap.dfdl.core.DfdlSchemaResolver;
import io.atlasmap.xml.core.XmlIOHelper;
import io.atlasmap.xml.inspect.XmlInstanceInspector;
import io.atlasmap.xml.inspect.XmlSchemaInspector;
import io.atlasmap.xml.v2.XmlDocument;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.apache.daffodil.japi.Compiler;

public class DfdlInspector {
    private static final Logger LOG = LoggerFactory.getLogger(DfdlInspector.class);

    private ClassLoader classLoader;
    private DfdlSchemaResolver schemaResolver;
    private XmlInstanceInspector xmlInstanceInspector = new XmlInstanceInspector();
    private XmlSchemaInspector xmlSchemaInspector = new XmlSchemaInspector();
    private XmlDocument output;

    public DfdlInspector(ClassLoader loader) {
        this.classLoader = loader;
        xmlSchemaInspector.setClassLoader(loader);
        this.schemaResolver = new DfdlSchemaResolver(loader);
    }

    public void inspectInstance(String dfdlSchemaName, Map<String, String> options) throws Exception {
        String example = options.get(DfdlConstants.OPTION_EXAMPLE_DATA);
        if (example == null) {
            LOG.error("DFDL instance inspection requires example data, but it was not specified");
            return;
        }

        Compiler c = Daffodil.compiler();
        ProcessorFactory pf = c.compileSource(schemaResolver.resolve(dfdlSchemaName, options));
        DataProcessor dp = pf.onPath("/");
        W3CDOMInfosetOutputter output = new W3CDOMInfosetOutputter();
        ParseResult result = dp.parse(new InputSourceDataInputStream(example.getBytes()), output);
        if (result.isError()) {
            StringBuffer buf = new StringBuffer("DFDL inspection failed");
            result.getDiagnostics().forEach(d -> {
                buf.append("; ").append(d.getMessage());
            });
            throw new AtlasException(buf.toString());
        }

        if (LOG.isTraceEnabled()) {
            try {
                LOG.trace(new XmlIOHelper(classLoader).writeDocumentToString(false, output.getResult()));
            } catch (Exception e) {}
        }
        xmlInstanceInspector.inspect(output.getResult());
        this.output = xmlInstanceInspector.getXmlDocument();
    }

    public void inspectSchema(String dfdlSchemaName, Map<String, String> options) throws Exception {
        URI uri = schemaResolver.resolve(dfdlSchemaName, options);
        xmlSchemaInspector.inspect(new File(uri));
        this.output = xmlSchemaInspector.getXmlDocument();
    }

	public XmlDocument getXmlDocument() {
        return this.output;
	}

}