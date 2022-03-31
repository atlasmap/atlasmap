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
 * The enumeration of the Document type such as {@link #JAVA}, {@link #JSON}, {@link #XML} and etc.
 */
public enum DocumentType {

    CORE("Core"),
    CSV ("CSV"),
    DFDL("DFDL"),
    JAVA("JAVA"),
    JSON("JSON"),
    KAFKA_AVRO("KAFKA_AVRO"),
    KAFKA_JSON("KAFKA_JSON"),
    XML("XML"),
    XSD("XSD"),
    // Predefined Documents
    CONSTANT("Constants"),
    PROPERTY("Property");

    private final String value;

    /**
     * A constructor.
     * @param v value
     */
    DocumentType(String v) {
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
     * Gets the enum from value.
     * @param v value
     * @return the enum
     */
    public static DocumentType fromValue(String v) {
        for (DocumentType c: DocumentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
