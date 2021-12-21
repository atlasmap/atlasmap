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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * AtlasModuleDetail adds metadata onto {@link AtlasModule}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AtlasModuleDetail {

    /**
     * Gets the name.
     * @return name
     */
    String name();

    /**
     * Gets the URI.
     * @return URI
     */
    String uri();

    /**
     * Gets a list of data formats.
     * @return a list of data formats
     */
    String[] dataFormats();

    /**
     * Gets the full package name of the corresponding model objects.
     * @return a list of the package names
     */
    String[] configPackages();

    /**
     * Gets a list of supported module modes.
     * @return a list of the supported module modes
     */
    String[] modes();
}
