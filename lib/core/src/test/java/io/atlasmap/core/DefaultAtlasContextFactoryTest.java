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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.atlasmap.spi.AtlasCollectionHelper;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasCombineStrategy;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.AtlasModuleDetail;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.DataSourceMetadata;
import io.atlasmap.v2.Field;

public class DefaultAtlasContextFactoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasContextFactoryTest.class);
    private DefaultAtlasContextFactory factory = null;

    @Test
    public void testInitDestroy() {
        factory = DefaultAtlasContextFactory.getInstance();
        factory.init();

        assertNotNull(factory);
        assertNotNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNotNull(factory.getUuid());
        assertNotNull(factory.getJmxObjectName());
        assertNotNull(factory.getMappingService());
        assertNotNull(factory.getModuleInfoRegistry());

        factory.destroy();
        assertNotNull(factory);
        assertNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNull(factory.getUuid());
        assertNull(factory.getJmxObjectName());
        assertNull(factory.getMappingService());
        assertNull(factory.getModuleInfoRegistry());
    }

    @Test
    public void testInitDestroyInitDestroy() {
        factory = DefaultAtlasContextFactory.getInstance();
        factory.init();
        String origUuid = factory.getUuid();

        assertNotNull(factory);
        assertNotNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNotNull(factory.getUuid());
        assertNotNull(factory.getJmxObjectName());
        assertNotNull(factory.getMappingService());
        assertNotNull(factory.getModuleInfoRegistry());

        factory.destroy();
        assertNotNull(factory);
        assertNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNull(factory.getUuid());
        assertNull(factory.getJmxObjectName());
        assertNull(factory.getMappingService());
        assertNull(factory.getModuleInfoRegistry());

        factory.init();
        assertNotNull(factory);
        assertNotNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNotNull(factory.getUuid());
        assertNotNull(factory.getJmxObjectName());
        assertNotNull(factory.getMappingService());
        assertNotNull(factory.getModuleInfoRegistry());
        assertNotEquals(origUuid, factory.getUuid());

        factory.destroy();
        assertNotNull(factory);
        assertNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNull(factory.getUuid());
        assertNull(factory.getJmxObjectName());
        assertNull(factory.getMappingService());
        assertNull(factory.getModuleInfoRegistry());
    }

    @Test
    public void testStaticFactoryInitDestroy() {
        factory = DefaultAtlasContextFactory.getInstance();
        factory.init();
        assertNotNull(factory);
        assertNotNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNotNull(factory.getUuid());
        assertNotNull(factory.getJmxObjectName());
        assertNotNull(factory.getMappingService());
        assertNotNull(factory.getModuleInfoRegistry());

        factory.destroy();
        assertNotNull(factory);
        assertNull(factory.getThreadName());
        assertEquals("io.atlasmap.core.DefaultAtlasContextFactory", factory.getClassName());
        assertNull(factory.getUuid());
        assertNull(factory.getJmxObjectName());
        assertNull(factory.getMappingService());
        assertNull(factory.getModuleInfoRegistry());
    }

    @Test
    public void testDefaultAtlasContextFactoryProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        DefaultAtlasContextFactory contextFactory = DefaultAtlasContextFactory.getInstance();
        contextFactory.init();
        contextFactory.setProperties(properties);
        contextFactory.setThreadName("threadName");
        contextFactory.setModuleInfoRegistry(null);
        contextFactory.setCombineStrategy(null);
        contextFactory.setPropertyStrategy(null);
        contextFactory.setSeparateStrategy(null);
        contextFactory.setValidationService(null);
        assertNull(contextFactory.getCombineStrategy());
        assertNull(contextFactory.getPropertyStrategy());
        assertNull(contextFactory.getSeparateStrategy());
        assertNull(contextFactory.getValidationService());
        assertNull(contextFactory.getVersion());
        assertNotNull(contextFactory);
        assertNotNull(contextFactory.getProperties());
        AtlasCombineStrategy strategy = new TemplateCombineStrategy();
        contextFactory.setCombineStrategy(strategy);
        assertSame(strategy, contextFactory.getCombineStrategy());
        contextFactory.destroy();
    }

    @Test
    public void testCreateContextWithFile() throws AtlasException {
        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json")
                .toFile();
        factory = DefaultAtlasContextFactory.getInstance();
        factory.init();
        assertNotNull(factory.createContext(file));
    }

    @Test
    public void testCreateContextWithAtlasMapping() throws AtlasException {
        AtlasMapping atlasMapping = new AtlasMapping();
        factory = DefaultAtlasContextFactory.getInstance();
        factory.init();
        assertNotNull(factory.createContext(atlasMapping));
    }

    @Test(expected = AtlasException.class)
    public void testCreateContextWithFileAtlasMappingFormat() throws AtlasException {
        factory = DefaultAtlasContextFactory.getInstance();
        factory.init();
        File file = null;
        assertNotNull(factory.createContext(file));
    }

    @Test
    public void testCreateContextWithURI() throws AtlasException {
        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json")
                .toFile();
        factory = DefaultAtlasContextFactory.getInstance();
        factory.init();
        assertNotNull(factory.createContext(file.toURI()));
    }

    @Test(expected = AtlasException.class)
    public void testCreateContextWithURIAtlasExceptionNoUri() throws AtlasException {
        factory = DefaultAtlasContextFactory.getInstance();
        factory.init();
        URI uri = null;
        assertNotNull(factory.createContext(uri));
    }

    @Test
    public void testIsClassAtlasModule() {
        factory = DefaultAtlasContextFactory.getInstance();
        assertFalse(factory.isClassAtlasModule(null, null));

        assertFalse(factory.isClassAtlasModule(AtlasModuleDetail.class, AtlasModule.class));

        assertFalse(factory.isAtlasModuleInterface(null, null));
    }

    @Test
    public void testGetModuleName() {
        factory = DefaultAtlasContextFactory.getInstance();
        assertNotNull(factory.getModuleName(Object.class));
    }

    @Test
    public void testRegisterFactoryJmx() {
        factory = DefaultAtlasContextFactory.getInstance();
        factory.registerFactoryJmx(null);
    }

    @Test
    public void testGetModuleUri() {
        factory = DefaultAtlasContextFactory.getInstance();
        assertEquals("UNDEFINED", factory.getModuleUri(Object.class));
    }

    @Test
    public void testGetConfigPackages() {
        factory = DefaultAtlasContextFactory.getInstance();

        assertNotNull(factory.getConfigPackages(MockModule.class));
    }

    @Test
    public void testGetSupportedDataFormats() {
        factory = DefaultAtlasContextFactory.getInstance();

        assertNotNull(factory.getSupportedDataFormats(MockModule.class));
    }

    @AtlasModuleDetail(name = "ConstantModule", uri = "", modes = { "SOURCE" }, dataFormats = { "xml",
            "json" }, configPackages = { "io.atlasmap.core" })
    private class MockModule implements AtlasModule {

        private ClassLoader classLoader;

        @Override
        public void init() {
            LOG.debug("init method");
        }

        @Override
        public void destroy() {
            LOG.debug("destroy method");
        }

        @Override
        public void setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public ClassLoader getClassLoader() {
            return this.classLoader;
        }

        @Override
        public void processPreValidation(AtlasInternalSession session) throws AtlasException {
            LOG.debug("processPreValidation method");
        }

        @Override
        public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
            LOG.debug("processPreSourceExecution method");
        }

        @Override
        public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
            LOG.debug("processPreTargetExecution method");
        }

        @Override
        public void readSourceValue(AtlasInternalSession session) throws AtlasException {
            LOG.debug("processSourceFieldMapping method");
        }

        @Override
        public void populateTargetField(AtlasInternalSession session) throws AtlasException {
            LOG.debug("populateTargetField method");
        }

        @Override
        public void writeTargetValue(AtlasInternalSession session) throws AtlasException {
            LOG.debug("processTargetFieldMapping method");
        }

        @Override
        public void processPostSourceExecution(AtlasInternalSession session) throws AtlasException {
            LOG.debug("processPostSourceExecution method");
        }

        @Override
        public void processPostTargetExecution(AtlasInternalSession session) throws AtlasException {
            LOG.debug("processPostTargetExecution method");
        }

        @Override
        public void processPostValidation(AtlasInternalSession session) throws AtlasException {
            LOG.debug("processPostValidation method");
        }

        @Override
        public AtlasModuleMode getMode() {
            return null;
        }

        @Override
        public void setMode(AtlasModuleMode atlasModuleMode) {
            LOG.debug("setMode method");
        }

        @Override
        public AtlasConversionService getConversionService() {
            return null;
        }

        @Override
        public void setConversionService(AtlasConversionService atlasConversionService) {
            LOG.debug("setConversionService method");
        }

        @Override
        public AtlasFieldActionService getFieldActionService() {
            return null;
        }

        @Override
        public AtlasCollectionHelper getCollectionHelper() {
            return null;
        }

        @Override
        public void setFieldActionService(AtlasFieldActionService atlasFieldActionService) {
            LOG.debug("setFieldActionService method");
        }

        @Override
        public List<AtlasModuleMode> listSupportedModes() {
            return null;
        }

        @Override
        public String getDocId() {
            return null;
        }

        @Override
        public void setDocId(String docId) {
            LOG.debug("setDocId method");
        }

        @Override
        public String getUri() {
            return null;
        }

        @Override
        public void setUri(String uri) {
            LOG.debug("setUri method");
        }

        @Override
        public Boolean isStatisticsSupported() {
            return null;
        }

        @Override
        public Boolean isStatisticsEnabled() {
            return null;
        }

        @Override
        public Boolean isSupportedField(Field field) {
            return null;
        }

        @Override
        public Field cloneField(Field field) throws AtlasException {
            return null;
        }

        @Override
        public String getUriDataType() {
            return null;
        }

        @Override
        public Map<String, String> getUriParameters() {
            return null;
        }

        @Override
        public void setDataSourceMetadata(DataSourceMetadata meta) {
        }

        @Override
        public DataSourceMetadata getDataSourceMetadata() {
            return null;
        }

    }
}
