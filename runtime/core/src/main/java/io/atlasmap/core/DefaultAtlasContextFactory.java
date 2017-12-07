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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasFieldActionService;
import io.atlasmap.api.AtlasValidationService;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.mxbean.AtlasContextFactoryMXBean;
import io.atlasmap.spi.AtlasCombineStrategy;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleInfo;
import io.atlasmap.spi.AtlasModuleInfoRegistry;
import io.atlasmap.spi.AtlasPropertyStrategy;
import io.atlasmap.spi.AtlasSeparateStrategy;
import io.atlasmap.v2.AtlasMapping;

public class DefaultAtlasContextFactory implements AtlasContextFactory, AtlasContextFactoryMXBean {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasContextFactory.class);

    private static DefaultAtlasContextFactory factory = null;
    private String uuid = null;
    private String threadName = null;
    private ObjectName objectName = null;
    private AtlasMappingService atlasMappingService = null;
    private DefaultAtlasConversionService atlasConversionService = null;
    private DefaultAtlasFieldActionService atlasFieldActionService = null;
    private AtlasCombineStrategy atlasCombineStrategy = new DefaultAtlasCombineStrategy();
    private AtlasPropertyStrategy atlasPropertyStrategy = new DefaultAtlasPropertyStrategy();
    private AtlasSeparateStrategy atlasSeparateStrategy = new DefaultAtlasSeparateStrategy();
    private AtlasValidationService atlasValidationService = new DefaultAtlasValidationService();
    private AtlasModuleInfoRegistry moduleInfoRegistry;
    private Map<String, String> properties = null;

    public DefaultAtlasContextFactory() {
    }

    public DefaultAtlasContextFactory(Map<String, String> properties) {
        this.properties = properties;
        init(properties);
    }

    public DefaultAtlasContextFactory(Properties properties) {
        Map<String, String> tmpProps = new HashMap<String, String>();
        for (final String name : properties.stringPropertyNames()) {
            tmpProps.put(name, properties.getProperty(name));
        }
        setProperties(tmpProps);
    }

    public static DefaultAtlasContextFactory getInstance() {
        if (factory == null) {
            factory = new DefaultAtlasContextFactory();
            factory.init();
        }
        return factory;
    }

    @Override
    public void init() {
        init(null);
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void init(Map<String, String> properties) {
        this.uuid = UUID.randomUUID().toString();
        this.threadName = Thread.currentThread().getName();
        this.atlasConversionService = DefaultAtlasConversionService.getInstance();
        this.atlasFieldActionService = new DefaultAtlasFieldActionService(this.atlasConversionService);
        this.atlasFieldActionService.init();
        registerFactoryJmx(this);
        this.moduleInfoRegistry = new DefaultAtlasModuleInfoRegistry(this);
        loadModules("moduleClass", AtlasModule.class);
        setMappingService(new AtlasMappingService(getAllModuleConfigPackages(getModuleInfoRegistry())));
    }

    @Override
    public void destroy() {

        unloadModules();

        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(getJmxObjectName());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unregistered AtlasContextFactory with JMX");
            }
        } catch (Exception e) {
            LOG.warn("Unable to unregister with JMX", e);
        }

        this.uuid = null;
        this.objectName = null;
        this.properties = null;
        this.atlasMappingService = null;
        this.atlasFieldActionService = null;
        this.atlasConversionService = null;
        this.atlasPropertyStrategy = null;
        this.moduleInfoRegistry = null;
        this.threadName = null;
        factory = null;
    }

    @Override
    public AtlasContext createContext(File atlasMappingFile) throws AtlasException {
        return createContext(atlasMappingFile, AtlasMappingFormat.XML);
    }

    public AtlasContext createContext(File atlasMappingFile, AtlasMappingFormat format) throws AtlasException {
        if (atlasMappingFile == null) {
            throw new AtlasException("AtlasMappingFile must be specified");
        }

        return createContext(atlasMappingFile.toURI(), format);
    }

    @Override
    public AtlasContext createContext(URI atlasMappingUri) throws AtlasException {
        return createContext(atlasMappingUri, AtlasMappingFormat.XML);
    }

    public AtlasContext createContext(URI atlasMappingUri, AtlasMappingFormat format) throws AtlasException {
        if (atlasMappingUri == null) {
            throw new AtlasException("AtlasMappingUri must be specified");
        }
        if (getMappingService() == null) {
            throw new AtlasException("AtlasMappingService is not set");
        }
        DefaultAtlasContext context = new DefaultAtlasContext(this, atlasMappingUri, format);
        context.init();
        return context;
    }

    public AtlasContext createContext(AtlasMapping mapping) throws AtlasException {
        DefaultAtlasContext context = new DefaultAtlasContext(this, mapping);
        context.init();
        return context;
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getThreadName() {
        return this.threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public String getVersion() {
        return this.getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public ObjectName getJmxObjectName() {
        return this.objectName;
    }

    public AtlasMappingService getMappingService() {
        return this.atlasMappingService;
    }

    public void setMappingService(AtlasMappingService atlasMappingService) {
        this.atlasMappingService = atlasMappingService;
    }

    public AtlasModuleInfoRegistry getModuleInfoRegistry() {
        return this.moduleInfoRegistry;
    }

    public void setModuleInfoRegistry(AtlasModuleInfoRegistry registry) {
        this.moduleInfoRegistry = registry;
    }

    @Override
    public AtlasConversionService getConversionService() {
        return this.atlasConversionService;
    }

    @Override
    public AtlasFieldActionService getFieldActionService() {
        return this.atlasFieldActionService;
    }

    @Override
    public AtlasCombineStrategy getCombineStrategy() {
        return atlasCombineStrategy;
    }

    public void setCombineStrategy(AtlasCombineStrategy atlasCombineStrategy) {
        this.atlasCombineStrategy = atlasCombineStrategy;
    }

    @Override
    public AtlasPropertyStrategy getPropertyStrategy() {
        return atlasPropertyStrategy;
    }

    public void setPropertyStrategy(AtlasPropertyStrategy atlasPropertyStrategy) {
        this.atlasPropertyStrategy = atlasPropertyStrategy;
    }

    @Override
    public AtlasSeparateStrategy getSeparateStrategy() {
        return atlasSeparateStrategy;
    }

    public void setSeparateStrategy(AtlasSeparateStrategy atlasSeparateStrategy) {
        this.atlasSeparateStrategy = atlasSeparateStrategy;
    }

    @Override
    public AtlasValidationService getValidationService() {
        return atlasValidationService;
    }

    public void setValidationService(AtlasValidationService atlasValidationService) {
        this.atlasValidationService = atlasValidationService;
    }

    protected void loadModules(String moduleClassProperty, Class<?> moduleInterface) {
        Class<?> moduleClass = null;
        String moduleClassName = null;
        Set<String> serviceClasses = new HashSet<String>();

        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            Enumeration<URL> urls = classLoader.getResources("META-INF/services/atlas/module/atlas.module");
            while (urls.hasMoreElements()) {
                URL tmp = urls.nextElement();
                Properties prop = AtlasUtil.loadPropertiesFromURL(tmp);
                String serviceClassPropertyValue = (String) prop.get(moduleClassProperty);
                if (!AtlasUtil.isEmpty(serviceClassPropertyValue)) {
                    serviceClasses.add((serviceClassPropertyValue));
                }
            }
        } catch (Exception e) {
            LOG.warn("Error loading module resources", e);
        }

        for (String clazz : serviceClasses) {
            try {
                moduleClass = Class.forName(clazz);
                moduleClassName = moduleClass.getName();

                if (isClassAtlasModule(moduleClass, moduleInterface)) {
                    @SuppressWarnings("unchecked")
                    Class<AtlasModule> atlasModuleClass = (Class<AtlasModule>)moduleClass;
                    Constructor<AtlasModule> constructor = atlasModuleClass.getDeclaredConstructor();
                    if (constructor != null) {
                        AtlasModuleInfo module = new DefaultAtlasModuleInfo(getModuleName(moduleClass),
                                getModuleUri(moduleClass), atlasModuleClass, constructor,
                                getSupportedDataFormats(moduleClass), getConfigPackages(moduleClass));
                        getModuleInfoRegistry().register(module);
                    } else {
                        LOG.warn("Invalid module class " + moduleClassName + ": constructor is not present");
                    }
                } else {
                    LOG.warn("Invalid module class  " + moduleClassName + ": unsupported AtlasModule");
                }
            } catch (NoSuchMethodException e) {
                LOG.warn("Invalid module class " + moduleClassName + ": constructor is not present.", e);
            } catch (ClassNotFoundException e) {
                LOG.warn("Invalid module class " + moduleClassName + " not found in classLoader", e);
            } catch (Exception e) {
                LOG.warn("Invalid module class " + moduleClassName + " unknown error", e);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded: " + getModuleInfoRegistry().size() + " of " + serviceClasses.size() + " detected modules");
        }
    }

    protected void unloadModules() {
        int moduleCount = getModuleInfoRegistry().size();
        getModuleInfoRegistry().unregisterAll();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unloaded: " + moduleCount + " modules");
        }
    }

    protected boolean isClassAtlasModule(Class<?> clazz, Class<?> moduleInterface) {
        if (clazz == null) {
            return false;
        }

        if (isAtlasModuleInterface(clazz, moduleInterface) && clazz.isAnnotationPresent(AtlasModuleDetail.class)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        clazz.getCanonicalName() + " is a '" + moduleInterface.getSimpleName() + "' implementation");
            }
            return true;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    clazz.getCanonicalName() + " is NOT a '" + moduleInterface.getSimpleName() + "' implementation");
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

        return "UNDEFINED-" + UUID.randomUUID().toString();
    }

    protected List<String> getSupportedDataFormats(Class<?> clazz) {

        List<String> dataFormats = null;
        AtlasModuleDetail detail = clazz.getAnnotation(AtlasModuleDetail.class);

        if (detail != null) {
            dataFormats = new ArrayList<String>();

            String[] formats = detail.dataFormats();

            for (String format : formats) {
                dataFormats.add(format.trim());
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Module: " + clazz.getCanonicalName() + " supports data formats: " + dataFormats.toString());
        }

        return dataFormats;
    }

    protected List<String> getConfigPackages(Class<?> clazz) {

        List<String> configPackages = null;
        AtlasModuleDetail detail = clazz.getAnnotation(AtlasModuleDetail.class);

        if (detail != null) {
            configPackages = new ArrayList<String>();

            String[] packages = detail.configPackages();

            for (String pkg : packages) {
                configPackages.add(pkg.trim());
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Module: " + clazz.getCanonicalName() + " config packages: " + configPackages.toString());
        }

        return configPackages;
    }

    protected List<String> getAllModuleConfigPackages(AtlasModuleInfoRegistry registry) {
        List<String> pkgs = new ArrayList<String>();
        for (AtlasModuleInfo moduleInfo : registry.getAll()) {
            pkgs.addAll(Arrays.asList(moduleInfo.getPackageNames()));
        }
        return pkgs;
    }

    protected void registerFactoryJmx(DefaultAtlasContextFactory factory) {
        try {
            setObjectName(factory.uuid);
            ManagementFactory.getPlatformMBeanServer().registerMBean(factory, factory.getJmxObjectName());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registered AtlasContextFactory with JMX");
            }
        } catch (Exception e) {
            LOG.warn("Unable to resgister DefaultAtlasContextFactory with JMX", e);
        }
    }

    protected void setObjectName(String name) throws MalformedObjectNameException {
        String objectName = String.format("io.atlasmap:type=AtlasServiceFactory,factoryUuid=%s", getUuid());
        this.objectName = new ObjectName(objectName);
    }

    protected static DefaultAtlasContextFactory getFactory() {
        return factory;
    }

}
