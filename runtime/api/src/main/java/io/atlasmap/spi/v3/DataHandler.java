/**
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.atlasmap.spi.v3;

import io.atlasmap.api.v3.DocumentRole;
import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public abstract class DataHandler {

    private Object document;

    public abstract String[] supportedDataFormats();

    /**
     * @return The document roles supported by this module. The default is to support all roles.
     */
    public DocumentRole[] supportedRoles() {
        return null;
    }

    protected Object document() {
        return document;
    }

    /**
     * @param document
     */
    public void setDocument(Object document) {
        this.document = document;
    }

    /**
     * @param path
     * @return the value at <code>path</code>
     * @throws AtlasException if <code>path</code> is invalid
     */
    public abstract Object value(String path) throws AtlasException;

    /**
     * @param path
     * @param value
     * @throws AtlasException if <code>path</code> is invalid or <code>value</code> is invalid for the last field in <code>path</code>
     */
    public abstract void setValue(String path, Object value) throws AtlasException;
}
