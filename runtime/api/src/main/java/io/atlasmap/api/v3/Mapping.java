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
import java.util.NavigableSet;

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

    void moveTransformationBefore(Transformation transformation, Transformation beforeTransformation);

    void replaceTransformation(Transformation transformation, Transformation withTransformation);

    List<Transformation> transformations();

    NavigableSet<String> properties();
}
