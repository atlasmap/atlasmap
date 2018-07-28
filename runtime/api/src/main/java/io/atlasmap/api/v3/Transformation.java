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

import java.util.List;

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

    boolean complete();

    /**
     *
     */
    public interface Descriptor {

        String name();

        String description();
    }
}
