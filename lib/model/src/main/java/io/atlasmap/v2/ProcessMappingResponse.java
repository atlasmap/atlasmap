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

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonRootName("ProcessMappingResponse")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class ProcessMappingResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Mapping mapping;

    protected Audits audits;

    protected AtlasMappingResult atlasMappingResult;

    /**
     * Gets the value of the mapping property.
     * 
     * @return
     *     possible object is
     *     {@link Mapping }
     *     
     */
    public Mapping getMapping() {
        return mapping;
    }

    /**
     * Sets the value of the mapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mapping }
     *     
     */
    public void setMapping(Mapping value) {
        this.mapping = value;
    }

    /**
     * Gets the value of the audits property.
     * 
     * @return
     *     possible object is
     *     {@link Audits }
     *     
     */
    public Audits getAudits() {
        return audits;
    }

    /**
     * Sets the value of the audits property.
     * 
     * @param value
     *     allowed object is
     *     {@link Audits }
     *     
     */
    public void setAudits(Audits value) {
        this.audits = value;
    }

    /**
     * Gets the value of the atlasMappingResult property.
     * 
     * @return
     *     possible object is
     *     {@link AtlasMappingResult }
     *     
     */
    public AtlasMappingResult getAtlasMappingResult() {
        return atlasMappingResult;
    }

    /**
     * Sets the value of the atlasMappingResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link AtlasMappingResult }
     *     
     */
    public void setAtlasMappingResult(AtlasMappingResult value) {
        this.atlasMappingResult = value;
    }

}
