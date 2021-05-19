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
package io.atlasmap.java.v2;

public enum Modifier {

    ALL("All"),
    ABSTRACT("Abstract"),
    FINAL("Final"),
    INTERFACE("Interface"),
    NATIVE("Native"),
    PACKAGE_PRIVATE("Package Private"),
    PUBLIC("Public"),
    PROTECTED("Protected"),
    PRIVATE("Private"),
    STATIC("Static"),
    STRICT("Strict"),
    SYNCHRONIZED("Synchronized"),
    TRANSIENT("Transient"),
    VOLATILE("Volatile"),
    NONE("None");

    private final String value;

    Modifier(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Modifier fromValue(String v) {
        for (Modifier c: Modifier.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
