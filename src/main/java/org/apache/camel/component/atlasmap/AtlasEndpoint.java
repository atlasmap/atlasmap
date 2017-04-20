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

import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.IOHelper;
import org.apache.camel.util.ResourceHelper;
import com.mediadriver.atlas.api.v2.AtlasContext;
import com.mediadriver.atlas.api.v2.AtlasContextFactory;
import com.mediadriver.atlas.api.v2.AtlasSession;
import com.mediadriver.atlas.core.v2.DefaultAtlasContextFactory;
import com.mediadriver.atlas.v2.AtlasMapping;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.util.ObjectHelper;

/**
 * Transforms the message using an AtlasMap transformation
 */
@UriEndpoint(firstVersion = "2.19.0", scheme = "atlas", title = "AtlasMap", syntax = "atlas:resourceUri", producerOnly = true, label = "transformation")
public class AtlasEndpoint extends ResourceEndpoint {

    private AtlasContextFactory atlasContextFactory;
    // TODO: cache the context so only a session needs to be created on each exchange
    private AtlasContext atlasContext;

    @UriParam(defaultValue = "true")
    private boolean loaderCache = true;
    @UriParam
    private String encoding;
    @UriParam
    private String propertiesFile;

    public AtlasEndpoint() { }

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

    private synchronized AtlasContextFactory getAtlasContextFactory() throws Exception {
        if (atlasContextFactory == null) {

        	Properties properties = new Properties();
        	
            // load the properties from property file which may overrides the default ones
            if (ObjectHelper.isNotEmpty(getPropertiesFile())) {
                InputStream reader = ResourceHelper.resolveMandatoryResourceAsInputStream(getCamelContext(), getPropertiesFile());
                try {
                    properties.load(reader);
                    log.info("Loaded the Atlas properties file " + getPropertiesFile());
                } finally {
                    IOHelper.close(reader, getPropertiesFile(), log);
                }
                log.debug("Initializing AtlasContextFactory with properties {}", properties);
                atlasContextFactory = new DefaultAtlasContextFactory(properties);
            } else {
            	atlasContextFactory = DefaultAtlasContextFactory.getInstance();
            }       
        }
        return atlasContextFactory;
    }

    public void setAtlasContextFactory(AtlasContextFactory atlasContextFactory) {
        this.atlasContextFactory = atlasContextFactory;
    }

    public boolean isLoaderCache() {
        return loaderCache;
    }

    /**
     * Enables / disables the atlas map resource loader cache which is enabled by default
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
     * The URI of the properties file which is used for AtlasContextFactory initialization.
     */
    public void setPropertiesFile(String file) {
        propertiesFile = file;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public AtlasEndpoint findOrCreateEndpoint(String uri, String newResourceUri) {
        String newUri = uri.replace(getResourceUri(), newResourceUri);
        log.debug("Getting endpoint with URI: {}", newUri);
        return getCamelContext().getEndpoint(newUri, AtlasEndpoint.class);
    }

    @Override
    protected void onExchange(Exchange exchange) throws Exception {
        String path = getResourceUri();
        ObjectHelper.notNull(path, "mappingUri");

        String newResourceUri = exchange.getIn().getHeader(AtlasConstants.ATLAS_RESOURCE_URI, String.class);
        if (newResourceUri != null) {
            exchange.getIn().removeHeader(AtlasConstants.ATLAS_RESOURCE_URI);

            log.debug("{} set to {} creating new endpoint to handle exchange", AtlasConstants.ATLAS_RESOURCE_URI, newResourceUri);
            AtlasEndpoint newEndpoint = findOrCreateEndpoint(getEndpointUri(), newResourceUri);
            newEndpoint.onExchange(exchange);
            return;
        }

        Reader reader;
        String content = exchange.getIn().getHeader(AtlasConstants.ATLAS_MAPPING, String.class);
        if (content != null) {
            // use content from header
            reader = new StringReader(content);
            if (log.isDebugEnabled()) {
                log.debug("Atlas mapping content read from header {} for endpoint {}", AtlasConstants.ATLAS_MAPPING, getEndpointUri());
            }
            // remove the header to avoid it being propagated in the routing
            exchange.getIn().removeHeader(AtlasConstants.ATLAS_MAPPING);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Atlas mapping content read from resource {} with resourceUri: {} for endpoint {}", new Object[]{getResourceUri(), path, getEndpointUri()});
            }
            reader = getEncoding() != null ? new InputStreamReader(getResourceAsInputStream(), getEncoding()) : new InputStreamReader(getResourceAsInputStream());
        }

       	AtlasMapping atlasMapping = null;
       	        
       	if(path != null && path.endsWith("json")) {
       	    atlasMapping = ((DefaultAtlasContextFactory)getAtlasContextFactory()).getMappingService().loadMappingJson(reader);
       	} else {
       	    atlasMapping = ((DefaultAtlasContextFactory)getAtlasContextFactory()).getMappingService().loadMapping(reader);
       	}
       	
        AtlasContext atlasContext = getAtlasContextFactory().createContext(atlasMapping);
		AtlasSession atlasSession = atlasContext.createSession();
		atlasSession.setInput(exchange.getIn().getBody());
		atlasContext.process(atlasSession);
          
        // now lets output the results to the exchange
        Message out = exchange.getOut();
        out.setBody(atlasSession.getOutput());
        out.setHeaders(exchange.getIn().getHeaders());
        out.setAttachments(exchange.getIn().getAttachments());
    }
}
