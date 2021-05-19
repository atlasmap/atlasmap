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
package org.apache.camel.component.atlasmap;

import static io.atlasmap.api.AtlasContextFactory.Format.ADM;
import static io.atlasmap.api.AtlasContextFactory.Format.JSON;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.MessageHelper;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;

/**
 * Transforms the message using an AtlasMap transformation.
 */
@UriEndpoint(firstVersion = "2.19.0", scheme = "atlas", title = "AtlasMap", syntax = "atlas:resourceUri", producerOnly = true, label = "transformation")
public class AtlasEndpoint extends ResourceEndpoint {

    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";

    private static final Logger LOG = LoggerFactory.getLogger(AtlasEndpoint.class);
    private AtlasContextFactory atlasContextFactory;
    private AtlasContext atlasContext;

    @UriParam(defaultValue = "true")
    private boolean loaderCache = true;
    @UriParam
    private String encoding;
    @UriParam
    private String sourceMapName;
    @UriParam
    private String targetMapName;
    @UriParam(defaultValue = "MAP")
    private TargetMapMode targetMapMode = TargetMapMode.MAP;

    public enum TargetMapMode {
        MAP, MESSAGE_HEADER, EXCHANGE_PROPERTY;
    }

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
     * default.
     * @param loaderCache true to enable loader cache
     */
    public void setLoaderCache(boolean loaderCache) {
        this.loaderCache = loaderCache;
    }

    /**
     * Character encoding of the resource content.
     * @param encoding encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    /**
     * The Exchange property name for a source message map which hold
     * <code>java.util.Map&lt;String, Message&gt;</code> where the key is AtlasMap Document ID.
     * AtlasMap consumes Message bodies as source documents.
     * @param name Exchange property name for source map
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
     * @param name Exchange property name for target map
     */
    public void setTargetMapName(String name) {
        this.targetMapName = name;
    }

    public String getTargetMapName() {
        return this.targetMapName;
    }

    /**
     * {@link TargetMapMode} enum value to specify how multiple documents are delivered.
     * @param mode {@link TargetMapMode}
     */
    public void setTargetMapMode(TargetMapMode mode) {
        this.targetMapMode = mode;
    }

    public TargetMapMode getTargetMapMode() {
        return this.targetMapMode;
    }

