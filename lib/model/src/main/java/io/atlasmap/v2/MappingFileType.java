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
 * The enumeration of the file type those are used in AtlasMap.
 */
public enum MappingFileType {
    /** ADM archive. */
    ADM("adm"),
    /** gzipped. */
    GZ("gz"),
    /** zipped. */
    ZIP("zip"),
    /** JSON. */
    JSON("json"),
    /** XML. */
    XML("xml");

    private String value;

    /**
     * A constructor.
     * @param mappingFormat file type
     */
    MappingFileType(String mappingFormat) {
        value = mappingFormat;
    }

    /**
     * Gets the value.
     * @return value
     */
    public String value(){
        return this.value;
    }
}
