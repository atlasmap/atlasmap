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
package io.atlasmap.spi;

/**
 * The enumeration of conversion concerns which represents the concerns about the data conversion.
 * This is supposed to be displayed in the UI where the concern is appropriate.
 */
public enum AtlasConversionConcern {
    /** none. */
    NONE("none", "Conversion from '%s' to '%s' is supported"),
    /** out of range. */
    RANGE("range", "Conversion from '%s' to '%s' can cause out of range exceptions"),
    /** fractional part could be lost. */
    FRACTIONAL_PART("fractional part", "Conversion from '%s' to '%s' can cause fractional part to be lost"),
    /** timezone could be lost. */
    TIMEZONE("timezone", "Conversion from '%s' to '%s' causes timezone part to be lost"),
    /** numeric format exception. */
    FORMAT("format", "Conversion from '%s' to '%s' can cause numeric format exceptions"),
    /** default convention is used. */
    CONVENTION("format", "Conversion from '%s' to '%s' uses a default convention for values"),
    /** unsupported. */
    UNSUPPORTED("unsupported", "Conversions from '%s' to '%s' is not supported");

    private String name;
    private String message;

    /**
     * A consturctor.
     * @param name name
     * @param message message
     */
    AtlasConversionConcern(String name, String message) {
        this.name = name;
        this.message = message;
    }

    /**
     * Gets the value.
     * @return value
     */
    public String value() {
        return this.name;
    }

    /**
     * Gets the message.
     * @param converterAnno converter annotation
     * @return message
     */
    public String getMessage(AtlasConversionInfo converterAnno) {
        return String.format(this.message, converterAnno.sourceType(), converterAnno.targetType());
    }

    /**
     * Gets the enum from the value.
     * @param v value
     * @return the enum
     */
    public static AtlasConversionConcern fromValue(String v) {
        for (AtlasConversionConcern c : AtlasConversionConcern.values()) {
            if (c.name().equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
