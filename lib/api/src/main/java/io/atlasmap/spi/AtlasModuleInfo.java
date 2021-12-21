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

/**
 * Represents {@link AtlasModule} metadata.
 */
public interface AtlasModuleInfo {

    /**
     * Gets the name.
     * @return name
     */
    String getName();

    /**
     * Gets the URI.
     * @return URI
     */
    String getUri();

    /**
     * Gets the module class.
     * @return module class
     */
    Class<AtlasModule> getModuleClass();

    /**
     * Gets a list of the data formats.
     * @return a list of the data formats
     */
    String[] getDataFormats();

    /**
     * Gets the full package name of the corresponding model objects.
     * @return a list of package names
     */
    String[] getPackageNames();

    /**
     * Gets if this module supports to be the source Document.
     * @return true if source is supported, or false
     */
    Boolean isSourceSupported();

    /**
     * Gets if this module supports to be the target Document.
     * @return true if target is supported, or false
     */
    Boolean isTargetSupported();

}
