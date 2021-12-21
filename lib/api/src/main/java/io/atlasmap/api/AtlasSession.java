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

import java.util.Map;

import io.atlasmap.spi.AtlasPropertyStrategy;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Validations;

/**
 * Represents mapping processing session at runtime. This is supposed to be created
 * for each execution through {@link AtlasContext}.
 */
public interface AtlasSession {

    /**
     * Gets the properties.
     * @return properties
     */
    @Deprecated
    Map<String, Object> getProperties();

    /**
     * Gets the source properties.
     * @return source properties
     */
    Map<String, Object> getSourceProperties();

    /**
     * Gets the target properties.
     * @return target properties
     */
    Map<String, Object> getTargetProperties();

    /**
     * Gets the property strategy.
     * @return property strategy
     */
    AtlasPropertyStrategy getAtlasPropertyStrategy();

    /**
     * Sets the property strategy.
     * @param strategy property strategy
     */
    void setAtlasPropertyStrategy(AtlasPropertyStrategy strategy);

    /**
     * Gets the associated {@link AtlasContext}.
     * @return {@link AtlasContext}
     */
    AtlasContext getAtlasContext();

    /**
     * Sets the {@link AtlasContext}.
     * @param atlasContext {@link AtlasContext}
     */
    void setAtlasContext(AtlasContext atlasContext);

    /**
     * Gets the {@link AtlasMapping} associated with this session.
     * @return {@link AtlasMapping}
     */
    AtlasMapping getMapping();

    /**
     * Gets the default source Document.
     * @return default source Document
     */
    Object getDefaultSourceDocument();

    /**
     * Sets the default source Document.
     * @param sourceDoc default source Document
     */
    void setDefaultSourceDocument(Object sourceDoc);

    /**
     * Gets the source Document associated with the specified Document ID.
     * @param docId Document ID
     * @return source Document
     */
    Object getSourceDocument(String docId);

    /**
     * Sets the source Document.
     * @param docId Document ID
     * @param sourceDoc source Document
     */
    void setSourceDocument(String docId, Object sourceDoc);

    /**
     * Gets if there's a target Document associated with the specified Document ID.
     * @param docId Document ID
     * @return true if the source Document exits, or false
     */
    boolean hasSourceDocument(String docId);

    /**
     * Gets the source Document map.
     * @return source Document map
     */
    Map<String, Object> getSourceDocumentMap();

    /**
     * Gets the default target Document.
     * @return default target Document.
     */
    Object getDefaultTargetDocument();

    /**
     * Sets the default target Document.
     * @param targetDoc default target Document
     */
    void setDefaultTargetDocument(Object targetDoc);

    /**
     * Gets the target Document associated with the specified Document ID.
     * @param docId Document ID
     * @return target Document
     */
    Object getTargetDocument(String docId);

    /**
     * Sets the target Document.
     * @param docId Document ID
     * @param targetDoc target Document
     */
    void setTargetDocument(String docId, Object targetDoc);

    /**
     * Gets if there's a target Document associated with the specified Document ID.
     * @param docId Document ID
     * @return true if the target Document exits, or false
     */
    boolean hasTargetDocument(String docId);

    /**
     * Gets the target Document map.
     * @return target Document map.
     */
    Map<String, Object> getTargetDocumentMap();

    /**
     * Gets the validations.
     * @return validations
     */
    Validations getValidations();

    /**
     * Sets the validations.
     * @param validations validations
     */
    void setValidations(Validations validations);

    /**
     * Gets a list of {@link io.atlasmap.v2.Audit}.
     * @return a list of audits
     */
    Audits getAudits();

    /**
     * Sets a list of {@link io.atlasmap.v2.Audit}.
     * @param audits a list of audits
     */
    void setAudits(Audits audits);

    /**
     * Gets if there's any error recorded on this session.
     * @return true if there's any error, or false
     */
    boolean hasErrors();

    /**
     * Gets if there's any warning recorded on this session.
     * @return true if there's any warning, or false
     */
    boolean hasWarns();

    /**
     * Gets the number of errors recorded on this session.
     * @return the number of the errors
     */
    Integer errorCount();

    /**
     * Gets the number of warnings recoreded on this session.
     * @return the number of the warnings
     */
    Integer warnCount();
}
