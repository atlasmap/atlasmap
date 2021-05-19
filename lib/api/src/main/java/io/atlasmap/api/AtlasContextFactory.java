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
package io.atlasmap.api;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import io.atlasmap.spi.AtlasCombineStrategy;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.spi.AtlasPropertyStrategy;
import io.atlasmap.spi.AtlasSeparateStrategy;

public interface AtlasContextFactory {

    static final String PROPERTY_ATLASMAP_CORE_VERSION = "atlasmap.core.version";

    enum Format { ADM, JSON };

    void init();

    void destroy();

    AtlasContext createContext(File atlasMappingFile) throws AtlasException;

    AtlasContext createContext(URI atlasMappingUri) throws AtlasException;

    AtlasContext createContext(Format format, InputStream atlasMappingStream) throws AtlasException;

    AtlasPreviewContext createPreviewContext() throws AtlasException;

    @Deprecated
    AtlasCombineStrategy getCombineStrategy() throws AtlasException;

    AtlasConversionService getConversionService() throws AtlasException;

    AtlasFieldActionService getFieldActionService() throws AtlasException;

    AtlasPropertyStrategy getPropertyStrategy() throws AtlasException;

    void setPropertyStrategy(AtlasPropertyStrategy strategy) throws AtlasException;

    @Deprecated
    AtlasSeparateStrategy getSeparateStrategy() throws AtlasException;

    AtlasValidationService getValidationService() throws AtlasException;

    void setProperties(Map<String, String> properties);

    void setProperties(Properties properties);

    Map<String, String> getProperties();

    void addClassLoader(ClassLoader cl);

}
