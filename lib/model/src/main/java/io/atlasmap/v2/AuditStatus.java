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
 * The enumeration of the audit status.
 */
public enum AuditStatus {

    /** All. */
    ALL("All"),
    /** Info. */
    INFO("Info"),
    /** Warn. */
    WARN("Warn"),
    /** Error. */
    ERROR("Error"),
    /** None. */
    NONE("None");

    private final String value;

    /**
     * A Constructor.
     * @param v value
     */
    AuditStatus(String v) {
        value = v;
    }

    /**
     * Gets a value.
     * @return value
     */
    public String value() {
        return value;
    }

    /**
     * Gets an enum from value.
     * @param v value
     * @return an enum
     */
    public static AuditStatus fromValue(String v) {
        for (AuditStatus c: AuditStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
