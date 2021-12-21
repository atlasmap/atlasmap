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
package io.atlasmap.mxbean;

/**
 * The {@link io.atlasmap.spi.AtlasModuleInfo} MBean.
 */
public interface AtlasModuleInfoMXBean {

    /**
     * Gets the name.
     * @return name
     */
    String getName();

    /**
     * Gets the class name.
     * @return class name.
     */
    String getClassName();

    /**
     * Gets the module class name.
     * @return module class name
     */
    String getModuleClassName();

    /**
     * Gets the version.
     * @return version
     */
    String getVersion();

    /**
     * Gets the data formats.
     * @return data formats
     */
    String[] getDataFormats();

    /**
     * Gets the package names.
     * @return package names
     */
    String[] getPackageNames();

    /**
     * Gets if it supports to be a source Document.
     * @return true if supported
     */
    Boolean isSourceSupported();

    /**
     * Gets if it supports to be a target Document.
     * @return true if supported
     */
    Boolean isTargetSupported();

}
