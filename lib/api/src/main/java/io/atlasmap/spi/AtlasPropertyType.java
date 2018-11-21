/**
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

public enum AtlasPropertyType {
    ENVIRONMENT_VARIABLES("EnvironmentVariables"), JAVA_SYSTEM_PROPERTIES(
            "SystemProperties"), MAPPING_DEFINED_PROPERTIES(
                    "MappingDefinedProperties"), RUNTIME_PROPERTIES("RuntimeProperties");

    private final String value;

    private AtlasPropertyType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static AtlasPropertyType fromValue(String v) {
        for (AtlasPropertyType c : AtlasPropertyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    public static List<AtlasPropertyType> getAll() {
        return Arrays.asList(ENVIRONMENT_VARIABLES, JAVA_SYSTEM_PROPERTIES, MAPPING_DEFINED_PROPERTIES,
                RUNTIME_PROPERTIES);
    }

    public static List<String> getAllValues() {
        return Arrays.asList(ENVIRONMENT_VARIABLES.value(), JAVA_SYSTEM_PROPERTIES.value(),
                MAPPING_DEFINED_PROPERTIES.value(), RUNTIME_PROPERTIES.value());
    }
}
