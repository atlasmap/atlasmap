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

import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;

/**
 * The internal version of {@link AtlasSession} which provides extended access
 * to the internal AtlasMap components.
 */
public interface AtlasInternalSession extends AtlasSession {

    /**
     * Gets the field reader associated with this session.
     * @param docId Document ID
     * @return field reader
     */
    AtlasFieldReader getFieldReader(String docId);

    /**
     * Gets the field reader associated with this session.
     * @param <T> the type of the field reader
     * @param docId Document ID
     * @param clazz the type of the field reader
     * @return field reader
     */
    <T extends AtlasFieldReader> T getFieldReader(String docId, Class<T> clazz);

    /**
     * Sets the field reader to this session.
     * @param docId Document ID
     * @param reader field reader to set
     */
    void setFieldReader(String docId, AtlasFieldReader reader);

    /**
     * Removes the field reader from this session.
     * @param docId Document ID
     * @return removed field reader
     */
    AtlasFieldReader removeFieldReader(String docId);

    /**
     * Gets the field writer associated with this session.
     * @param docId Document ID
     * @return field writer
     */
    AtlasFieldWriter getFieldWriter(String docId);

    /**
     * Gets the field writer associated with this session.
     * @param <T> the type of the field writer
     * @param docId Document ID
     * @param clazz the type of the field writer
     * @return field writer
     */
    <T extends AtlasFieldWriter> T getFieldWriter(String docId, Class<T> clazz);

    /**
     * Sets the field writer to this session.
     * @param docId Document ID
     * @param writer field writer
     */
    void setFieldWriter(String docId, AtlasFieldWriter writer);

    /**
     * Removes the field writer from this session.
     * @param docId Document ID
     * @return field writer
     */
    AtlasFieldWriter removeFieldWriter(String docId);

    /**
     * Looks up an {@link AtlasModule} associated with the specified Document ID.
     * @param docId Document ID
     * @return module
     */
    AtlasModule resolveModule(String docId);

    /**
     * Gets the current {@link Head}.
     * @return head
     */
    Head head();

    /**
     * The {@link Head} object represents what is under processing right now.
     * {@link io.atlasmap.api.AtlasContext} processes each mapping contained in {@link io.atlasmap.v2.AtlasMapping}
     * one-by-one and this {@link Head} represents which one of {@link Mapping} is under processing,
     * as well as source field and target field.
     */
    public interface Head {

        /**
         * Gets the mapping entry.
         * @return mapping
         */
        Mapping getMapping();

        /**
         * Gets the source field.
         * @return source field
         */
        Field getSourceField();

        /**
         * Gets the target field.
         * @return target field
         */
        Field getTargetField();

        /**
         * Gets the lookup table.
         * @return lookup table
         */
        LookupTable getLookupTable();

        /**
         * Sets the mapping entry.
         * @param mapping mapping
         * @return this object
         */
        Head setMapping(Mapping mapping);

        /**
         * Sets the lookup table.
         * @param table lookup table
         * @return this object
         */
        Head setLookupTable(LookupTable table);

        /**
         * Sets the source field.
         * @param sourceField source field
         * @return this object
         */
        Head setSourceField(Field sourceField);

        /**
         * Sets the target field.
         * @param targetField target field
         * @return this object
         */
        Head setTargetField(Field targetField);

        /**
         * Reset/Clear this head object.
         * @return this object
         */
        Head unset();

        /**
         * Check if there's any error in Audit.
         * @return true if there's any error, or false
         */
        boolean hasError();

        /**
         * Adds an audit entry.
         * @param status audit status
         * @param field affected field
         * @param message message
         * @return this object
         */
        Head addAudit(AuditStatus status, Field field, String message);

        /**
         * Gets a list of {@link Audit}.
         * @return a list of audit
         */
        List<Audit> getAudits();

    }

}
