package io.atlasmap.java.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class JavaClass extends JavaField implements Serializable {

    private final static long serialVersionUID = 1L;

    protected JavaEnumFields javaEnumFields;

    protected JavaFields javaFields;

    protected String packageName;

    protected Boolean annotation;

    protected Boolean annonymous;

    protected Boolean enumeration;

    protected Boolean _interface;

    protected Boolean localClass;

    protected Boolean memberClass;

    protected String uri;

    /**
     * Gets the value of the javaEnumFields property.
     * 
     * @return
     *     possible object is
     *     {@link JavaEnumFields }
     *     
     */
    public JavaEnumFields getJavaEnumFields() {
        return javaEnumFields;
    }

    /**
     * Sets the value of the javaEnumFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaEnumFields }
     *     
     */
    public void setJavaEnumFields(JavaEnumFields value) {
        this.javaEnumFields = value;
    }

    /**
     * Gets the value of the javaFields property.
     * 
     * @return
     *     possible object is
     *     {@link JavaFields }
     *     
     */
    public JavaFields getJavaFields() {
        return javaFields;
    }

    /**
     * Sets the value of the javaFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link JavaFields }
     *     
     */
    public void setJavaFields(JavaFields value) {
        this.javaFields = value;
    }

    /**
     * Gets the value of the packageName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the value of the packageName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPackageName(String value) {
        this.packageName = value;
    }

    /**
     * Gets the value of the annotation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAnnotation() {
        return annotation;
    }

    /**
     * Sets the value of the annotation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAnnotation(Boolean value) {
        this.annotation = value;
    }

    /**
     * Gets the value of the annonymous property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAnnonymous() {
        return annonymous;
    }

    /**
     * Sets the value of the annonymous property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAnnonymous(Boolean value) {
        this.annonymous = value;
    }

    /**
     * Gets the value of the enumeration property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEnumeration() {
        return enumeration;
    }

    /**
     * Sets the value of the enumeration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnumeration(Boolean value) {
        this.enumeration = value;
    }

    /**
     * Gets the value of the interface property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInterface() {
        return _interface;
    }

    /**
     * Sets the value of the interface property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInterface(Boolean value) {
        this._interface = value;
    }

    /**
     * Gets the value of the localClass property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLocalClass() {
        return localClass;
    }

    /**
     * Sets the value of the localClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLocalClass(Boolean value) {
        this.localClass = value;
    }

    /**
     * Gets the value of the memberClass property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMemberClass() {
        return memberClass;
    }

    /**
     * Sets the value of the memberClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMemberClass(Boolean value) {
        this.memberClass = value;
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
        final JavaClass that = ((JavaClass) object);
        {
            JavaEnumFields leftJavaEnumFields;
            leftJavaEnumFields = this.getJavaEnumFields();
            JavaEnumFields rightJavaEnumFields;
            rightJavaEnumFields = that.getJavaEnumFields();
            if (this.javaEnumFields!= null) {
                if (that.javaEnumFields!= null) {
                    if (!leftJavaEnumFields.equals(rightJavaEnumFields)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.javaEnumFields!= null) {
                    return false;
                }
            }
        }
        {
            JavaFields leftJavaFields;
            leftJavaFields = this.getJavaFields();
            JavaFields rightJavaFields;
            rightJavaFields = that.getJavaFields();
            if (this.javaFields!= null) {
                if (that.javaFields!= null) {
                    if (!leftJavaFields.equals(rightJavaFields)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.javaFields!= null) {
                    return false;
                }
            }
        }
        {
            String leftPackageName;
            leftPackageName = this.getPackageName();
            String rightPackageName;
            rightPackageName = that.getPackageName();
            if (this.packageName!= null) {
                if (that.packageName!= null) {
                    if (!leftPackageName.equals(rightPackageName)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.packageName!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftAnnotation;
            leftAnnotation = this.isAnnotation();
            Boolean rightAnnotation;
            rightAnnotation = that.isAnnotation();
            if (this.annotation!= null) {
                if (that.annotation!= null) {
                    if (!leftAnnotation.equals(rightAnnotation)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.annotation!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftAnnonymous;
            leftAnnonymous = this.isAnnonymous();
            Boolean rightAnnonymous;
            rightAnnonymous = that.isAnnonymous();
            if (this.annonymous!= null) {
                if (that.annonymous!= null) {
                    if (!leftAnnonymous.equals(rightAnnonymous)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.annonymous!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftEnumeration;
            leftEnumeration = this.isEnumeration();
            Boolean rightEnumeration;
            rightEnumeration = that.isEnumeration();
            if (this.enumeration!= null) {
                if (that.enumeration!= null) {
                    if (!leftEnumeration.equals(rightEnumeration)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.enumeration!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftInterface;
            leftInterface = this.isInterface();
            Boolean rightInterface;
            rightInterface = that.isInterface();
            if (this._interface!= null) {
                if (that._interface!= null) {
                    if (!leftInterface.equals(rightInterface)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that._interface!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftLocalClass;
            leftLocalClass = this.isLocalClass();
            Boolean rightLocalClass;
            rightLocalClass = that.isLocalClass();
            if (this.localClass!= null) {
                if (that.localClass!= null) {
                    if (!leftLocalClass.equals(rightLocalClass)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.localClass!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftMemberClass;
            leftMemberClass = this.isMemberClass();
            Boolean rightMemberClass;
            rightMemberClass = that.isMemberClass();
            if (this.memberClass!= null) {
                if (that.memberClass!= null) {
                    if (!leftMemberClass.equals(rightMemberClass)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.memberClass!= null) {
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
        return true;
    }

    public int hashCode() {
        int currentHashCode = 1;
        currentHashCode = ((currentHashCode* 31)+ super.hashCode());
        {
            currentHashCode = (currentHashCode* 31);
            JavaEnumFields theJavaEnumFields;
            theJavaEnumFields = this.getJavaEnumFields();
            if (this.javaEnumFields!= null) {
                currentHashCode += theJavaEnumFields.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            JavaFields theJavaFields;
            theJavaFields = this.getJavaFields();
            if (this.javaFields!= null) {
                currentHashCode += theJavaFields.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String thePackageName;
            thePackageName = this.getPackageName();
            if (this.packageName!= null) {
                currentHashCode += thePackageName.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theAnnotation;
            theAnnotation = this.isAnnotation();
            if (this.annotation!= null) {
                currentHashCode += theAnnotation.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theAnnonymous;
            theAnnonymous = this.isAnnonymous();
            if (this.annonymous!= null) {
                currentHashCode += theAnnonymous.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theEnumeration;
            theEnumeration = this.isEnumeration();
            if (this.enumeration!= null) {
                currentHashCode += theEnumeration.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theInterface;
            theInterface = this.isInterface();
            if (this._interface!= null) {
                currentHashCode += theInterface.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theLocalClass;
            theLocalClass = this.isLocalClass();
            if (this.localClass!= null) {
                currentHashCode += theLocalClass.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theMemberClass;
            theMemberClass = this.isMemberClass();
            if (this.memberClass!= null) {
                currentHashCode += theMemberClass.hashCode();
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
        return currentHashCode;
    }

}
