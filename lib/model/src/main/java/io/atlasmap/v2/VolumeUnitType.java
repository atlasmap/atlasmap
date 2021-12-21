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
 * The enumeration of the volume units.
 */
public enum VolumeUnitType {
    /** Cubic Meter. */
    CUBIC_METER("Cubic Meter"),
    /** Liter. */
    LITER("Liter"),
    /** Cubic Foot. */
    CUBIC_FOOT("Cubic Foot"),
    /** Gallon (US Fluid). */
    GALLON_US_FLUID("Gallon (US Fluid)");

    private final String value;

    /**
     * A constuctor.
     * @param v value
     */
    VolumeUnitType(String v) {
        value = v;
    }

    /**
     * Gets the value.
     * @return value
     */
    @JsonValue
    public String value() {
        return value;
    }

    /**
     * Gets the enum from the value.
     * @param v value
     * @return the enum
     */
    @JsonCreator
    public static VolumeUnitType fromValue(String v) {
        for (VolumeUnitType c: VolumeUnitType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
