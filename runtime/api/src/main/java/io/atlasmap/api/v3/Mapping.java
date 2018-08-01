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
import java.util.Map;
import java.util.Set;

import io.atlasmap.api.v3.Transformation.Descriptor;

/**
 *
 */
public interface Mapping {

    String name();

    void setName(String name);

    String description();

    void setDescription(String description);

    Transformation addTransformation(Descriptor descriptor);

    void removeTransformation(Transformation transformation);

    /**
     * @param movingTransformation
     * @param transformation
     */
    void moveTransformationBefore(Transformation movingTransformation, Transformation transformation);

    /**
     * @param movingTransformation
     * @param transformation
     */
    void moveTransformationAfter(Transformation movingTransformation, Transformation transformation);

    Transformation replaceTransformation(Transformation transformation, Descriptor descriptor);

    List<Transformation> transformations();

    Collection<String> outputPropertyNames();

    Map<String, Set<Parameter>> dependentParametersByOutputProperty();

    /**
     * @return all messages associated with this mapping
     */
    Collection<Message> messages();

    /**
     * @return <code>true</code> if this mapping has any errors
     */
    boolean hasErrors();

    /**
     * @return <code>true</code> if this mapping has any warnings
     */
    boolean hasWarnings();
}
