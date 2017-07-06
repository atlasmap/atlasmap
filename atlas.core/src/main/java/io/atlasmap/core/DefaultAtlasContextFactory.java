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
import io.atlasmap.mxbean.AtlasContextFactoryMXBean;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleInfo;
import io.atlasmap.spi.AtlasSeparateStrategy;
import io.atlasmap.v2.AtlasMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

public class DefaultAtlasContextFactory implements AtlasContextFactory, AtlasContextFactoryMXBean {

	private static Logger logger = LoggerFactory.getLogger(DefaultAtlasContextFactory.class);

	private static DefaultAtlasContextFactory factory = null;
	private String uuid = null;
	private String threadName = null;
	private ObjectName objectName = null;
	private List<AtlasModuleInfo> modules = new ArrayList<AtlasModuleInfo>();
	private AtlasSeparateStrategy separateStrategy = new DefaultAtlasSeparateStrategy();
	private AtlasMappingService atlasMappingService = null;
	private Map<String, String> properties = null;

	public static DefaultAtlasContextFactory getInstance() {
		if (factory == null) {
			factory = new DefaultAtlasContextFactory();
			factory.init();
		}
		return factory;
	}

	public DefaultAtlasContextFactory() { }
	
	public DefaultAtlasContextFactory(Map<String, String> properties) {
		this.properties = properties;
		init(properties);
	}
	
	public DefaultAtlasContextFactory(Properties properties) {
		Map<String, String> tmpProps = new HashMap<String, String>();
		for (final String name: properties.stringPropertyNames()) {
			tmpProps.put(name, properties.getProperty(name));
		}
		setProperties(tmpProps);
	}
	
	public void init() {
		init(null);
	}
	
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	public Map<String, String> getProperties() {
		return this.properties;
	}
	
	public void init(Map<String, String> properties) {
		this.uuid = UUID.randomUUID().toString();
		this.threadName = Thread.currentThread().getName();
		registerFactoryJmx(this);
		loadModules("moduleClass", AtlasModule.class);		
		setMappingService(new AtlasMappingService(getAllModuleConfigPackages(getModules())));
	}

	public void destroy() {
		
		unloadModules();

		try {
			ManagementFactory.getPlatformMBeanServer().unregisterMBean(getJmxObjectName());
			if(logger.isDebugEnabled()) {
				logger.debug("Unregistered AtlasContextFactory with JMX");
			}
		} catch (Exception e) {
			logger.warn("Unable to unregister with JMX", e);
		}
		
		this.uuid = null;
		this.objectName = null;
		this.properties = null;
		this.atlasMappingService = null;
		this.threadName = null;
		factory = null;
	}
	
	public AtlasContext createContext(File atlasMappingFile) throws AtlasException {
		if(getMappingService() == null) {
			throw new AtlasException("AtlasMappingService is not set");
		}
		
		return createContext(getMappingService().loadMapping(atlasMappingFile));
	}
	
	public AtlasContext createContext(URI atlasMappingUri) throws AtlasException {
		if(getMappingService() == null) {
			throw new AtlasException("AtlasMappingService is not set");
		}
		
		return createContext(getMappingService().loadMapping(atlasMappingUri));
	}
	
	public AtlasContext createContext(AtlasMapping atlasMapping) throws AtlasException {
		DefaultAtlasContext context = new DefaultAtlasContext(this, atlasMapping);
		context.init();
		return context;
	}
		
	protected void loadModules(String moduleClassProperty, Class<?> moduleInterface) {
        Class<?> moduleClass = null;
        String moduleClassName = null;
        Set<String> serviceClasses = new HashSet<String>();
        
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
        	Enumeration<URL> urls = classLoader.getResources("META-INF/services/atlas/module/atlas.module");        	
            while(urls.hasMoreElements()) {
            	URL tmp = urls.nextElement();
            	Properties prop = AtlasUtil.loadPropertiesFromURL(tmp);
            	String serviceClassPropertyValue = (String) prop.get(moduleClassProperty);
            	if (!AtlasUtil.isEmpty(serviceClassPropertyValue)) {
            		serviceClasses.add((serviceClassPropertyValue));
            	}
            }            
        } catch (Exception e) {
        	logger.warn("Error loading module resources", e);
        }
   
