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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.atlasmap.v2.Field;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class XmlEnumField extends Field implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Integer ordinal;

    protected String typeName;

    protected Boolean attribute = false;

    protected Boolean userCreated;

    /**
     * Gets the value of the ordinal property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOrdinal() {
        return ordinal;
    }

    /**
     * Sets the value of the ordinal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOrdinal(Integer value) {
        this.ordinal = value;
    }

    /**
     * Gets the value of the typeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Sets the value of the typeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTypeName(String value) {
        this.typeName = value;
    }

    /**
     * Gets the value of the attribute property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAttribute() {
        return attribute;
    }

    /**
     * Sets the value of the attribute property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAttribute(Boolean value) {
        this.attribute = value;
    }

    /**
     * Gets the value of the userCreated property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isUserCreated() {
        return userCreated;
    }

    /**
     * Sets the value of the userCreated property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setUserCreated(Boolean value) {
        this.userCreated = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!super.equals(object)) {
            return false;
        }
        final XmlEnumField that = ((XmlEnumField) object);
        {
            String leftName;
            leftName = this.getName();
            String rightName;
            rightName = that.getName();
            if (this.name!= null) {
                if (that.name!= null) {
                    if (!leftName.equals(rightName)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.name!= null) {
                    return false;
                }
            }
        }
        {
            Integer leftOrdinal;
            leftOrdinal = this.getOrdinal();
            Integer rightOrdinal;
            rightOrdinal = that.getOrdinal();
            if (this.ordinal!= null) {
                if (that.ordinal!= null) {
                    if (!leftOrdinal.equals(rightOrdinal)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.ordinal!= null) {
                    return false;
                }
            }
        }
        {
            String leftTypeName;
            leftTypeName = this.getTypeName();
            String rightTypeName;
            rightTypeName = that.getTypeName();
            if (this.typeName!= null) {
                if (that.typeName!= null) {
                    if (!leftTypeName.equals(rightTypeName)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.typeName!= null) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int currentHashCode = 1;
        currentHashCode = ((currentHashCode* 31)+ super.hashCode());
        {
            currentHashCode = (currentHashCode* 31);
            String theName;
            theName = this.getName();
            if (this.name!= null) {
                currentHashCode += theName.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Integer theOrdinal;
            theOrdinal = this.getOrdinal();
            if (this.ordinal!= null) {
                currentHashCode += theOrdinal.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theTypeName;
            theTypeName = this.getTypeName();
            if (this.typeName!= null) {
                currentHashCode += theTypeName.hashCode();
            }
        }
        return currentHashCode;
    }

}
