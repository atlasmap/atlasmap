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
package io.atlasmap.json.v2;

/**
 * @deprecated Migrate to {@code io.atlasmap.v2.InspectionType}
 */
@Deprecated
public enum InspectionType {

    ALL("All"),
    INSTANCE("Instance"),
    SCHEMA("Schema"),
    NONE("None");

    private final String value;

    InspectionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static InspectionType fromValue(String v) {
        for (InspectionType c: InspectionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
