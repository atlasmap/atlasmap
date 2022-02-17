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
 * The enumeration of {@link Field} type.
 */
public enum FieldType {

    /** Any. */
    ANY("Any"),
    /** Any Date. */
    ANY_DATE("Any Date"),
    /** Big Integer. */
    BIG_INTEGER("Big Integer"),
    /** Boolean. */
    BOOLEAN("Boolean"),
    /** Byte. */
    BYTE("Byte"),
    /** Byte Array. */
    BYTE_ARRAY("ByteArray"),
    /** Char. */
    CHAR("Char"),
    /** Complex. */
    COMPLEX("Complex"),
    /** Date. */
    DATE("Date"),
    /** Date Time. */
    DATE_TIME("DateTime"),
    /** Date Time TZ. */
    DATE_TIME_TZ("DateTimeTZ"),
    /** Date TZ. */
    DATE_TZ("DateTZ"),
    /** Decimal. */
    DECIMAL("Decimal"),
    /** Double. */
    DOUBLE("Double"),
    /** Enum. */
    ENUM("Enum"),
    /** Float. */
    FLOAT("Float"),
    /** Integer. */
    INTEGER("Integer"),
    /** Long. */
    LONG("Long"),
    /** None. */
    NONE("None"),
    /** Number. */
    NUMBER("Number"),
    /** Short. */
    SHORT("Short"),
    /** String. */
    STRING("String"),
    /** Time. */
    TIME("Time"),
    /** Time TZ. */
    TIME_TZ("TimeTZ"),
    /** Unsigned Byte. */
    UNSIGNED_BYTE("Unsigned Byte"),
    /** Unsigned Integer. */
    UNSIGNED_INTEGER("Unsigned Integer"),
    /** Unsigned Long. */
    UNSIGNED_LONG("Unsigned Long"),
    /** Unsigned Short. */
    UNSIGNED_SHORT("Unsigned Short"),
    /** Unsupported. */
    UNSUPPORTED("Unsupported");

    private final String value;

    /**
     * A constructor.
     * @param v value
     */
    FieldType(String v) {
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
    public static FieldType fromValue(String v) {
        for (FieldType c: FieldType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
