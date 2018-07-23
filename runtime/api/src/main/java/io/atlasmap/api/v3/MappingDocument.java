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
import java.util.Set;

import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public interface MappingDocument {

    String[] availableDataFormats(DocumentRole role);

    void addDataDocument(String id, DocumentRole role, String dataFormat, Object document) throws AtlasException;

    void removeDataDocument(String id, DocumentRole role);

    Mapping addMapping();

    Mapping addMapping(String from, String to) throws AtlasException;

    void removeMapping(Mapping mapping);

    List<Mapping> mappings();

    Set<TransformationDescriptor> availableTransformationDescriptors();

    boolean autoSaves();

    MappingDocument setAutoSaves(boolean autoSaves);

    void save();
}
