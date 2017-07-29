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

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.v2.AtlasMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AtlasMappingService implements Serializable {
    
    public enum AtlasMappingFormat {
        XML("xml"),
        JSON("json");
        
        private String value;
        
        private AtlasMappingFormat(String value) { this.value = value; }
        public String value() { return value; }
    }
    
	private static final long serialVersionUID = 1668362984516180517L;
	private static final Logger logger = LoggerFactory.getLogger(AtlasMappingService.class);
	private transient JAXBContext ctx = null;
	private transient Marshaller marshaller = null;
	private transient Unmarshaller unmarshaller = null;
	
	private static final String CONFIG_V2_PACKAGE = "io.atlasmap.v2";
	
	public AtlasMappingService() {
		try {
			List<String> tmp = new ArrayList<String>();
			tmp.add(CONFIG_V2_PACKAGE);
			initialize(tmp);
		} catch (Exception e) {
			logger.error("Error initializing JAXB: " + e.getMessage(), e);
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	public AtlasMappingService(List<String> modulePackages) {
		try {
			initialize(modulePackages);
		} catch (Exception e) {
			logger.error("Error initializing JAXB: " + e.getMessage(), e);
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	protected void initialize(List<String> packages) throws JAXBException {

		if (getJAXBContext() == null) {
			setJAXBContext(JAXBContext.newInstance(stringListToColonSeparated(packages)));
			if(logger.isDebugEnabled()) {
				logger.debug("Initialized JAXBContext: " + stringListToColonSeparated(packages));
			}
		}
		
        marshaller = getJAXBContext().createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        unmarshaller = getJAXBContext().createUnmarshaller();
	}
	
	public AtlasMapping loadMapping(String fileName) throws AtlasValidationException {
		try {
			StreamSource streamSource = new StreamSource(new File(fileName));
			AtlasMapping atlasMapping = unmarshaller.unmarshal(streamSource, AtlasMapping.class).getValue();
			validate(atlasMapping);
			return atlasMapping;
		} catch (JAXBException e) {
			throw new AtlasValidationException(e.getMessage(), e);
		}
	}
	
	public AtlasMapping loadMapping(Reader reader) throws AtlasValidationException {
		try {
			StreamSource streamSource = new StreamSource(reader);
			AtlasMapping atlasMapping = unmarshaller.unmarshal(streamSource, AtlasMapping.class).getValue();
			validate(atlasMapping);
			return atlasMapping;
		} catch (JAXBException e) {
			throw new AtlasValidationException(e.getMessage(), e);
		}
	}
	
	public AtlasMapping loadMapping(InputStream inputStream) throws AtlasValidationException {
		try {
			StreamSource streamSource = new StreamSource(inputStream);
			AtlasMapping atlasMapping = unmarshaller.unmarshal(streamSource, AtlasMapping.class).getValue();
			validate(atlasMapping);
			return atlasMapping;
		} catch (JAXBException e) {
			throw new AtlasValidationException(e.getMessage(), e);
		}
	}
	
	public AtlasMapping loadMapping(File file) throws AtlasValidationException {
		try {
			StreamSource streamSource = new StreamSource(file);
			AtlasMapping atlasMapping = unmarshaller.unmarshal(streamSource, AtlasMapping.class).getValue();
			validate(atlasMapping);
			return atlasMapping;
		} catch (JAXBException e) {
			throw new AtlasValidationException(e.getMessage(), e);
		}
	}
	
	public AtlasMapping loadMapping(URI uri) throws AtlasValidationException {
		try {
			StreamSource streamSource = new StreamSource(new File(uri));
			JAXBElement<AtlasMapping> altasMappingElem = unmarshaller.unmarshal(streamSource, AtlasMapping.class);
			AtlasMapping atlasMapping = altasMappingElem.getValue();
			validate(atlasMapping);
			return atlasMapping;
		} catch (JAXBException e) {
			throw new AtlasValidationException(e.getMessage(), e);
		}
	}
	
	public AtlasMapping loadMapping(URL url) throws AtlasValidationException {
		try {
			StreamSource streamSource = new StreamSource(new File(url.toURI()));
			AtlasMapping atlasMapping = unmarshaller.unmarshal(streamSource, AtlasMapping.class).getValue();
			validate(atlasMapping);
			return atlasMapping;
		} catch (JAXBException e) {
			throw new AtlasValidationException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new AtlasValidationException(e.getMessage(), e);
		}
	}
	
	public void saveMappingAsFile(AtlasMapping atlasMapping, File file) throws AtlasException {
	    saveMappingAsFile(atlasMapping, file, AtlasMappingFormat.XML);
	}
	
	public void saveMappingAsFile(AtlasMapping atlasMapping, File file, AtlasMappingFormat format) throws AtlasException {
		switch(format) {
		case JSON: saveMappingAsJsonFile(atlasMapping, file); break;
		case XML: saveMappingAsXmlFile(atlasMapping, file); break;
		default: saveMappingAsXmlFile(atlasMapping, file); break;
		}
	}
	
	protected void saveMappingAsJsonFile(AtlasMapping atlasMapping, File file) throws AtlasUnsupportedException {
	    throw new AtlasUnsupportedException("Saving as Json not implemented");
	}
	
	protected void saveMappingAsXmlFile(AtlasMapping atlasMapping, File file) throws AtlasException {
	    try {
	        marshaller.marshal(atlasMapping ,file);
	    } catch (JAXBException e) {
	        throw new AtlasValidationException(e.getMessage(), e);
	    }       
	}

	public void validate(AtlasMapping atlasMapping) throws AtlasValidationException {
//		if(atlasMapping == null || atlasMapping.getName() == null) {
//			throw new AtlasValidationException("AtlasMapping and name must be specified");
//		}
	}
	
	public JAXBContext getJAXBContext() {
		return ctx;
	}

	public void setJAXBContext(JAXBContext ctx) {
		this.ctx = ctx;
	}

	public Marshaller getMarshaller() {
		return marshaller;
	}

	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}

	public void setUnmarshaller(Unmarshaller unmarshaller) {
		this.unmarshaller = unmarshaller;
	}
	
	private String stringListToColonSeparated(List<String> items) {
		StringBuilder buffer = new StringBuilder(CONFIG_V2_PACKAGE);
		
		if(items == null) {
			return null;
		}
		
		if(items.isEmpty()) {
			return buffer.toString();
		}
		
		boolean first = true;
		for (int i=0; i<items.size(); i++) {
		    if(!CONFIG_V2_PACKAGE.equals(items.get(i))) {
		        if(first) {
		            buffer.append(":");
		            first = false;
		        }
		        buffer.append(items.get(i));
		        
		        if(i < items.size()-1 ) {
		            buffer.append(":");
		        }
			}
		}
	
		return buffer.toString();
	}
}
