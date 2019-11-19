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
package io.atlasmap.dfdl.module;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.daffodil.japi.Daffodil;
import org.apache.daffodil.japi.DataProcessor;
import org.apache.daffodil.japi.Diagnostic;
import org.apache.daffodil.japi.ParseResult;
import org.apache.daffodil.japi.ProcessorFactory;
import org.apache.daffodil.japi.UnparseResult;
import org.apache.daffodil.japi.infoset.W3CDOMInfosetInputter;
import org.apache.daffodil.japi.infoset.W3CDOMInfosetOutputter;
import org.apache.daffodil.japi.io.InputSourceDataInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import io.atlasmap.api.AtlasException;
import io.atlasmap.dfdl.core.DfdlConstants;
import io.atlasmap.dfdl.core.DfdlSchemaResolver;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.xml.module.XmlModule;

@AtlasModuleDetail(name = "DfdlModule", uri = "atlas:dfdl", modes = { "SOURCE", "TARGET" }, dataFormats = {
        "dfdl" }, configPackages = { "io.atlasmap.dfdl.v2" })
public class DfdlModule extends XmlModule {
    private static final Logger LOG = LoggerFactory.getLogger(DfdlModule.class);

    private DfdlSchemaResolver schemaResolver;
    private DataProcessor daffodil;

    @Override
    public void init() throws AtlasException {
        super.init();
        this.schemaResolver = new DfdlSchemaResolver(getClassLoader());
        String type = getUriDataType();

        // FIXME temporary hack until we get a room for carrying DFDL options in catalog file
        // https://github.com/atlasmap/atlasmap/issues/1476
        Map<String, String> options = new HashMap<>();
        for (Entry<String, String> e : getUriParameters().entrySet()) {
            if (e.getKey().startsWith(DfdlConstants.OPTION_PREFIX)) {
                options.put(e.getKey(), e.getValue());
            }
        }

        try {
            URI dfdl = this.schemaResolver.resolve(type, options);
            org.apache.daffodil.japi.Compiler compiler = Daffodil.compiler();
            ProcessorFactory factory = compiler.compileSource(dfdl);
            if (factory.isError()) {
                StringBuffer buf = new StringBuffer("Faield ti initialize DFDL module: [");
                for (Diagnostic d : factory.getDiagnostics()) {
                    buf.append(d.getMessage()).append("; ");
                }
                buf.append("]");
                throw new AtlasException(buf.toString());
            }
            this.daffodil = factory.onPath("/");
        } catch (Exception e) {
            throw new AtlasException("Failed to initialize DFDL module:", e);
        }
    }

    @Override
    protected DfdlValidationService createValidationService() {
        DfdlValidationService dfdlValidationService = new DfdlValidationService(getConversionService(), getFieldActionService());
        dfdlValidationService.setMode(getMode());
        dfdlValidationService.setDocId(getDocId());
        return dfdlValidationService;
    }

    @Override
    protected Document convertToXmlDocument(String source, boolean namespaced) throws AtlasException {
        W3CDOMInfosetOutputter output = new W3CDOMInfosetOutputter();
        ParseResult result = this.daffodil.parse(new InputSourceDataInputStream(source.getBytes()), output);
        if (result.isError()) {
            StringBuffer buf = new StringBuffer("DFDL document read error");
            result.getDiagnostics().forEach(d -> {
                buf.append("; ").append(d.getMessage());
            });
            throw new AtlasException(buf.toString());
        }
        if (LOG.isTraceEnabled()) {
            try {
                LOG.trace("DFDL: converted to XML >>> " + getXmlIOHelper().writeDocumentToString(false, output.getResult()));
            } catch (Exception e) {}
        }
        return output.getResult();
    }

    @Override
    protected String convertFromXmlDocument(Document xml) throws AtlasException {
        if (LOG.isTraceEnabled()) {
            try {
                LOG.trace("DFDL: converting from XML >>> " + getXmlIOHelper().writeDocumentToString(false, xml));
            } catch (Exception e) {}
        }
        W3CDOMInfosetInputter input = new W3CDOMInfosetInputter(xml);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UnparseResult result = this.daffodil.unparse(input, Channels.newChannel(bos));
        if (result.isError()) {
            StringBuffer buf = new StringBuffer("DFDL document read error");
            result.getDiagnostics().forEach(d -> {
                buf.append("; ").append(d.getMessage());
            });
            throw new AtlasException(buf.toString());
        }
        return new String(bos.toByteArray());
    }

}
