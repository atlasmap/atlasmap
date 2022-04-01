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
package io.atlasmap.xml.v2;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.atlasmap.v2.BaseInspectionRequest;

/**
 * The top container object of XML Document inspection request that AtlasMap UI sends
 * to the backend.
 */
@JsonRootName("XmlInspectionRequest")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class XmlInspectionRequest extends BaseInspectionRequest {

    private static final long serialVersionUID = 1L;
    /** Raw XML schema/instance data to inspect. */
    private String xmlData;

    /**
     * Gets the value of the xmlData property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getXmlData() {
        return xmlData;
    }

    /**
     * Sets the value of the xmlData property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setXmlData(String value) {
        this.xmlData = value;
    }

    @Override
    public boolean equals(Object object) {
        if (!super.equals(object)) {
            return false;
        }
        final XmlInspectionRequest that = ((XmlInspectionRequest) object);
        String leftXmlData;
        leftXmlData = this.getXmlData();
        String rightXmlData;
        rightXmlData = that.getXmlData();
        if (this.xmlData!= null) {
            if (that.xmlData!= null) {
                if (!leftXmlData.equals(rightXmlData)) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (that.xmlData!= null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int currentHashCode = (super.hashCode() * 31);
        String theXmlData;
        theXmlData = this.getXmlData();
        if (this.xmlData!= null) {
            currentHashCode += theXmlData.hashCode();
        }
        return currentHashCode;
    }

}
