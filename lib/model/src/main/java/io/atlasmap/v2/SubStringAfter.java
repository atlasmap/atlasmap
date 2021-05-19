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

public class SubStringAfter extends Action implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Integer startIndex;

    protected Integer endIndex;

    protected String match;

    /**
     * Gets the value of the startIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the value of the startIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    @JsonPropertyDescription("The start index to substring")
    @AtlasActionProperty(title = "Start index", type = FieldType.STRING)
    public void setStartIndex(Integer value) {
        this.startIndex = value;
    }

    /**
     * Gets the value of the endIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEndIndex() {
        return endIndex;
    }

    /**
     * Sets the value of the endIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    @JsonPropertyDescription("The end index to substring")
    @AtlasActionProperty(title = "End index", type = FieldType.STRING)
    public void setEndIndex(Integer value) {
        this.endIndex = value;
    }

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

}
