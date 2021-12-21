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
package io.atlasmap.xml.v2;

/**
 * The enumeration of the types of the XML schema restrictions.
 * @see Restriction
 */
public enum RestrictionType {
    /** All. */
    ALL("All"),
    /** enumeration. */
    ENUMERATION("enumeration"),
    /** Fraction Digits. */
    FRACTION_DIGITS("fractionDigits"),
    /** Length. */
    LENGTH("length"),
    /** Max Exclusive. */
    MAX_EXCLUSIVE("maxExclusive"),
    /** Max Inclusive. */
    MAX_INCLUSIVE("maxInclusive"),
    /** Max Length. */
    MAX_LENGTH("maxLength"),
    /** Min Exclusive. */
    MIN_EXCLUSIVE("minExclusive"),
    /** Min Inclusive. */
    MIN_INCLUSIVE("minInclusive"),
    /** Min Length. */
    MIN_LENGTH("minLength"),
    /** Pattern. */
    PATTERN("pattern"),
    /** Total Digits. */
    TOTAL_DIGITS("totalDigits"),
    /** Whitespace. */
    WHITE_SPACE("whiteSpace"),
    /** None. */
    NONE("None");

    private final String value;

    /**
     * A constructor.
     * @param v value
     */
    RestrictionType(String v) {
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
    public static RestrictionType fromValue(String v) {
        for (RestrictionType c: RestrictionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
