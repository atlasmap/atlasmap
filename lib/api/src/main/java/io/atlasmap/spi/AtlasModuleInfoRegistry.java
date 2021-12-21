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

import java.util.Set;

/**
 * The runtime registry of {@link AtlasModuleInfo}. {@link io.atlasmap.api.AtlasContext} builds this registry
 * to store module metadata which corresponds to the DataSource/Document defined in the
 * mapping definition.
 */
public interface AtlasModuleInfoRegistry {

    /**
     * Looks up the module info by URI.
     * @param dataFormat URI
     * @return module info
     */
    AtlasModuleInfo lookupByUri(String dataFormat);

    /**
     * Gets all module info registered.
     * @return a list of module info
     */
    Set<AtlasModuleInfo> getAll();

    /**
     * Registers the module info.
     * @param module module info
     */
    void register(AtlasModuleInfo module);

    /**
     * Gets the number of the module info registered.
     * @return the number of the module info registered
     */
    int size();

    /**
     * Unregister all module info from this registry.
     */
    void unregisterAll();

}
