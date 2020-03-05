/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.atlasmap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.ResourceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.ADMArchiveHandler;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;

/**
 * Transforms the message using an AtlasMap transformation
 */
@UriEndpoint(firstVersion = "2.19.0", scheme = "atlas", title = "AtlasMap", syntax = "atlas:resourceUri", producerOnly = true, label = "transformation")
public class AtlasEndpoint extends ResourceEndpoint {

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";

    private static final Logger LOG = LoggerFactory.getLogger(AtlasEndpoint.class);
    private AtlasContextFactory atlasContextFactory;
    private AtlasContext atlasContext;
    private Pattern atlasmapMappingFileNamePattern = Pattern.compile("atlasmapping(-UI\\.[0-9]+)?\\.json");

    @UriParam(defaultValue = "true")
    private boolean loaderCache = true;
    @UriParam
    private String encoding;
    @UriParam
    private String propertiesFile;
    @UriParam
    private String sourceMapName;
    @UriParam
    private String targetMapName;

    public AtlasEndpoint(String uri, AtlasComponent component, String resourceUri) {
        super(uri, component, resourceUri);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public ExchangePattern getExchangePattern() {
        return ExchangePattern.InOut;
    }

    @Override
    protected String createEndpointUri() {
        return "atlas:" + getResourceUri();
    }

    public AtlasContextFactory getAtlasContextFactory() {
        return this.atlasContextFactory;
    }

    public void setAtlasContextFactory(AtlasContextFactory atlasContextFactory) {
        this.atlasContextFactory = atlasContextFactory;
    }

    public AtlasContext getAtlasContext() {
        return this.atlasContext;
    }

    public void setAtlasContext(AtlasContext atlasContext) {
        this.atlasContext = atlasContext;
    }

    public boolean isLoaderCache() {
        return loaderCache;
    }

    /**
     * Enables / disables the atlas map resource loader cache which is enabled by
     * default
     */
    public void setLoaderCache(boolean loaderCache) {
        this.loaderCache = loaderCache;
    }

    /**
     * Character encoding of the resource content.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    /**
     * The URI of the properties file which is used for AtlasContextFactory
     * initialization.
     */
    public void setPropertiesFile(String file) {
        propertiesFile = file;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    /**
     * The Exchange property name for a source message map which hold
     * <code>java.util.Map&lt;String, Message&gt;</code> where the key is AtlasMap Document ID.
     * AtlasMap consumes Message bodies as source documents.
     */
    public void setSourceMapName(String name) {
        this.sourceMapName = name;
    }

    public String getSourceMapName() {
        return this.sourceMapName;
    }

    /**
     * The Exchange property name for a target document map which hold
     * <code>java.util.Map&lt;String, Object&gt;</code> where the key is AtlasMap Document ID.
     * AtlasMap populates multiple target documents into this map.
     */
    public void setTargetMapName(String name) {
        this.targetMapName = name;
    }

    public String getTargetMapName() {
        return this.targetMapName;
    }

    public AtlasEndpoint findOrCreateEndpoint(String uri, String newResourceUri) {
        String newUri = uri.replace(getResourceUri(), newResourceUri);
        log.debug("Getting endpoint with URI: {}", newUri);
        return getCamelContext().getEndpoint(newUri, AtlasEndpoint.class);
    }

    /**
     * Extract the AtlasMap mappings file (JSON) from the specified ADM archive
     * and return it as a String.
     *
     * @param in - Live input stream to the ADM archive file via the resource URI.
     * @param admPath - used for diagnostics only
     * @return the extracted mappings JSON content
     *
     * @throws Exception
     */
    private String extractMappingsFromADM(InputStream in, String admPath) throws Exception {
        try {
            ADMArchiveHandler handler = new ADMArchiveHandler(getCamelContext().getApplicationContextClassLoader());
            handler.setIgnoreLibrary(true);
            handler.load(in);
            if (log.isDebugEnabled()) {
                log.debug("Atlas mapping content extracted from ADM archive: {}", admPath);
            }
            return new String(handler.getMappingDefinitionBytes());
        } catch (Exception e) {
            throw new IOException("Error extracting mappings file from ADM archive " + admPath, e);
        }
    }

    @Override
    protected void onExchange(Exchange exchange) throws Exception {
        Message incomingMessage = exchange.getIn();
        String newResourceUri = incomingMessage.getHeader(AtlasConstants.ATLAS_RESOURCE_URI, String.class);
        if (newResourceUri != null) {
            incomingMessage.removeHeader(AtlasConstants.ATLAS_RESOURCE_URI);

            log.debug("{} set to {} creating new endpoint to handle exchange", AtlasConstants.ATLAS_RESOURCE_URI,
                    newResourceUri);
            AtlasEndpoint newEndpoint = findOrCreateEndpoint(getEndpointUri(), newResourceUri);
            newEndpoint.onExchange(exchange);
            return;
        }

        AtlasSession atlasSession = getOrCreateAtlasContext(incomingMessage).createSession();
        populateSourceDocuments(exchange, atlasSession);
        atlasSession.getAtlasContext().process(atlasSession);

        List<Audit> errors = new ArrayList<>();
        for (Audit audit : atlasSession.getAudits().getAudit()) {
            switch (audit.getStatus()) {
            case ERROR:
                errors.add(audit);
                break;
            case WARN:
                LOG.warn("{}: docId='{}', path='{}'", audit.getMessage(), audit.getDocId(), audit.getPath());
                break;
            default:
                LOG.info("{}: docId='{}', path='{}'", audit.getMessage(), audit.getDocId(), audit.getPath());
            }
        }
        if (!errors.isEmpty()) {
            StringBuilder buf = new StringBuilder("Errors: ");
            errors.stream().forEach(a -> buf.append(
                    String.format("[%s: docId='%s', path='%s'], ", a.getMessage(), a.getDocId(), a.getPath())));
            throw new AtlasException(buf.toString());
        }

        populateTargetDocuments(atlasSession, exchange);
    }

    private AtlasContext getOrCreateAtlasContext(Message incomingMessage) throws Exception {
        String path = getResourceUri();
        ObjectHelper.notNull(path, "mappingUri");
        Reader reader = null;

        String content = incomingMessage.getHeader(AtlasConstants.ATLAS_MAPPING, String.class);
        if (content != null) {
            // use content from header
            reader = new StringReader(content);
            if (log.isDebugEnabled()) {
                log.debug("Atlas mapping content read from header {} for endpoint {}", AtlasConstants.ATLAS_MAPPING,
                        getEndpointUri());
            }
            // remove the header to avoid it being propagated in the routing
            incomingMessage.removeHeader(AtlasConstants.ATLAS_MAPPING);
            AtlasMapping mapping = ((DefaultAtlasContextFactory) getOrCreateAtlasContextFactory())
                                    .getMappingService()
                                    .loadMapping(reader);
            return ((DefaultAtlasContextFactory) getOrCreateAtlasContextFactory()).createContext(mapping);
        } else if (getAtlasContext() != null) {
            // no mapping specified in header, and found an existing context
            return getAtlasContext();
        }

        // No mapping in header, and no existing context. Create new one from resourceUri
        if (log.isDebugEnabled()) {
            log.debug("Atlas mapping content read from resourceUri: {} for endpoint {}",
                    new Object[] { path, getEndpointUri() });
        }
        if (path.toLowerCase().endsWith("adm")) {
            reader = new StringReader(extractMappingsFromADM(getResourceAsInputStream(), path));
        }
        else {
            reader = getEncoding() != null ? new InputStreamReader(getResourceAsInputStream(), getEncoding())
                : new InputStreamReader(getResourceAsInputStream());
        }
        AtlasMapping mapping = ((DefaultAtlasContextFactory) getOrCreateAtlasContextFactory())
                .getMappingService()
                .loadMapping(reader);
        atlasContext = ((DefaultAtlasContextFactory) getOrCreateAtlasContextFactory()).createContext(mapping);
        return atlasContext;
    }

    private synchronized AtlasContextFactory getOrCreateAtlasContextFactory() throws Exception {
        if (atlasContextFactory != null) {
            return atlasContextFactory;
        }

        atlasContextFactory = DefaultAtlasContextFactory.getInstance();
        ((DefaultAtlasContextFactory)atlasContextFactory).addClassLoader(getCamelContext().getApplicationContextClassLoader());
        // load the properties from property file which may overrides the default ones
        if (ObjectHelper.isNotEmpty(getPropertiesFile())) {
            Properties properties = new Properties();
            InputStream reader = ResourceHelper.resolveMandatoryResourceAsInputStream(getCamelContext(),
                    getPropertiesFile());
            try {
                properties.load(reader);
                log.info("Loaded the Atlas properties file " + getPropertiesFile());
            } finally {
                IOHelper.close(reader, getPropertiesFile(), log);
            }
            log.debug("Initializing AtlasContextFactory with properties {}", properties);
            atlasContextFactory.setProperties(properties);
        }
        return atlasContextFactory;
    }

    private void populateSourceDocuments(Exchange exchange, AtlasSession session) {
        if (session.getMapping().getDataSource() == null) {
            return;
        }
        DataSource[] sourceDataSources = session.getMapping().getDataSource().stream()
                .filter(ds -> ds.getDataSourceType() == DataSourceType.SOURCE)
                .toArray(DataSource[]::new);
        if (sourceDataSources.length == 0) {
            session.setDefaultSourceDocument(exchange.getIn().getBody());
            return;
        }

        if (sourceDataSources.length == 1) {
            String docId = sourceDataSources[0].getId();
            Object payload = extractPayload(sourceDataSources[0], exchange.getIn());
            if (docId == null || docId.isEmpty()) {
                session.setDefaultSourceDocument(payload);
            } else {
                session.setSourceDocument(docId, payload);
            }
            return;
        }

        // TODO handle headers docId - https://github.com/atlasmap/atlasmap/issues/67
        Map<String, Message> sourceMessages = null;
        Map<String, Object> sourceDocuments = null;
        if (sourceMapName != null) {
            sourceMessages = exchange.getProperty(sourceMapName, Map.class);
        }
        if (sourceMessages == null) {
            Object body = exchange.getIn().getBody();
            if (body instanceof Map) {
                sourceDocuments = (Map<String, Object>)body;
            } else {
                session.setDefaultSourceDocument(body);
                return;
            }
        }
        for (DataSource ds : sourceDataSources) {
            String docId = ds.getId();
            Object payload = sourceMessages != null ? extractPayload(ds, sourceMessages.get(docId))
                    : sourceDocuments.get(docId);
            if (docId == null || docId.isEmpty()) {
                session.setDefaultSourceDocument(payload);
            } else {
                session.setSourceDocument(docId, payload);
            }
        }
    }

    private Object extractPayload(final DataSource dataSource, Message message) {
        if (dataSource == null || message == null) {
            return null;
        }
        if (dataSource != null && dataSource.getUri() != null
                && !(dataSource.getUri().startsWith("atlas:core")
                        || dataSource.getUri().startsWith("atlas:java"))) {
            return message.getBody(String.class);
        }
        return message.getBody();
    }

    private void populateTargetDocuments(AtlasSession session, Exchange exchange) {
        Message outMessage = exchange.getOut();
        outMessage.setHeaders(exchange.getIn().getHeaders());
        outMessage.setAttachments(exchange.getIn().getAttachments());

        if (session.getMapping().getDataSource() == null) {
            return;
        }
        DataSource[] targetDataSources = session.getMapping().getDataSource().stream()
                .filter(ds -> ds.getDataSourceType() == DataSourceType.TARGET)
                .toArray(DataSource[]::new);
        if (targetDataSources.length == 0) {
            outMessage.setBody(session.getDefaultTargetDocument());
            return;
        }

        if (targetDataSources.length == 1) {
            String docId = targetDataSources[0].getId();
            if (docId == null || docId.isEmpty()) {
                outMessage.setBody(session.getDefaultTargetDocument());
            } else {
                outMessage.setBody(session.getTargetDocument(docId));
            }
            setContentType(targetDataSources[0], outMessage);
            return;
        }

        // TODO handle headers docId - https://github.com/atlasmap/atlasmap/issues/67
        Map<String, Object> targetDocuments = new HashMap<>();
        for (DataSource ds : targetDataSources) {
            String docId = ds.getId();
            if (docId == null || docId.isEmpty()) {
                targetDocuments.put(io.atlasmap.api.AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID,
                        session.getDefaultTargetDocument());
                outMessage.setBody(session.getDefaultTargetDocument());
                setContentType(ds, outMessage);
            } else {
                targetDocuments.put(docId, session.getTargetDocument(docId));
            }
        }
        if (targetMapName != null) {
            exchange.setProperty(targetMapName, targetDocuments);
        } else {
            outMessage.setBody(targetDocuments);
        }
    }

    private void setContentType(DataSource ds, Message message) {
        if (ds.getUri() == null) {
            return;
        }
        if (ds.getUri().startsWith("atlas:json")) {
            message.setHeader(Exchange.CONTENT_TYPE, CONTENT_TYPE_JSON);
        } else if (ds.getUri().startsWith("atlas:xml")) {
            message.setHeader(Exchange.CONTENT_TYPE, CONTENT_TYPE_XML);
        }
    }
}
