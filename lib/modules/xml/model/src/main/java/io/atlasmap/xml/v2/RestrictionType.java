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

public enum RestrictionType {

    ALL("All"),
    ENUMERATION("enumeration"),
    FRACTION_DIGITS("fractionDigits"),
    LENGTH("length"),
    MAX_EXCLUSIVE("maxExclusive"),
    MAX_INCLUSIVE("maxInclusive"),
    MAX_LENGTH("maxLength"),
    MIN_EXCLUSIVE("minExclusive"),
    MIN_INCLUSIVE("minInclusive"),
    MIN_LENGTH("minLength"),
    PATTERN("pattern"),
    TOTAL_DIGITS("totalDigits"),
    WHITE_SPACE("whiteSpace"),
    NONE("None");

    private final String value;

    RestrictionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RestrictionType fromValue(String v) {
        for (RestrictionType c: RestrictionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
