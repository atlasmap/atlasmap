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

import io.atlasmap.api.v3.MappingDocument.DataDocumentRole;
import io.atlasmap.api.v3.Message.Scope;
import io.atlasmap.api.v3.Message.Status;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public abstract class DataHandler {

    private Object document;
    private DataHandlerSupport support;

    public abstract String[] supportedDataFormats();

    /**
     * @return The document roles supported by this module. The default is to support all roles.
     */
    public DataDocumentRole[] supportedRoles() {
        return null;
    }

    /**
     * <strong>Warning:</strong> Must never be called by subclasses
     *
     * @param support
     */
    public void setSupport(DataHandlerSupport support) {
        this.support = support;
    }

    /**
     * <strong>Warning:</strong> Must never be called by subclasses
     *
     * @param document
     */
    public void setDocument(Object document) {
        this.document = document;
    }

    protected Object document() {
        return document;
    }

    /**
     * @param path
     * @return the value at <code>path</code>
     * @throws AtlasException if <code>path</code> is invalid
     */
    protected abstract Object value(String path) throws AtlasException;

    /**
     * @param path
     * @param value
     * @param parameter
     * @throws AtlasException if <code>path</code> is invalid or <code>value</code> is invalid for the last field in <code>path</code>
     */
    protected abstract void setValue(String path, Object value, Parameter parameter) throws AtlasException;

    protected void addMessage(Status status, Scope scope, Object context, String message, Object... arguments) {
        support.addMessage(status, scope, context, message, arguments);
    }

    void clearMessages(Parameter parameter) {
        support.clearMessages(Scope.DATA_HANDLER, parameter);
    }
}
