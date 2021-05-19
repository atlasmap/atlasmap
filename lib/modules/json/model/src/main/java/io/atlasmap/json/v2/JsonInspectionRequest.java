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
package io.atlasmap.json.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.atlasmap.v2.StringList;

@JsonRootName("JsonInspectionRequest")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class JsonInspectionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    protected StringList fieldNameExclusions;

    protected StringList typeNameExclusions;

    protected StringList namespaceExclusions;

    protected String jsonData;

    protected String uri;

    protected InspectionType type;

    /**
     * Gets the value of the fieldNameExclusions property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getFieldNameExclusions() {
        return fieldNameExclusions;
    }

    /**
     * Sets the value of the fieldNameExclusions property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setFieldNameExclusions(StringList value) {
        this.fieldNameExclusions = value;
    }

    /**
     * Gets the value of the typeNameExclusions property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getTypeNameExclusions() {
        return typeNameExclusions;
    }

    /**
     * Sets the value of the typeNameExclusions property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setTypeNameExclusions(StringList value) {
        this.typeNameExclusions = value;
    }

    /**
     * Gets the value of the namespaceExclusions property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getNamespaceExclusions() {
        return namespaceExclusions;
    }

    /**
     * Sets the value of the namespaceExclusions property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setNamespaceExclusions(StringList value) {
        this.namespaceExclusions = value;
    }

    /**
     * Gets the value of the jsonData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJsonData() {
        return jsonData;
    }

    /**
     * Sets the value of the jsonData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJsonData(String value) {
        this.jsonData = value;
    }

    /**
     * Gets the value of the uri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the value of the uri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUri(String value) {
        this.uri = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link InspectionType }
     *     
     */
    public InspectionType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link InspectionType }
     *     
     */
    public void setType(InspectionType value) {
        this.type = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final JsonInspectionRequest that = ((JsonInspectionRequest) object);
        {
            StringList leftFieldNameExclusions;
            leftFieldNameExclusions = this.getFieldNameExclusions();
            StringList rightFieldNameExclusions;
            rightFieldNameExclusions = that.getFieldNameExclusions();
            if (this.fieldNameExclusions!= null) {
                if (that.fieldNameExclusions!= null) {
                    if (!leftFieldNameExclusions.equals(rightFieldNameExclusions)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.fieldNameExclusions!= null) {
                    return false;
                }
            }
        }
        {
            StringList leftTypeNameExclusions;
            leftTypeNameExclusions = this.getTypeNameExclusions();
            StringList rightTypeNameExclusions;
            rightTypeNameExclusions = that.getTypeNameExclusions();
            if (this.typeNameExclusions!= null) {
                if (that.typeNameExclusions!= null) {
                    if (!leftTypeNameExclusions.equals(rightTypeNameExclusions)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.typeNameExclusions!= null) {
                    return false;
                }
            }
        }
        {
            StringList leftNamespaceExclusions;
            leftNamespaceExclusions = this.getNamespaceExclusions();
            StringList rightNamespaceExclusions;
            rightNamespaceExclusions = that.getNamespaceExclusions();
            if (this.namespaceExclusions!= null) {
                if (that.namespaceExclusions!= null) {
                    if (!leftNamespaceExclusions.equals(rightNamespaceExclusions)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.namespaceExclusions!= null) {
                    return false;
                }
            }
        }
        {
            String leftJsonData;
            leftJsonData = this.getJsonData();
            String rightJsonData;
            rightJsonData = that.getJsonData();
            if (this.jsonData!= null) {
                if (that.jsonData!= null) {
                    if (!leftJsonData.equals(rightJsonData)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.jsonData!= null) {
                    return false;
                }
            }
        }
        {
            String leftUri;
            leftUri = this.getUri();
            String rightUri;
            rightUri = that.getUri();
            if (this.uri!= null) {
                if (that.uri!= null) {
                    if (!leftUri.equals(rightUri)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.uri!= null) {
                    return false;
                }
            }
        }
        {
            InspectionType leftType;
            leftType = this.getType();
            InspectionType rightType;
            rightType = that.getType();
            if (this.type!= null) {
                if (that.type!= null) {
                    if (!leftType.equals(rightType)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.type!= null) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int currentHashCode = 1;
        {
            currentHashCode = (currentHashCode* 31);
            StringList theFieldNameExclusions;
            theFieldNameExclusions = this.getFieldNameExclusions();
            if (this.fieldNameExclusions!= null) {
                currentHashCode += theFieldNameExclusions.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            StringList theTypeNameExclusions;
            theTypeNameExclusions = this.getTypeNameExclusions();
            if (this.typeNameExclusions!= null) {
                currentHashCode += theTypeNameExclusions.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            StringList theNamespaceExclusions;
            theNamespaceExclusions = this.getNamespaceExclusions();
            if (this.namespaceExclusions!= null) {
                currentHashCode += theNamespaceExclusions.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theJsonData;
            theJsonData = this.getJsonData();
            if (this.jsonData!= null) {
                currentHashCode += theJsonData.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theUri;
            theUri = this.getUri();
            if (this.uri!= null) {
                currentHashCode += theUri.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            InspectionType theType;
            theType = this.getType();
            if (this.type!= null) {
                currentHashCode += theType.hashCode();
            }
        }
        return currentHashCode;
    }

}
