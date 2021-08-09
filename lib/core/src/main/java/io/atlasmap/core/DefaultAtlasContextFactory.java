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
package io.atlasmap.core;

import java.io.File;
import java.io.InputStream;
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

import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationService;
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
    private boolean initialized = false;
    private String uuid = null;
    private String threadName = null;
    private ObjectName objectName = null;
    private DefaultAtlasConversionService atlasConversionService = null;
    private DefaultAtlasFieldActionService atlasFieldActionService = null;
    private AtlasCombineStrategy atlasCombineStrategy = null;
    private AtlasPropertyStrategy atlasPropertyStrategy = null;
    private AtlasSeparateStrategy atlasSeparateStrategy = null;
    private AtlasValidationService atlasValidationService = null;
    private AtlasModuleInfoRegistry moduleInfoRegistry;
    private Map<String, String> properties = null;
    private CompoundClassLoader classLoader = null;

    private DefaultAtlasContextFactory() {
    }

    public static DefaultAtlasContextFactory getInstance() {
        return getInstance(true);
    }

    /**
     * Returns the default singleton, possibly creating it if necessary.
     *
     * @param init if {@code true} the newly created {@link DefaultAtlasContextFactory} will be initialized upon
     *            creation via {@link #init()}; otherwise, the newly created {@link DefaultAtlasContextFactory} will not
     *            be initialized upon creation via {@link #init()}
     * @return the singleton
     */
    public static DefaultAtlasContextFactory getInstance(boolean init) {
        if (factory == null) {
            factory = new DefaultAtlasContextFactory();
            if (init) {
                factory.init();
            }
        }
        return factory;
    }

    @Override
    public synchronized void init() {
        CompoundClassLoader cl = new DefaultAtlasCompoundClassLoader();
        cl.addAlternativeLoader(AtlasMapping.class.getClassLoader());
        init(cl);
    }

    public synchronized void init(CompoundClassLoader cl) {
        if (this.initialized) {
            return;
        }

        this.uuid = UUID.randomUUID().toString();
        this.threadName = Thread.currentThread().getName();
        this.classLoader = cl;
        try {
            this.properties = new HashMap<>();
            Properties props = new Properties();
            props.load(this.getClass().getClassLoader().getResourceAsStream("atlasmap.properties"));
            String version = props.getProperty(PROPERTY_ATLASMAP_CORE_VERSION);
            this.properties.put(PROPERTY_ATLASMAP_CORE_VERSION, version);
        } catch (Exception e) {
            LOG.debug("Failed to read atlasmap.properties", e);
        }

        this.atlasConversionService = DefaultAtlasConversionService.getInstance();
        this.atlasFieldActionService = DefaultAtlasFieldActionService.getInstance();
        this.atlasFieldActionService.init(this.classLoader);
        this.atlasCombineStrategy = new DefaultAtlasCombineStrategy();
        this.atlasPropertyStrategy = new DefaultAtlasPropertyStrategy();
        this.atlasSeparateStrategy = new DefaultAtlasSeparateStrategy();
        this.atlasValidationService = new DefaultAtlasValidationService();
        registerFactoryJmx(this);
        this.moduleInfoRegistry = new DefaultAtlasModuleInfoRegistry(this);
        loadModules("moduleClass", AtlasModule.class);
        this.initialized = true;

    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public void setProperties(Properties properties) {
        this.properties = new HashMap<>();
        properties.forEach((key, value) -> this.properties.put(key.toString(), value.toString()));
    }

    @Override
    public Map<String, String> getProperties() {
        return this.properties;
    }

    @Override
    public synchronized void destroy() {
        if (!this.initialized) {
            return;
        }

        unloadModules();

        try {
            if (ManagementFactory.getPlatformMBeanServer().isRegistered(getJmxObjectName())) {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(getJmxObjectName());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unregistered AtlasContextFactory with JMX");
                }
            }
        } catch (Exception e) {
            LOG.warn("Unable to unregister with JMX", e);
        }

        this.uuid = null;
        this.objectName = null;
        this.properties = null;
        this.atlasFieldActionService = null;
        this.atlasConversionService = null;
        this.atlasPropertyStrategy = null;
        this.atlasCombineStrategy = null;
        this.atlasSeparateStrategy = null;
        this.atlasValidationService = null;
        this.moduleInfoRegistry = null;
        this.classLoader = null;
        this.threadName = null;
        this.initialized = false;
    }

    @Override
    public DefaultAtlasContext createContext(File atlasMappingFile) throws AtlasException {
        if (atlasMappingFile == null) {
            throw new AtlasException("AtlasMappingFile must be specified");
        }

        return createContext(atlasMappingFile.toURI());
    }

    @Override
    public DefaultAtlasContext createContext(URI atlasMappingUri) throws AtlasException {
        if (atlasMappingUri == null) {
            throw new AtlasException("AtlasMappingUri must be specified");
        }
        DefaultAtlasContext context = new DefaultAtlasContext(this, atlasMappingUri);
        return context;
    }

    public DefaultAtlasContext createContext(AtlasMapping mapping) throws AtlasException {
        DefaultAtlasContext context = new DefaultAtlasContext(this, mapping);
        return context;
    }

    @Override
    public DefaultAtlasContext createContext(Format format, InputStream stream) throws AtlasException {
        DefaultAtlasContext context = new DefaultAtlasContext(this, format, stream);
        return context;
    }

    @Override
    public DefaultAtlasPreviewContext createPreviewContext() {
        DefaultAtlasPreviewContext context = new DefaultAtlasPreviewContext(this);
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

    public AtlasModuleInfoRegistry getModuleInfoRegistry() {
        return this.moduleInfoRegistry;
    }

    public void setModuleInfoRegistry(AtlasModuleInfoRegistry registry) {
        this.moduleInfoRegistry = registry;
    }

    @Override
    public DefaultAtlasConversionService getConversionService() {
        return this.atlasConversionService;
    }

    @Override
    public DefaultAtlasFieldActionService getFieldActionService() {
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

    @Override
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

    public CompoundClassLoader getClassLoader() {
        return this.classLoader;
    }

    @Override
    public void addClassLoader(ClassLoader cl) {
        this.classLoader.addAlternativeLoader(cl);
    }

    public void setClassLoader(CompoundClassLoader cl) {
        this.classLoader = cl;
    }

    protected void loadModules(String moduleClassProperty, Class<?> moduleInterface) {
        Class<?> moduleClass = null;
        String moduleClassName = null;
        Set<String> serviceClasses = new HashSet<>();

        try {
            Enumeration<URL> urls = classLoader.getResources("META-INF/services/atlas/module/atlas.module");
            while (urls.hasMoreElements()) {
                URL tmp = urls.nextElement();
                Properties prop = AtlasUtil.loadPropertiesFromURL(tmp);
                String serviceClassPropertyValue = (String) prop.get(moduleClassProperty);
                String[] splitted = serviceClassPropertyValue != null ? serviceClassPropertyValue.split(",") : new String[0];
                for (String entry : splitted) {
                    if (!AtlasUtil.isEmpty(entry)) {
                        serviceClasses.add((entry));
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Error loading module resources", e);
        }

        for (String clazz : serviceClasses) {
            try {
                moduleClass = classLoader.loadClass(clazz);
                moduleClassName = moduleClass.getName();

                if (isClassAtlasModule(moduleClass, moduleInterface)) {
                    @SuppressWarnings("unchecked")
                    Class<AtlasModule> atlasModuleClass = (Class<AtlasModule>) moduleClass;
                    Constructor<AtlasModule> constructor = atlasModuleClass.getDeclaredConstructor();
                    if (constructor != null) {
                        AtlasModuleInfo module = new DefaultAtlasModuleInfo(getModuleName(moduleClass),
                                getModuleUri(moduleClass), atlasModuleClass, constructor,
                                getSupportedDataFormats(moduleClass), getConfigPackages(moduleClass));
                        getModuleInfoRegistry().register(module);
                    } else {
                        LOG.warn("Invalid module class {}: constructor is not present", moduleClassName);
                    }
                } else {
                    LOG.warn("Invalid module class {}: unsupported AtlasModule", moduleClassName);
                }
            } catch (NoSuchMethodException e) {
                LOG.warn(String.format("Invalid module class %s: constructor is not present.", moduleClassName), e);
            } catch (ClassNotFoundException e) {
                LOG.warn(String.format("Invalid module class %s: not found in classLoader.", moduleClassName), e);
            } catch (Exception e) {
                LOG.warn(String.format("Invalid module class %s: unknown error.", moduleClassName), e);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded: {} of {} detected modules", getModuleInfoRegistry().size(), serviceClasses.size());
        }
    }

    protected void unloadModules() {
        if (getModuleInfoRegistry() == null) {
            return;
        }
        int moduleCount = getModuleInfoRegistry().size();
        getModuleInfoRegistry().unregisterAll();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unloaded: {} modules", moduleCount);
        }
    }

    protected boolean isClassAtlasModule(Class<?> clazz, Class<?> moduleInterface) {
        if (clazz == null) {
            return false;
        }

        if (isAtlasModuleInterface(clazz, moduleInterface) && clazz.isAnnotationPresent(AtlasModuleDetail.class)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} is a '{}' implementation", clazz.getCanonicalName(), moduleInterface.getSimpleName());
            }
            return true;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} is NOT a '{}' implementation", clazz.getCanonicalName(), moduleInterface.getSimpleName());
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
            dataFormats = new ArrayList<>();

            String[] formats = detail.dataFormats();

            for (String format : formats) {
                dataFormats.add(format.trim());
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Module: {} supports data formats: {}", clazz.getCanonicalName(), dataFormats);
        }

        return dataFormats;
    }

    protected List<String> getConfigPackages(Class<?> clazz) {

        List<String> configPackages = null;
        AtlasModuleDetail detail = clazz.getAnnotation(AtlasModuleDetail.class);

        if (detail != null) {
            configPackages = new ArrayList<>();

            String[] packages = detail.configPackages();

            for (String pkg : packages) {
                configPackages.add(pkg.trim());
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Module: {} config packages: {}", clazz.getCanonicalName(), configPackages);
        }

        return configPackages;
    }

    protected List<String> getAllModuleConfigPackages(AtlasModuleInfoRegistry registry) {
        List<String> pkgs = new ArrayList<>();
        for (AtlasModuleInfo moduleInfo : registry.getAll()) {
            pkgs.addAll(Arrays.asList(moduleInfo.getPackageNames()));
        }
        return pkgs;
    }

    protected void registerFactoryJmx(DefaultAtlasContextFactory factory) {
        if (factory == null) {
            return;
        }
        try {
            factory.setObjectName();
            if (!ManagementFactory.getPlatformMBeanServer().isRegistered(factory.getJmxObjectName())) {
                ManagementFactory.getPlatformMBeanServer().registerMBean(factory, factory.getJmxObjectName());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Registered AtlasContextFactory with JMX");
                }
            }
        } catch (Exception e) {
            LOG.warn("Unable to resgister DefaultAtlasContextFactory with JMX", e);
        }
    }

    protected void setObjectName() throws MalformedObjectNameException {
        this.objectName = new ObjectName(String.format("io.atlasmap:type=AtlasServiceFactory,factoryUuid=%s", getUuid()));
    }

}
