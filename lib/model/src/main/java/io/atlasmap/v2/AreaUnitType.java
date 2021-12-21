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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The enumeration of area units.
 */
public enum AreaUnitType {

    /** Square Meter. */
    SQUARE_METER("Square Meter"),
    /** Square Mile. */
    SQUARE_MILE("Square Mile"),
    /** Square Foot. */
    SQUARE_FOOT("Square Foot");

    private final String value;

    AreaUnitType(String v) {
        value = v;
    }

    /**
     * Gets a value of the enum.
     * @return value
     */
    @JsonValue
    public String value() {
        return value;
    }

    /**
     * Gets an enum represented by the string value.
     * @param v string value
     * @return the enum
     */
    @JsonCreator
    public static AreaUnitType fromValue(String v) {
        for (AreaUnitType c: AreaUnitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
