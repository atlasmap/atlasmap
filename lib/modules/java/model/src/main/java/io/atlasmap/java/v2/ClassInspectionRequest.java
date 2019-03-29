package io.atlasmap.java.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.StringList;

@JsonRootName("ClassInspectionRequest")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class ClassInspectionRequest implements Serializable {

    private final static long serialVersionUID = 1L;

    protected StringList fieldNameBlacklist;

    protected StringList classNameBlacklist;
    protected String classpath;

    protected String className;

    protected CollectionType collectionType;

    protected String collectionClassName;

    protected Boolean disablePrivateOnlyFields;

    protected Boolean disableProtectedOnlyFields;

    protected Boolean disablePublicOnlyFields;

    protected Boolean disablePublicGetterSetterFields;

    /**
     * Gets the value of the fieldNameBlacklist property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getFieldNameBlacklist() {
        return fieldNameBlacklist;
    }

    /**
     * Sets the value of the fieldNameBlacklist property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setFieldNameBlacklist(StringList value) {
        this.fieldNameBlacklist = value;
    }

    /**
     * Gets the value of the classNameBlacklist property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getClassNameBlacklist() {
        return classNameBlacklist;
    }

    /**
     * Sets the value of the classNameBlacklist property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setClassNameBlacklist(StringList value) {
        this.classNameBlacklist = value;
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
            StringList leftFieldNameBlacklist;
            leftFieldNameBlacklist = this.getFieldNameBlacklist();
            StringList rightFieldNameBlacklist;
            rightFieldNameBlacklist = that.getFieldNameBlacklist();
            if (this.fieldNameBlacklist!= null) {
                if (that.fieldNameBlacklist!= null) {
                    if (!leftFieldNameBlacklist.equals(rightFieldNameBlacklist)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.fieldNameBlacklist!= null) {
                    return false;
                }
            }
        }
        {
            StringList leftClassNameBlacklist;
            leftClassNameBlacklist = this.getClassNameBlacklist();
            StringList rightClassNameBlacklist;
            rightClassNameBlacklist = that.getClassNameBlacklist();
            if (this.classNameBlacklist!= null) {
                if (that.classNameBlacklist!= null) {
                    if (!leftClassNameBlacklist.equals(rightClassNameBlacklist)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.classNameBlacklist!= null) {
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
            StringList theFieldNameBlacklist;
            theFieldNameBlacklist = this.getFieldNameBlacklist();
            if (this.fieldNameBlacklist!= null) {
                currentHashCode += theFieldNameBlacklist.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            StringList theClassNameBlacklist;
            theClassNameBlacklist = this.getClassNameBlacklist();
            if (this.classNameBlacklist!= null) {
                currentHashCode += theClassNameBlacklist.hashCode();
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