		for(String clazz : serviceClasses) {			
			try {
            	moduleClass = Class.forName(clazz);
            	moduleClassName = moduleClass.getName();
            	
            	if (isClassAtlasModule(moduleClass, moduleInterface)) {
            		Constructor<?> constructor = moduleClass.getDeclaredConstructor();
            		if(constructor != null) {
            			AtlasModuleInfo module = new DefaultAtlasModuleInfo(getModuleName(moduleClass), getModuleUri(moduleClass), moduleClass, constructor, getSupportedDataFormats(moduleClass), getConfigPackages(moduleClass));
            			getModules().add(module);
            			registerModuleJmx(module);
            		} else {
            			logger.warn("Invalid module class " + moduleClassName + ": constructor is not present");
            		}
            	} else {
            		logger.warn("Invalid module class  " + moduleClassName + ": unsupported AtlasModule");
            	}
        	} catch (NoSuchMethodException e) {
            	logger.warn("Invalid module class " + moduleClassName + ": constructor is not present.", e);
        	} catch (ClassNotFoundException e) {
        		logger.warn("Invalid module class " + moduleClassName + " not found in classLoader", e);
        	} catch (Exception e) {
        		logger.warn("Invalid module class " + moduleClassName + " unknown error", e);
        	}
		} 
		
