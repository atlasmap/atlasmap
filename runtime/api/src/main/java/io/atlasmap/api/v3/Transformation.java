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

import java.util.Collection;
import java.util.List;

import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public interface Transformation {

    String name();

    String description();

    List<Parameter> parameters();

    /**
     * @param name
     * @return The parameter with the supplied name, or <code>null</code> if either there isn't exactly one parameter with that name
     */
    Parameter parameter(String name);

    Parameter cloneParameter(Parameter parameter);

    void removeClonedParameter(Parameter parameter) throws AtlasException;

    Mapping mapping();

    /**
     * @return all messages associated with this transformation
     */
    Collection<Message> messages();

    /**
     * @return <code>true</code> if this transformation has any errors
     */
    boolean hasErrors();

    /**
     * @return <code>true</code> if this transformation has any warnings
     */
    boolean hasWarnings();

    /**
     *
     */
    public interface Descriptor {

        String name();

        String description();
    }
}
