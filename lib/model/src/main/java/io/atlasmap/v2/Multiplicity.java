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
 * The enumeration of multiplicity.
 */
public enum Multiplicity {
    /** One To One. */
    ONE_TO_ONE("OneToOne"),
    /** One To Many. */
    ONE_TO_MANY("OneToMany"),
    /** Many To One. */
    MANY_TO_ONE("ManyToOne"),
    /** Zero To One. */
    ZERO_TO_ONE("ZeroToOne"),
    /** Many To Many. */
    MANY_TO_MANY("ManyToMany");

    private final String value;

    /**
     * A constructor.
     * @param v value
     */
    Multiplicity(String v) {
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
    public static Multiplicity fromValue(String v) {
        for (Multiplicity c: Multiplicity.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
