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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class ReplaceFirst extends Action implements Serializable {

    private static final long serialVersionUID = 1L;

    private String match;

    private String newString;

    /**
     * Gets the value of the match property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     * 
     */
    public String getMatch() {
        return match;
    }

    /**
     * Sets the value of the match property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The string to match")
    @AtlasActionProperty(title = "Match", type = FieldType.STRING)
    public void setMatch(String value) {
        this.match = value;
    }

    /**
     * Gets the value of the newString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewString() {
        return newString;
    }

    /**
     * Sets the value of the newString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The string to replace with")
    @AtlasActionProperty(title = "New string", type = FieldType.STRING)
    public void setNewString(String value) {
        this.newString = value;
    }

}
