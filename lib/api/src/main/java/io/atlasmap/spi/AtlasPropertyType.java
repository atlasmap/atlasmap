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

import java.util.Arrays;
import java.util.List;

/**
 * The type of the property.
 */
public enum AtlasPropertyType {
    /** Environment variables. */
    ENVIRONMENT_VARIABLES("EnvironmentVariables"),
    /** Java system properties. */
    JAVA_SYSTEM_PROPERTIES("SystemProperties"),
    /** Mapping defined properties. */
    MAPPING_DEFINED_PROPERTIES("MappingDefinedProperties"),
    /** Runtime properties. */
    RUNTIME_PROPERTIES("RuntimeProperties");

    private final String value;

    /**
     * A constructor.
     * @param value value
     */
    AtlasPropertyType(String value) {
        this.value = value;
    }

    /**
     * Gets the value.
     * @return value
     */
    public String value() {
        return this.value;
    }

    /**
     * Gets the enum from the value.
     * @param v value
     * @return the enum
     */
    public static AtlasPropertyType fromValue(String v) {
        for (AtlasPropertyType c : AtlasPropertyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    /**
     * Gets a list of property type enum.
     * @return a list of property type enum
     */
    public static List<AtlasPropertyType> getAll() {
        return Arrays.asList(ENVIRONMENT_VARIABLES, JAVA_SYSTEM_PROPERTIES, MAPPING_DEFINED_PROPERTIES,
                RUNTIME_PROPERTIES);
    }

    /**
     * Gets a list of property type enum values.
     * @return a list of property type enum values
     */
    public static List<String> getAllValues() {
        return Arrays.asList(ENVIRONMENT_VARIABLES.value(), JAVA_SYSTEM_PROPERTIES.value(),
                MAPPING_DEFINED_PROPERTIES.value(), RUNTIME_PROPERTIES.value());
    }
}
