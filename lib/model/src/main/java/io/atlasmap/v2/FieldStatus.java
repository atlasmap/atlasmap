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

/**
 * The enumeration of {#link Field} status.
 */
public enum FieldStatus {

    /** Supported. */
    SUPPORTED("Supported"),
    /** Unsupported. */
    UNSUPPORTED("Unsupported"),
    /** Cached. */
    CACHED("Cached"),
    /** Error. */
    ERROR("Error"),
    /** Not Found. */
    NOT_FOUND("NotFound"),
    /** Excluded. */
    EXCLUDED("Excluded");

    private final String value;

    /**
     * A constructor.
     * @param v value
     */
    FieldStatus(String v) {
        value = v;
    }

    /**
     * Gets the value.
     * @return value
     */
    public String value() {
        return value;
    }

    /**
     * Gets the enum from the value.
     * @param v value
     * @return the enum
     */
    public static FieldStatus fromValue(String v) {
        for (FieldStatus c: FieldStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
