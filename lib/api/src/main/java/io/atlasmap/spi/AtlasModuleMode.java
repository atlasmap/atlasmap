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
 * The enumeration of the {@link AtlasModule} mode, specifically
 * {@link SOURCE} for the source document, {@link TARGET} for the target document,
 * and {@link UNSET} for none of those.
 */
public enum AtlasModuleMode {
    /** Source Document. */
    SOURCE("source"),
    /** Target Document. */
    TARGET("target"),
    /** None of those. */
    UNSET("unset");

    private final String mode;

    /**
     * A constructor.
     * @param mode mode
     */
    AtlasModuleMode(String mode) {
        this.mode = mode;
    }

    /**
     * Gets the value.
     * @return value
     */
    public String value() {
        return this.mode;
    }

    /**
     * Gets the enum from the value.
     * @param v value
     * @return the enum
     */
    public static AtlasModuleMode fromValue(String v) {
        for (AtlasModuleMode c : AtlasModuleMode.values()) {
            if (c.mode.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
