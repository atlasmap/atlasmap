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
package io.atlasmap.spi;

import java.util.List;
import java.util.Map;

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceMetadata;
import io.atlasmap.v2.Field;

/**
 * A SPI contract between AtlasMap core and modules. AtlasMap core engine invokes those
 * methods while processing mappings.
 * AtlasModule corresponds to each DataSource/Document one-by-one. It is created from the metadata
 * read from DataSource definition at runtime so that data format specific handling could be performed.
 */
public interface AtlasModule {

    /**
     * Initializes the module.
     * @throws AtlasException unexpected error
     */
    void init() throws AtlasException;

    /**
     * Destroys the module.
     * @throws AtlasException unexpected error
     */
    void destroy() throws AtlasException;

    /**
     * Sets the class loader.
     * @param classLoader class loader
     */
    void setClassLoader(ClassLoader classLoader);

    /**
     * Gets the class loader.
     * @return class loader
     */
    ClassLoader getClassLoader();

    /**
     * Processes pre-validation.
     * @param session session
     * @throws AtlasException unexpected error
     */
    void processPreValidation(AtlasInternalSession session) throws AtlasException;

    /**
     * Processes pre-source execution.
     * @param session session
     * @throws AtlasException unexpected error
     */
    void processPreSourceExecution(AtlasInternalSession session) throws AtlasException;

    /**
     * Processes pre-target execution.
     * @param session session
     * @throws AtlasException unexpected error
     */
    void processPreTargetExecution(AtlasInternalSession session) throws AtlasException;

    /**
     * Reads source field value from source document and store into source field object.
     *
     * @param session current session
     * @throws AtlasException failed to read source value
     */
    void readSourceValue(AtlasInternalSession session) throws AtlasException;

    /**
     * Populates target field value, usually by just copy from source field value.
     * Also apply type converters where it's needed.
     *
     * @param session current session
     * @throws AtlasException failed to populate target field value
     */
    void populateTargetField(AtlasInternalSession session) throws AtlasException;

    /**
     * Writes target field value into target document.
     *
     * @param session current session
     * @throws AtlasException faield to write target field value
     */
    void writeTargetValue(AtlasInternalSession session) throws AtlasException;

    /**
     * Processes post-source execution.
     * @param session session
     * @throws AtlasException unexpected error
     */
    void processPostSourceExecution(AtlasInternalSession session) throws AtlasException;

    /**
     * Processes post-target execution.
     * @param session session
     * @throws AtlasException unexpected error
     */
    void processPostTargetExecution(AtlasInternalSession session) throws AtlasException;

    /**
     * Processes post-validation.
     * @param session session
     * @throws AtlasException unexpected error
     */
    void processPostValidation(AtlasInternalSession session) throws AtlasException;

    /**
     * Gets the data source.
     * @return data source
     */
    DataSource getDataSource();

    /**
     * Sets the data source.
     * @param ds data source
     */
    void setDataSource(DataSource ds);

    /**
     * Gets the module mode.
     * @return module mode
     */
    AtlasModuleMode getMode();

    /**
     * Sets the module mode.
     * @param atlasModuleMode {@link AtlasModuleMode} to be set
     * @deprecated use {@link #setDataSource(DataSource)} instead.
     **/
    @Deprecated
    void setMode(AtlasModuleMode atlasModuleMode);

    /**
     * Gets the conversion service.
     * @return conversion service
     */
    AtlasConversionService getConversionService();

    /**
     * Sets the conversion service.
     * @param atlasConversionService conversion service
     */
    void setConversionService(AtlasConversionService atlasConversionService);

    /**
     * Gets the field action service.
     * @return field action service
     */
    AtlasFieldActionService getFieldActionService();

    /**
     * Sets the field action service.
     * @param atlasFieldActionService field action service
     */
    void setFieldActionService(AtlasFieldActionService atlasFieldActionService);

    /**
     * Gets the collection helper.
     * @return collection helper
     */
    AtlasCollectionHelper getCollectionHelper();

    /**
     * Gets a list of supported module mode.
     * @return a list of supported module mode
     */
    List<AtlasModuleMode> listSupportedModes();

    /**
     * Gets the Document ID.
     * @return Document ID
     */
    String getDocId();

    /**
     * Sets the Document ID.
     * @param docId Document ID to be set
     * @deprecated use {@link #setDataSource(DataSource)} instead.
     **/
    @Deprecated
    void setDocId(String docId);

    /**
     * Gets the Document name.
     * @return Document name.
     */
    String getDocName();

    /**
     * Sets the Document name.
     * @param docName Document name to be set
     * @deprecated use {@link #setDataSource(DataSource)} instead.
     **/
    @Deprecated
    void setDocName(String docName);

    /**
     * Gets the URI.
     * @return URI
     */
    String getUri();

    /**
     * Sets the URI.
     * @param uri URI string representation to be set
     * @deprecated use {@link #setDataSource(DataSource)} instead.
     **/
    @Deprecated
    void setUri(String uri);

    /**
     * Gets the data type in the URI.
     * @return data type
     */
    String getUriDataType();

    /**
     * Gets the URI parameters.
     * @return URI parameters
     */
    Map<String, String> getUriParameters();

    /**
     * Gets if statistics is supported.
     * @return true if statistics is supported, or false
     */
    Boolean isStatisticsSupported();

    /**
     * Gets if statistics is enabled.
     * @return true if statistics is enabled, or false
     */
    Boolean isStatisticsEnabled();

    /**
     * Check if the field is supported by this module.
     * @param field field
     * @return true if supported, or false
     */
    Boolean isSupportedField(Field field);

    /**
     * Clones the field.
     * @param field field to clone
     * @return cloned field
     * @throws AtlasException unexpected error
     */
    Field cloneField(Field field) throws AtlasException;

    /**
     * Sets the data source metadata.
     * @param meta data source metadata.
     */
    void setDataSourceMetadata(DataSourceMetadata meta);

    /**
     * Gets the data source metadata.
     * @return data sourec metadata
     */
    DataSourceMetadata getDataSourceMetadata();

    /**
     * Creates a field.
     * @return created field
     */
    Field createField();

}
