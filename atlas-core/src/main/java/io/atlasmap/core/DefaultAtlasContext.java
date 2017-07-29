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

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.mxbean.AtlasContextMXBean;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleInfo;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.Validations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class DefaultAtlasContext implements AtlasContext, AtlasContextMXBean {

	private static final Logger logger = LoggerFactory.getLogger(DefaultAtlasContext.class);
	private ObjectName jmxObjectName;
	private final UUID uuid;
	private DefaultAtlasContextFactory factory;
	private AtlasMapping mappingDefinition;
	private URI atlasMappingUri;
	private AtlasModule sourceModule;
	private AtlasModule targetModule;
	private Class<AtlasModule> sourceModuleClass;
	private Class<AtlasModule> targetModuleClass;
	private String sourceFormat;
	private String targetFormat;
	private Map<String, String> sourceProperties;
	private Map<String, String> targetProperties;
	
	public DefaultAtlasContext(URI atlasMappingUri) throws AtlasException {
		this.factory = DefaultAtlasContextFactory.getInstance();
		this.uuid = UUID.randomUUID();
		this.atlasMappingUri = atlasMappingUri;
	}
	
	public DefaultAtlasContext(DefaultAtlasContextFactory factory, URI atlasMappingUri) throws AtlasException {
		this.factory = factory;
		this.uuid = UUID.randomUUID();
        this.atlasMappingUri = atlasMappingUri;
	}
	
	/** 
	 * TODO: For dynamic re-load. This needs lock()
	 * 
	 * @throws AtlasException
	 */
	protected void init() throws AtlasException {
				
		registerJmx(this);

		this.mappingDefinition = factory.getMappingService().loadMapping(this.atlasMappingUri);
		
		List<AtlasModuleInfo> modules = factory.getModules();
		
		for (AtlasModuleInfo module : modules) {
			if(AtlasUtil.matchUriModule(module.getUri(), getSourceModuleUri())) {
				try {
					setSourceModuleClass((Class<AtlasModule>)Class.forName(module.getModuleClassName()));
					setSourceModule(getSourceModuleClass().newInstance());
					getSourceModule().setMode(AtlasModuleMode.SOURCE);
					getSourceModule().setConversionService(getDefaultAtlasContextFactory().getConversionService());
					getSourceModule().init();
				} catch (ClassNotFoundException e) {
					logger.error("Cannot find source ModuleClass " + module.toString(), e);
					throw new AtlasException("Cannot source ModuleClass: " + module.getModuleClassName(), e);
				} catch (ReflectiveOperationException e) {
					logger.error("Unable to initialize target module: " + module.toString(), e);
					throw new AtlasException("Unable to initialize target module: " + module.getModuleClassName(), e);
				}
			}
			if(AtlasUtil.matchUriModule(module.getUri(), getTargetModuleUri())) {
				try {
					setTargetModuleClass((Class<AtlasModule>)Class.forName(module.getModuleClassName()));
					setTargetModule(getTargetModuleClass().newInstance());
					getTargetModule().setMode(AtlasModuleMode.TARGET);
					getTargetModule().setConversionService(getDefaultAtlasContextFactory().getConversionService());
					getTargetModule().init();
				} catch (ClassNotFoundException e) {
					logger.error("Cannot find target ModuleClass: " + module.toString(), e);
					throw new AtlasException("Cannot find target ModuleClass: " + module.getModuleClassName(), e);
				} catch (ReflectiveOperationException e) {
					logger.error("Unable to initialize target module: " + module.toString(), e);
					throw new AtlasException("Unable to initialize target module: " + module.getModuleClassName(), e);
				}
			}
 		}		
	}
	
	protected void registerJmx(DefaultAtlasContext context) {
		try {
			setJmxObjectName(new ObjectName(getDefaultAtlasContextFactory().getJmxObjectName()+",context=Contexts,uuid="+uuid.toString()));
			ManagementFactory.getPlatformMBeanServer().registerMBean(this, getJmxObjectName());
			if(logger.isDebugEnabled()) {
				logger.debug("Registered AtlasContext " + context.getUuid() + " with JMX");
			}
		} catch (Throwable t) {
			logger.warn("Failured to register AtlasContext " + context.getUuid() + " with JMX msg: " + t.getMessage(), t);
		}
	}
	
	/**
	 * Process session lifecycle
	 * 
	 */
	@Override
	public void process(AtlasSession session) throws AtlasException {
		if(logger.isDebugEnabled()) {
			logger.debug("Begin process " + (session == null ? null : session.toString()));
		}
		
	    getSourceModule().processPreValidation(session);
	    getTargetModule().processPreValidation(session);
		
	    // TODO: Finish validations
	    /* 
            if(session.hasErrors()) {
                logger.error(String.format("Aborting processing due to %s errors", session.errorCount()));
                return;
            }
        */
		getSourceModule().processPreInputExecution(session);
		getTargetModule().processPreOutputExecution(session);

	      // TODO: Finish validations
        /* 
            if(session.hasErrors()) {
                logger.error(String.format("Aborting processing due to %s errors", session.errorCount()));
                return;
            }
        */
		
		for(BaseMapping mapping : session.getMapping().getMappings().getMapping()) {		    
		    getSourceModule().processInputMapping(session, mapping);
            getSourceModule().processInputActions(session, mapping);
            getTargetModule().processOutputMapping(session, mapping);            		    
              
		    if(session.hasErrors()) {
		        logger.error(String.format("Aborting processing due to %s errors", session.errorCount()));
		        break;
		    }
		}
            
		getSourceModule().processPostValidation(session);
		getTargetModule().processPostValidation(session);
        
	      // TODO: Finish validations
        /* 
            if(session.hasErrors()) {
                logger.error(String.format("Aborting processing due to %s errors", session.errorCount()));
                return;
            }
        */
		
		getSourceModule().processPostInputExecution(session);
		getTargetModule().processPostOutputExecution(session);
		
		if(logger.isDebugEnabled()) {
			logger.debug("End process " + (session == null ? null : session.toString()));
		}
	}
	
	@Override
    public void processValidation(AtlasSession session) throws AtlasException {
        if(logger.isDebugEnabled()) {
            logger.debug("Begin processValidation " + (session == null ? null : session.toString()));
        }

        List<Validation> validations = getContextFactory().getValidationService().validateMapping(session.getMapping());
        if(validations != null && !validations.isEmpty()) {
            session.getValidations().getValidation().addAll(validations);
        }
        
        if(logger.isDebugEnabled()) {
            logger.debug("Detected " + validations.size() + " core validation notices");
        }
         
        getSourceModule().processPreValidation(session);
        getTargetModule().processPreValidation(session);
   
        if(logger.isDebugEnabled()) {
            logger.debug("End processValidation " + (session == null ? null : session.toString()));
        }
    }
	
	protected DefaultAtlasContextFactory getDefaultAtlasContextFactory() { return this.factory; }
		
	@Override
	public AtlasContextFactory getContextFactory() {
		return this.factory;
	}

	public AtlasMapping getMapping() {
		return mappingDefinition;
	}

	@Override
	public AtlasSession createSession() throws AtlasValidationException {
		return createSession(getDefaultAtlasContextFactory().getMappingService().loadMapping(atlasMappingUri));
	}
	
	public AtlasSession createSession(AtlasMapping mappingDefinition) {
		AtlasSession session = new DefaultAtlasSession(mappingDefinition);
		session.setAtlasContext(this);
		session.setAudits(new Audits());
		session.setValidations(new Validations());
		setDefaultSessionProperties(session);
		return session;
	}
	
	protected void setDefaultSessionProperties(AtlasSession session) {
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		df.setTimeZone(TimeZone.getDefault());
		session.getProperties().put("Atlas.CreatedDateTimeTZ", df.format(date));
	}
	
	public AtlasModule getSourceModule() {
		return sourceModule;
	}

	public void setSourceModule(AtlasModule sourceModule) {
		this.sourceModule = sourceModule;
	}

	public AtlasModule getTargetModule() {
		return targetModule;
	}

	public void setTargetModule(AtlasModule targetModule) {
		this.targetModule = targetModule;
	}

	public Class<AtlasModule> getSourceModuleClass() {
		return sourceModuleClass;
	}

	public void setSourceModuleClass(Class<AtlasModule> sourceModuleClass) {
		this.sourceModuleClass = sourceModuleClass;
	}

	public Class<AtlasModule> getTargetModuleClass() {
		return targetModuleClass;
	}

	public void setTargetModuleClass(Class<AtlasModule> targetModuleClass) {
		this.targetModuleClass = targetModuleClass;
	}

	public String getSourceFormat() {
		return sourceFormat;
	}

	public void setSourceFormat(String sourceFormat) {
		this.sourceFormat = sourceFormat;
	}

	public String getTargetFormat() {
		return targetFormat;
	}

	public void setTargetFormat(String targetFormat) {
		this.targetFormat = targetFormat;
	}

	public Map<String, String> getSourceProperties() {
		return sourceProperties;
	}

	public void setSourceProperties(Map<String, String> sourceProperties) {
		this.sourceProperties = sourceProperties;
	}

	public Map<String, String> getTargetProperties() {
		return targetProperties;
	}

	public void setTargetProperties(Map<String, String> targetProperties) {
		this.targetProperties = targetProperties;
	}

	protected void setJmxObjectName(ObjectName jmxObjectName) {
		this.jmxObjectName = jmxObjectName;
	}
	
	public ObjectName getJmxObjectName() {
		return this.jmxObjectName;
	}
	
	public String getSourceModuleUri() {
		if(getMapping() != null && getMapping().getDataSource() != null && getMapping().getDataSource().get(0) != null) {
			return getMapping().getDataSource().get(0).getUri();
		}
		return null;
	}
	
	public String getTargetModuleUri() {
		if(getMapping() != null && getMapping().getDataSource() != null && getMapping().getDataSource().get(1) != null) {
			return getMapping().getDataSource().get(1).getUri();
		}
		return null;
	}
	
	@Override
	public String getUuid() {
		return (this.uuid != null ? this.uuid.toString() : null);
	}
	
	@Override
	public String getVersion() {
		return this.getClass().getPackage().getImplementationVersion();
	}

	@Override
	public String getMappingName() {
		return (mappingDefinition != null ? mappingDefinition.getName() : null);
	}
	
	protected void setMappingUri(URI atlasMappingUri) {
	    this.atlasMappingUri = atlasMappingUri;
	}
	
	@Override
    public String getMappingUri() {
        return (atlasMappingUri != null ? atlasMappingUri.toString() : null);
    }

    @Override
	public String getClassName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getThreadName() {
		return Thread.currentThread().getName();
	}
	
	@Override
	public String toString() {
		return "DefaultAtlasContext [jmxObjectName=" + jmxObjectName + ", uuid=" + uuid + ", factory=" + factory
				+ ", mappingName=" + getMappingName() + ", mappingUri=" + getMappingUri() 
				+ ", sourceModule=" + sourceModule + ", targetModule=" + targetModule
				+ ", sourceModuleClass=" + sourceModuleClass + ", targetModuleClass=" + targetModuleClass
				+ ", sourceFormat=" + sourceFormat + ", targetFormat=" + targetFormat + ", sourceProperties="
				+ sourceProperties + ", targetProperties=" + targetProperties + "]";
	}	
}
