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

/**
 * The factory class for {@link AtlasContext} and AtlasMap services.
 * Although {@link AtlasContext} could be created from a plain mapping definition JSON file,
 * it should be limited to test purpose only. The ADM archive file contains DataSource metadata
 * and some modules require it to achieve some features.
 */
public interface AtlasContextFactory {
    /** property key for atlasmap core version. */
    static final String PROPERTY_ATLASMAP_CORE_VERSION = "atlasmap.core.version";
    /** mapping definition format. */
    enum Format {
        /** ADM. */
        ADM,
        /** JSON. */
        JSON
    };

    /**
     * Initializes this factory.
     */
    void init();

    /**
     * Destroys this factory.
     */
    void destroy();

    /**
     * Creates {@link AtlasContext} from mapping definition JSON file.
     * This is for test purpose only. Instead, use {@link #createContext(Format, InputStream)} with an ADM archive file.
     * @param atlasMappingFile mapping definition JSON file
     * @return {@link AtlasContext}
     * @throws AtlasException unexpected error
     */
    AtlasContext createContext(File atlasMappingFile) throws AtlasException;

    /**
     * Creates {@link AtlasContext} from mapping definition JSON file.
     * This is for test purpose only. Instead, use {@link #createContext(Format, InputStream)} with an ADM archive file.
     * @param atlasMappingUri mapping definition JSON file
     * @return {@link AtlasContext}
     * @throws AtlasException unexpected error
     */
    AtlasContext createContext(URI atlasMappingUri) throws AtlasException;

    /**
     * Creates {@link AtlasContext}. Use {@link Format#ADM} other than for a simple test.
     * @param format mapping defiition format
     * @param atlasMappingStream ADM archive file or mapping definition JSON file
     * @return {@link AtlasContext}
     * @throws AtlasException unexpected error
     */
    AtlasContext createContext(Format format, InputStream atlasMappingStream) throws AtlasException;

    /**
     * Creates the preview context to process mapping preview.
     * @return preview context
     * @throws AtlasException unexpected error
     */
    AtlasPreviewContext createPreviewContext() throws AtlasException;

    /**
     * @deprecated .
     * @return strategy
     * @throws AtlasException unexpected error
     */
    @Deprecated
    AtlasCombineStrategy getCombineStrategy() throws AtlasException;

    /**
     * Gets the {@link AtlasConversionService}.
     * @return {@link AtlasConversionService}
     * @throws AtlasException unexpected error
     */
    AtlasConversionService getConversionService() throws AtlasException;

    /**
     * Gets the {@link AtlasFieldActionService}.
     * @return {@link AtlasFieldActionService}
     * @throws AtlasException unexpected error
     */
    AtlasFieldActionService getFieldActionService() throws AtlasException;

    /**
     * Gets the {@link AtlasPropertyStrategy}.
     * @return strategy
     * @throws AtlasException unexpected error
     */
    AtlasPropertyStrategy getPropertyStrategy() throws AtlasException;

    /**
     * Sets the {@link AtlasPropertyStrategy}.
     * @param strategy strategy
     * @throws AtlasException unexpected error
     */
    void setPropertyStrategy(AtlasPropertyStrategy strategy) throws AtlasException;

    /**
     * @deprecated .
     * @return strategy
     * @throws AtlasException unexpected error
     */
    @Deprecated
    AtlasSeparateStrategy getSeparateStrategy() throws AtlasException;

    /**
     * Gets the {@link AtlasValidationService}.
     * @return validation service
     * @throws AtlasException unexpected error
     */
    AtlasValidationService getValidationService() throws AtlasException;

    /**
     * Sets the properties.
     * @param properties properties
     */
    void setProperties(Map<String, String> properties);

    /**
     * Sets the properties.
     * @param properties properties
     */
    void setProperties(Properties properties);

    /**
     * Gets the properties.
     * @return properties
     */
    Map<String, String> getProperties();

    /**
     * Adds the class loader.
     * @param cl class loader.
     */
    void addClassLoader(ClassLoader cl);

}
