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

public enum FieldType {

    ANY("Any"),
    ANY_DATE("Any Date"),
    BIG_INTEGER("Big Integer"),
    BOOLEAN("Boolean"),
    BYTE("Byte"),
    BYTE_ARRAY("ByteArray"),
    CHAR("Char"),
    COMPLEX("Complex"),
    DATE("Date"),
    DATE_TIME("DateTime"),
    DATE_TIME_TZ("DateTimeTZ"),
    DATE_TZ("DateTZ"),
    DECIMAL("Decimal"),
    DOUBLE("Double"),
    FLOAT("Float"),
    INTEGER("Integer"),
    LONG("Long"),
    NONE("None"),
    NUMBER("Number"),
    SHORT("Short"),
    STRING("String"),
    TIME("Time"),
    TIME_TZ("TimeTZ"),
    UNSIGNED_BYTE("Unsigned Byte"),
    UNSIGNED_INTEGER("Unsigned Integer"),
    UNSIGNED_LONG("Unsigned Long"),
    UNSIGNED_SHORT("Unsigned Short"),
    UNSUPPORTED("Unsupported");

    private final String value;

    FieldType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FieldType fromValue(String v) {
        for (FieldType c: FieldType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
