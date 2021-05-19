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
package io.atlasmap.v2;

public enum ValidationScope {

    ALL("All"),
    DATA_SOURCE("DataSource"),
    MAPPING("Mapping"),
    LOOKUP_TABLE("LookupTable"),
    CONSTANT("Constant"),
    PROPERTY("Property");

    private final String value;

    ValidationScope(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ValidationScope fromValue(String v) {
        for (ValidationScope c: ValidationScope.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
