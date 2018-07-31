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
package io.atlasmap.api.v3;

import java.util.Set;

import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public interface Parameter {

    String name();

    boolean valueRequired();

    String stringValue();

    void setStringValue(String value) throws AtlasException;

    String description();

    Role role();

    Object value();

    boolean cloneable();

    boolean cloned();

    Transformation transformation();

    /**
     * @return all messages associated with this parameter
     */
    Set<Message> messages();

    /**
     * @return <code>true</code> if this parameter has any errors
     */
    boolean hasErrors();

    /**
     * @return <code>true</code> if this parameter has any warnings
     */
    boolean hasWarnings();

    /**
     *
     */
    public enum Role {
        INPUT,
        OUTPUT
    }
}
