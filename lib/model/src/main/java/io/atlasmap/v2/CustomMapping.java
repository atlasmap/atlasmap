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

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The custom mapping entry.
 */
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class CustomMapping extends BaseMapping {

    private static final long serialVersionUID = 1L;

    /** class name */
    private String className;

    /**
     * Gets the custom mapping class name.
     * @return custom mapping class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the custom mapping class name.
     * @param className custom mapping class name.
     */
    public void setClassName(String className) {
        this.className = className;
    }

}
