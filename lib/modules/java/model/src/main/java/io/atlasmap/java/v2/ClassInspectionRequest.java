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
package io.atlasmap.java.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.StringList;

@JsonRootName("ClassInspectionRequest")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class ClassInspectionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    protected StringList fieldNameExclusions;

    protected StringList classNameExclusions;
    protected String classpath;

    protected String className;

    protected CollectionType collectionType;

    protected String collectionClassName;

    protected Boolean disablePrivateOnlyFields;

    protected Boolean disableProtectedOnlyFields;

    protected Boolean disablePublicOnlyFields;

    protected Boolean disablePublicGetterSetterFields;

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
     * Gets the value of the classNameExclusions property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getClassNameExclusions() {
        return classNameExclusions;
    }

    /**
     * Sets the value of the classNameExclusions property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setClassNameExclusions(StringList value) {
        this.classNameExclusions = value;
    }

    /**
     * Gets the value of the classpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClasspath() {
        return classpath;
    }

    /**
     * Sets the value of the classpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClasspath(String value) {
        this.classpath = value;
    }

    /**
     * Gets the value of the className property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the collectionType property.
     * 
     * @return
     *     possible object is
     *     {@link CollectionType }
     *     
     */
    public CollectionType getCollectionType() {
        return collectionType;
    }

    /**
     * Sets the value of the collectionType property.
     * 
     * @param value
     *     allowed object is
     *     {@link CollectionType }
     *     
     */
    public void setCollectionType(CollectionType value) {
        this.collectionType = value;
    }

    /**
     * Gets the value of the collectionClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionClassName() {
        return collectionClassName;
    }

    /**
     * Sets the value of the collectionClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionClassName(String value) {
        this.collectionClassName = value;
    }

    /**
     * Gets the value of the disablePrivateOnlyFields property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDisablePrivateOnlyFields() {
        return disablePrivateOnlyFields;
    }

    /**
     * Sets the value of the disablePrivateOnlyFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisablePrivateOnlyFields(Boolean value) {
        this.disablePrivateOnlyFields = value;
    }

    /**
     * Gets the value of the disableProtectedOnlyFields property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDisableProtectedOnlyFields() {
        return disableProtectedOnlyFields;
    }

    /**
     * Sets the value of the disableProtectedOnlyFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisableProtectedOnlyFields(Boolean value) {
        this.disableProtectedOnlyFields = value;
    }

    /**
     * Gets the value of the disablePublicOnlyFields property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDisablePublicOnlyFields() {
        return disablePublicOnlyFields;
    }

    /**
     * Sets the value of the disablePublicOnlyFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisablePublicOnlyFields(Boolean value) {
        this.disablePublicOnlyFields = value;
    }

    /**
     * Gets the value of the disablePublicGetterSetterFields property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDisablePublicGetterSetterFields() {
        return disablePublicGetterSetterFields;
    }

    /**
     * Sets the value of the disablePublicGetterSetterFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDisablePublicGetterSetterFields(Boolean value) {
        this.disablePublicGetterSetterFields = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final ClassInspectionRequest that = ((ClassInspectionRequest) object);
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
            StringList leftClassNameExclusions;
            leftClassNameExclusions = this.getClassNameExclusions();
            StringList rightClassNameExclusions;
            rightClassNameExclusions = that.getClassNameExclusions();
            if (this.classNameExclusions!= null) {
                if (that.classNameExclusions!= null) {
                    if (!leftClassNameExclusions.equals(rightClassNameExclusions)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.classNameExclusions!= null) {
                    return false;
                }
            }
        }
        {
            String leftClasspath;
            leftClasspath = this.getClasspath();
            String rightClasspath;
            rightClasspath = that.getClasspath();
            if (this.classpath!= null) {
                if (that.classpath!= null) {
                    if (!leftClasspath.equals(rightClasspath)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.classpath!= null) {
                    return false;
                }
            }
        }
        {
            String leftClassName;
            leftClassName = this.getClassName();
            String rightClassName;
            rightClassName = that.getClassName();
            if (this.className!= null) {
                if (that.className!= null) {
                    if (!leftClassName.equals(rightClassName)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.className!= null) {
                    return false;
                }
            }
        }
        {
            CollectionType leftCollectionType;
            leftCollectionType = this.getCollectionType();
            CollectionType rightCollectionType;
            rightCollectionType = that.getCollectionType();
            if (this.collectionType!= null) {
                if (that.collectionType!= null) {
                    if (!leftCollectionType.equals(rightCollectionType)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.collectionType!= null) {
                    return false;
                }
            }
        }
        {
            String leftCollectionClassName;
            leftCollectionClassName = this.getCollectionClassName();
            String rightCollectionClassName;
            rightCollectionClassName = that.getCollectionClassName();
            if (this.collectionClassName!= null) {
                if (that.collectionClassName!= null) {
                    if (!leftCollectionClassName.equals(rightCollectionClassName)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.collectionClassName!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftDisablePrivateOnlyFields;
            leftDisablePrivateOnlyFields = this.isDisablePrivateOnlyFields();
            Boolean rightDisablePrivateOnlyFields;
            rightDisablePrivateOnlyFields = that.isDisablePrivateOnlyFields();
            if (this.disablePrivateOnlyFields!= null) {
                if (that.disablePrivateOnlyFields!= null) {
                    if (!leftDisablePrivateOnlyFields.equals(rightDisablePrivateOnlyFields)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.disablePrivateOnlyFields!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftDisableProtectedOnlyFields;
            leftDisableProtectedOnlyFields = this.isDisableProtectedOnlyFields();
            Boolean rightDisableProtectedOnlyFields;
            rightDisableProtectedOnlyFields = that.isDisableProtectedOnlyFields();
            if (this.disableProtectedOnlyFields!= null) {
                if (that.disableProtectedOnlyFields!= null) {
                    if (!leftDisableProtectedOnlyFields.equals(rightDisableProtectedOnlyFields)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.disableProtectedOnlyFields!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftDisablePublicOnlyFields;
            leftDisablePublicOnlyFields = this.isDisablePublicOnlyFields();
            Boolean rightDisablePublicOnlyFields;
            rightDisablePublicOnlyFields = that.isDisablePublicOnlyFields();
            if (this.disablePublicOnlyFields!= null) {
                if (that.disablePublicOnlyFields!= null) {
                    if (!leftDisablePublicOnlyFields.equals(rightDisablePublicOnlyFields)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.disablePublicOnlyFields!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftDisablePublicGetterSetterFields;
            leftDisablePublicGetterSetterFields = this.isDisablePublicGetterSetterFields();
            Boolean rightDisablePublicGetterSetterFields;
            rightDisablePublicGetterSetterFields = that.isDisablePublicGetterSetterFields();
            if (this.disablePublicGetterSetterFields!= null) {
                if (that.disablePublicGetterSetterFields!= null) {
                    if (!leftDisablePublicGetterSetterFields.equals(rightDisablePublicGetterSetterFields)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.disablePublicGetterSetterFields!= null) {
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
            StringList theClassNameExclusions;
            theClassNameExclusions = this.getClassNameExclusions();
            if (this.classNameExclusions!= null) {
                currentHashCode += theClassNameExclusions.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theClasspath;
            theClasspath = this.getClasspath();
            if (this.classpath!= null) {
                currentHashCode += theClasspath.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theClassName;
            theClassName = this.getClassName();
            if (this.className!= null) {
                currentHashCode += theClassName.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            CollectionType theCollectionType;
            theCollectionType = this.getCollectionType();
            if (this.collectionType!= null) {
                currentHashCode += theCollectionType.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theCollectionClassName;
            theCollectionClassName = this.getCollectionClassName();
            if (this.collectionClassName!= null) {
                currentHashCode += theCollectionClassName.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theDisablePrivateOnlyFields;
            theDisablePrivateOnlyFields = this.isDisablePrivateOnlyFields();
            if (this.disablePrivateOnlyFields!= null) {
                currentHashCode += theDisablePrivateOnlyFields.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theDisableProtectedOnlyFields;
            theDisableProtectedOnlyFields = this.isDisableProtectedOnlyFields();
            if (this.disableProtectedOnlyFields!= null) {
                currentHashCode += theDisableProtectedOnlyFields.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theDisablePublicOnlyFields;
            theDisablePublicOnlyFields = this.isDisablePublicOnlyFields();
            if (this.disablePublicOnlyFields!= null) {
                currentHashCode += theDisablePublicOnlyFields.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theDisablePublicGetterSetterFields;
            theDisablePublicGetterSetterFields = this.isDisablePublicGetterSetterFields();
            if (this.disablePublicGetterSetterFields!= null) {
                currentHashCode += theDisablePublicGetterSetterFields.hashCode();
            }
        }
        return currentHashCode;
    }

}