    public AtlasEndpoint findOrCreateEndpoint(String uri, String newResourceUri) {
        String newUri = uri.replace(getResourceUri(), newResourceUri);
        log.debug("Getting endpoint with URI: {}", newUri);
        return getCamelContext().getEndpoint(newUri, AtlasEndpoint.class);
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
                LOG.warn("{}: Document='{}(ID:{})', path='{}'",
                        audit.getMessage(), audit.getDocName(), audit.getDocId(), audit.getPath());
                break;
            default:
                LOG.info("{}: Document='{}(ID:{})', path='{}'",
                        audit.getMessage(), audit.getDocName(), audit.getDocId(), audit.getPath());
            }
        }
        if (!errors.isEmpty()) {
            StringBuilder buf = new StringBuilder("Errors: ");
            errors.stream().forEach(a -> buf.append(
                    String.format("[%s: Document='%s(ID:%s)', path='%s'], ",
                            a.getMessage(), a.getDocName(), a.getDocId(), a.getPath())));
            throw new AtlasException(buf.toString());
        }

        populateTargetDocuments(atlasSession, exchange);
    }

    private AtlasContext getOrCreateAtlasContext(Message incomingMessage) throws Exception {
        String path = getResourceUri();
        ObjectHelper.notNull(path, "mappingUri");

        String content = incomingMessage.getHeader(AtlasConstants.ATLAS_MAPPING, String.class);
        if (content != null) {
            // use content from header
            InputStream is = new ByteArrayInputStream(content.getBytes());
            if (log.isDebugEnabled()) {
                log.debug("Atlas mapping content read from header {} for endpoint {}", AtlasConstants.ATLAS_MAPPING,
                        getEndpointUri());
            }
            // remove the header to avoid it being propagated in the routing
            incomingMessage.removeHeader(AtlasConstants.ATLAS_MAPPING);
            return atlasContextFactory.createContext(JSON, is);
        } else if (getAtlasContext() != null) {
            // no mapping specified in header, and found an existing context
            return getAtlasContext();
        }

        // No mapping in header, and no existing context. Create new one from resourceUri
        if (log.isDebugEnabled()) {
            log.debug("Atlas mapping content read from resourceUri: {} for endpoint {}",
                    new Object[] { path, getEndpointUri() });
        }
        atlasContext = atlasContextFactory.createContext(
                path.toLowerCase().endsWith("adm") ? ADM : JSON, getResourceAsInputStream());
        return atlasContext;
    }

    private void populateSourceDocuments(Exchange exchange, AtlasSession session) {
        if (session.getMapping().getDataSource() == null) {
            return;
        }

        Message inMessage = exchange.getIn();
        CamelAtlasPropertyStrategy propertyStrategy = new CamelAtlasPropertyStrategy();
        propertyStrategy.setCurrentSourceMessage(inMessage);
        propertyStrategy.setTargetMessage(exchange.getMessage());
        propertyStrategy.setExchange(exchange);
        session.setAtlasPropertyStrategy(propertyStrategy);

        DataSource[] sourceDataSources = session.getMapping().getDataSource().stream()
                .filter(ds -> ds.getDataSourceType() == DataSourceType.SOURCE)
                .toArray(DataSource[]::new);
        if (sourceDataSources.length == 0) {
            session.setDefaultSourceDocument(exchange.getIn().getBody());
            return;
        }

        if (sourceDataSources.length == 1) {
            String docId = sourceDataSources[0].getId();
            Object payload = extractPayload(sourceDataSources[0], inMessage);
            if (docId == null || docId.isEmpty()) {
                session.setDefaultSourceDocument(payload);
            } else {
                session.setSourceDocument(docId, payload);
                propertyStrategy.setSourceMessage(docId, inMessage);
            }
            return;
        }

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
            }
        }
        for (DataSource ds : sourceDataSources) {
            String docId = ds.getId();
            if (docId == null || docId.isEmpty()) {
                Object payload = extractPayload(ds, inMessage);
                session.setDefaultSourceDocument(payload);
            } else if (sourceMessages != null) {
                Object payload = extractPayload(ds, sourceMessages.get(docId));
                session.setSourceDocument(docId, payload);
                propertyStrategy.setSourceMessage(docId, sourceMessages.get(docId));
            } else if (sourceDocuments != null) {
                Object payload = sourceDocuments.get(docId);
                session.setSourceDocument(docId, payload);
            } else if (inMessage.getHeaders().containsKey(docId)) {
                Object payload = inMessage.getHeader(docId);
                session.setSourceDocument(docId, payload);
            } else if (exchange.getProperties().containsKey(docId)) {
                Object payload = exchange.getProperty(docId);
                session.setSourceDocument(docId, payload);
            } else {
                LOG.warn("Ignoring missing source document: '{}(ID:{})'", ds.getName(), ds.getId());
            }
        }
    }

    private Object extractPayload(final DataSource dataSource, Message message) {
        if (dataSource == null || message == null) {
            return null;
        }
        Object body = null;

        if (dataSource != null && dataSource.getUri() != null
                && !(dataSource.getUri().startsWith("atlas:core")
                        || dataSource.getUri().startsWith("atlas:java"))) {
            body = message.getBody(String.class);
        } else {
            body = message.getBody();
        }

        //Just in case, prepare for future calls
        MessageHelper.resetStreamCache(message);


        return body;
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
            Object newBody = session.getDefaultTargetDocument();
            outMessage.setBody(newBody);
            return;
        }

        if (targetDataSources.length == 1) {
            String docId = targetDataSources[0].getId();
            if (docId == null || docId.isEmpty()) {
                Object newBody = session.getDefaultTargetDocument();
                outMessage.setBody(newBody);
            } else {
                Object newBody = session.getTargetDocument(docId);
                outMessage.setBody(newBody);
            }
            setContentType(targetDataSources[0], outMessage);
            return;
        }

        Map<String, Object> targetDocuments = new HashMap<>();
        for (DataSource ds : targetDataSources) {
            String docId = ds.getId();
            if (docId == null || docId.isEmpty()) {
                targetDocuments.put(io.atlasmap.api.AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID,
                        session.getDefaultTargetDocument());
                Object newBody = session.getDefaultTargetDocument();
                outMessage.setBody(newBody);
                setContentType(ds, outMessage);
            } else {
                targetDocuments.put(docId, session.getTargetDocument(docId));
            }
        }
        
        switch (targetMapMode) {
        case MAP:
            if (targetMapName != null) {
                exchange.setProperty(targetMapName, targetDocuments);
            } else {
                outMessage.setBody(targetDocuments);
            }
            break;
        case MESSAGE_HEADER:
            targetDocuments.remove(io.atlasmap.api.AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID);
            outMessage.getHeaders().putAll(targetDocuments);
            break;
        case EXCHANGE_PROPERTY:
            targetDocuments.remove(io.atlasmap.api.AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID);
            exchange.getProperties().putAll(targetDocuments);
            break;
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
