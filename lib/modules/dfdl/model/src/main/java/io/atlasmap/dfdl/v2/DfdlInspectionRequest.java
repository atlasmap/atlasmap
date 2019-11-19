/**
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
package io.atlasmap.dfdl.v2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.atlasmap.xml.v2.XmlInspectionRequest;

@JsonRootName("DfdlInspectionRequest")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class DfdlInspectionRequest extends XmlInspectionRequest implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String dfdlSchemaName;
    protected Map<String, String> options = new HashMap<>();

    /**
     * Gets the value of the options property.
     * 
     * @return
     *     possible object is
     *     {@link Map<String, String> }
     *     
     */
    public Map<String, String> getOptions() {
        return this.options;
    }

    /**
     * Sets the value of the options property.
     * 
     * @param value
     *     allowed object is
     *     {@link Map<String, String> }
     *     
     */
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    /**
     * Gets the value of the dfdlSchemaName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDfdlSchemaName() {
        return dfdlSchemaName;
    }

    /**
     * Sets the value of the dfdlSchemaName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDfdlSchemaName(String value) {
        this.dfdlSchemaName = value;
    }

    public boolean equals(Object object) {
        if (!super.equals(object) || !(object instanceof DfdlInspectionRequest)) {
            return false;
        }
        final DfdlInspectionRequest that = ((DfdlInspectionRequest) object);
        Map<String, String> leftOptions;
        leftOptions = this.getOptions();
        Map<String, String> rightOptions;
        rightOptions = that.getOptions();
        if (leftOptions !=  null) {
            if (rightOptions != null) {
                if (!leftOptions.equals(rightOptions)) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (rightOptions != null) {
                return false;
            }
        }
        String leftDfdl;
        leftDfdl = this.getDfdlSchemaName();
        String rightDfdl;
        rightDfdl = that.getDfdlSchemaName();
        if (leftDfdl != null) {
            if (rightDfdl != null) {
                if (!leftDfdl.equals(rightDfdl)) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (rightDfdl != null) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int currentHashCode = super.hashCode();
        currentHashCode = (currentHashCode* 31);
        Map<String, String> theOptions = this.getOptions();
        if (this.options != null) {
            currentHashCode += theOptions.hashCode();
        }
        currentHashCode = (currentHashCode* 31);
        String theDfdl = this.getDfdlSchemaName();
        if (theDfdl != null) {
            currentHashCode += theDfdl.hashCode();
        }
        return currentHashCode;
    }

}