		if(logger.isDebugEnabled()) {
			logger.debug("Loaded: " + getModules().size() + " of " + serviceClasses.size() + " detected modules");
		}
    }
	
	protected void unloadModules() {
		int moduleCount = getModules().size();
		for (AtlasModuleInfo module : getModules()) {
			try {
				String n = getJmxObjectName()+",info=AvailableModules,moduleName="+module.getName();
				ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(n));				
			} catch (Exception e) {
				logger.warn("Unable to unregister module '" + module.getName() +"' from JMX");
			}
		}

		this.modules.clear();

		if(logger.isDebugEnabled()) {
			logger.debug("Unloaded: " + moduleCount + " modules");
		}
	}
	
	protected boolean isClassAtlasModule(Class<?> clazz, Class<?> moduleInterface) {                
		if (clazz == null) { return false; }
		
		if (isAtlasModuleInterface(clazz, moduleInterface) && clazz.isAnnotationPresent(AtlasModuleDetail.class)) {
			if(logger.isDebugEnabled()) {
				logger.debug(clazz.getCanonicalName() + " is a '" + moduleInterface.getSimpleName() + "' implementation");
			}
			return true;
		}
	        
		if(logger.isDebugEnabled()) {
			logger.debug(clazz.getCanonicalName() + " is NOT a '" + moduleInterface.getSimpleName() + "' implementation");
		}
		return false;
	}
	    
	protected boolean isAtlasModuleInterface(Class<?> clazz, Class<?> moduleInterface) {
		if (clazz == null) {
			return false;
		}   

		boolean isIface = false;
		Class<?> superClazz = clazz.getSuperclass();
		if (superClazz != null) {
			isIface = isAtlasModuleInterface(superClazz, moduleInterface);
		}

		Class<?>[] interfaces = clazz.getInterfaces();
		for (Class<?> iface : interfaces) {
			if (iface.equals(moduleInterface)) {
				isIface = true;
			}
		}
	        
		return isIface;
	}	
	
	protected String getModuleUri(Class<?> clazz) {
        
        AtlasModuleDetail detail = clazz.getAnnotation(AtlasModuleDetail.class);
        
        if (detail != null) {
           return detail.uri();
        }        
        
        return "UNDEFINED";
    }
	
	
	protected String getModuleName(Class<?> clazz) {
        
		AtlasModuleDetail detail = clazz.getAnnotation(AtlasModuleDetail.class);
        
        if (detail != null) {
           return detail.name();
        }        
        
        return "UNDEFINED-"+UUID.randomUUID().toString();
    }
	
    protected List<String> getSupportedDataFormats(Class<?> clazz) {
        
        List<String> dataFormats = null;
        AtlasModuleDetail detail = clazz.getAnnotation(AtlasModuleDetail.class);
        
        if (detail != null) {
            dataFormats = new ArrayList<String>();
            
            String[] formats = detail.dataFormats();
            
            for(String format : formats) {
                dataFormats.add(format.trim());
            }
        }        
        
        if(logger.isDebugEnabled()) {
        	logger.debug("Module: " + clazz.getCanonicalName() + " supports data formats: " + dataFormats.toString());
        }

        return dataFormats;
    }
    
    protected List<String> getConfigPackages(Class<?> clazz) {
        
        List<String> configPackages = null;
        AtlasModuleDetail detail = clazz.getAnnotation(AtlasModuleDetail.class);
        
        if (detail != null) {
            configPackages = new ArrayList<String>();
            
            String[] packages = detail.configPackages();
            
            for(String pkg : packages) {
                configPackages.add(pkg.trim());
            }
        }        
        
        if(logger.isDebugEnabled()) {
        	logger.debug("Module: " + clazz.getCanonicalName() + " config packages: " + configPackages.toString());
        }

        return configPackages;
    }
	
    protected List<String> getAllModuleConfigPackages(List<AtlasModuleInfo> moduleInfos) {
    	List<String> pkgs = new ArrayList<String>();
    	for (AtlasModuleInfo moduleInfo : moduleInfos) {
    		pkgs.addAll(Arrays.asList(moduleInfo.getPackageNames()));
		}
    	return pkgs;
    }
    
    protected void registerFactoryJmx(DefaultAtlasContextFactory factory) {
		try {
			setObjectName(factory.uuid);
			ManagementFactory.getPlatformMBeanServer().registerMBean(factory, factory.getJmxObjectName());
			if(logger.isDebugEnabled()) {
				logger.debug("Registered AtlasContextFactory with JMX");
			}
		} catch (Exception e) {
			logger.warn("Unable to resgister DefaultAtlasContextFactory with JMX", e);
		}
    }
    
    protected void registerModuleJmx(AtlasModuleInfo atlasModuleInfo) {
    	try {
			String n = getJmxObjectName()+",modules=AvailableModules,moduleName="+atlasModuleInfo.getName();
			ManagementFactory.getPlatformMBeanServer().registerMBean(atlasModuleInfo, new ObjectName(n));
			
			if(logger.isDebugEnabled()) {
				logger.debug("Registered AtlasModule '"+atlasModuleInfo.getName()+"' with JMX");
			}
		} catch (Exception e) {
			logger.warn("Unable to register AtlasModule '"+atlasModuleInfo.getName()+"' with JMX", e);
		}
    }
    
	@Override
	public String getClassName() { return this.getClass().getName(); }
	public String getThreadName() { return this.threadName; }	
	public void setThreadName(String threadName) { this.threadName = threadName; }
	public String getVersion() { return this.getClass().getPackage().getImplementationVersion(); }
		
	public String getUuid() { return this.uuid; }
	
	protected void setObjectName(String name) throws MalformedObjectNameException {
		String objectName = String.format("io.atlasmap:type=AtlasServiceFactory,factoryUuid=%s",getUuid());
		this.objectName = new ObjectName(objectName);
	}
	
	public ObjectName getJmxObjectName() {
		return this.objectName;
	}
	
	protected static DefaultAtlasContextFactory getFactory() {
		return factory;
	}

	public AtlasSeparateStrategy getSeparateStrategy() {
		return this.separateStrategy;
	}
	
	public List<AtlasModuleInfo> getModules() {
		return this.modules;
	}

	protected void setModules(List<AtlasModuleInfo> modules) {
		this.modules = modules;
	}

	public AtlasMappingService getMappingService() {
		return this.atlasMappingService;
	}

	public void setMappingService(AtlasMappingService atlasMappingService) {
		this.atlasMappingService = atlasMappingService;
	}
}
