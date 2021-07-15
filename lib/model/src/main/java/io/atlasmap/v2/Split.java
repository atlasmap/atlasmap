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

public class Split extends Action implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String delimiter;
    private boolean collapseRepeatingDelimiter = false;

    /**
     * Gets the value of the delimiter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the value of the delimiter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The delimiter string to split with")
    @AtlasActionProperty(title = "Delimiter", type = FieldType.STRING)
    public void setDelimiter(String value) {
        this.delimiter = value;
    }

    public Boolean getCollapseRepeatingDelimiters() {
        return collapseRepeatingDelimiter;
    }

    @JsonPropertyDescription("Check to collapse repeating delimiters")
    @AtlasActionProperty(title = "Collapse Repeating Delimiters", type = FieldType.STRING)
    public void setCollapseRepeatingDelimiters(Boolean collapseRepeatingDelimiter) {
        this.collapseRepeatingDelimiter = collapseRepeatingDelimiter;
    }

}
