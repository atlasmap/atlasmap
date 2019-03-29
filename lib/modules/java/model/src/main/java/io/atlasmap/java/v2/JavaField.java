package io.atlasmap.java.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.StringList;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class JavaField extends Field implements Serializable {

    private final static long serialVersionUID = 1L;

    protected StringList annotations;

    protected ModifierList modifiers;

    protected StringList parameterizedTypes;

    protected String name;

    protected String className;

    protected String canonicalClassName;

    protected String collectionClassName;

    protected String getMethod;

    protected String setMethod;

    protected Boolean primitive;

    protected Boolean synthetic;

    /**
     * Gets the value of the annotations property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getAnnotations() {
        return annotations;
    }

    /**
     * Sets the value of the annotations property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setAnnotations(StringList value) {
        this.annotations = value;
    }

    /**
     * Gets the value of the modifiers property.
     * 
     * @return
     *     possible object is
     *     {@link ModifierList }
     *     
     */
    public ModifierList getModifiers() {
        return modifiers;
    }

    /**
     * Sets the value of the modifiers property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModifierList }
     *     
     */
    public void setModifiers(ModifierList value) {
        this.modifiers = value;
    }

    /**
     * Gets the value of the parameterizedTypes property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getParameterizedTypes() {
        return parameterizedTypes;
    }

    /**
     * Sets the value of the parameterizedTypes property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setParameterizedTypes(StringList value) {
        this.parameterizedTypes = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
     * Gets the value of the canonicalClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCanonicalClassName() {
        return canonicalClassName;
    }

    /**
     * Sets the value of the canonicalClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCanonicalClassName(String value) {
        this.canonicalClassName = value;
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
     * Gets the value of the getMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGetMethod() {
        return getMethod;
    }

    /**
     * Sets the value of the getMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGetMethod(String value) {
        this.getMethod = value;
    }

    /**
     * Gets the value of the setMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSetMethod() {
        return setMethod;
    }

    /**
     * Sets the value of the setMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSetMethod(String value) {
        this.setMethod = value;
    }

    /**
     * Gets the value of the primitive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPrimitive() {
        return primitive;
    }

    /**
     * Sets the value of the primitive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPrimitive(Boolean value) {
        this.primitive = value;
    }

    /**
     * Gets the value of the synthetic property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSynthetic() {
        return synthetic;
    }

    /**
     * Sets the value of the synthetic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSynthetic(Boolean value) {
        this.synthetic = value;
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
        final JavaField that = ((JavaField) object);
        {
            StringList leftAnnotations;
            leftAnnotations = this.getAnnotations();
            StringList rightAnnotations;
            rightAnnotations = that.getAnnotations();
            if (this.annotations!= null) {
                if (that.annotations!= null) {
                    if (!leftAnnotations.equals(rightAnnotations)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.annotations!= null) {
                    return false;
                }
            }
        }
        {
            ModifierList leftModifiers;
            leftModifiers = this.getModifiers();
            ModifierList rightModifiers;
            rightModifiers = that.getModifiers();
            if (this.modifiers!= null) {
                if (that.modifiers!= null) {
                    if (!leftModifiers.equals(rightModifiers)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.modifiers!= null) {
                    return false;
                }
            }
        }
        {
            StringList leftParameterizedTypes;
            leftParameterizedTypes = this.getParameterizedTypes();
            StringList rightParameterizedTypes;
            rightParameterizedTypes = that.getParameterizedTypes();
            if (this.parameterizedTypes!= null) {
                if (that.parameterizedTypes!= null) {
                    if (!leftParameterizedTypes.equals(rightParameterizedTypes)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.parameterizedTypes!= null) {
                    return false;
                }
            }
        }
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
            String leftCanonicalClassName;
            leftCanonicalClassName = this.getCanonicalClassName();
            String rightCanonicalClassName;
            rightCanonicalClassName = that.getCanonicalClassName();
            if (this.canonicalClassName!= null) {
                if (that.canonicalClassName!= null) {
                    if (!leftCanonicalClassName.equals(rightCanonicalClassName)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.canonicalClassName!= null) {
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
            String leftGetMethod;
            leftGetMethod = this.getGetMethod();
            String rightGetMethod;
            rightGetMethod = that.getGetMethod();
            if (this.getMethod!= null) {
                if (that.getMethod!= null) {
                    if (!leftGetMethod.equals(rightGetMethod)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.getMethod!= null) {
                    return false;
                }
            }
        }
        {
            String leftSetMethod;
            leftSetMethod = this.getSetMethod();
            String rightSetMethod;
            rightSetMethod = that.getSetMethod();
            if (this.setMethod!= null) {
                if (that.setMethod!= null) {
                    if (!leftSetMethod.equals(rightSetMethod)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.setMethod!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftPrimitive;
            leftPrimitive = this.isPrimitive();
            Boolean rightPrimitive;
            rightPrimitive = that.isPrimitive();
            if (this.primitive!= null) {
                if (that.primitive!= null) {
                    if (!leftPrimitive.equals(rightPrimitive)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.primitive!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftSynthetic;
            leftSynthetic = this.isSynthetic();
            Boolean rightSynthetic;
            rightSynthetic = that.isSynthetic();
            if (this.synthetic!= null) {
                if (that.synthetic!= null) {
                    if (!leftSynthetic.equals(rightSynthetic)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.synthetic!= null) {
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
            StringList theAnnotations;
            theAnnotations = this.getAnnotations();
            if (this.annotations!= null) {
                currentHashCode += theAnnotations.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            ModifierList theModifiers;
            theModifiers = this.getModifiers();
            if (this.modifiers!= null) {
                currentHashCode += theModifiers.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            StringList theParameterizedTypes;
            theParameterizedTypes = this.getParameterizedTypes();
            if (this.parameterizedTypes!= null) {
                currentHashCode += theParameterizedTypes.hashCode();
            }
        }
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
            String theClassName;
            theClassName = this.getClassName();
            if (this.className!= null) {
                currentHashCode += theClassName.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theCanonicalClassName;
            theCanonicalClassName = this.getCanonicalClassName();
            if (this.canonicalClassName!= null) {
                currentHashCode += theCanonicalClassName.hashCode();
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
            String theGetMethod;
            theGetMethod = this.getGetMethod();
            if (this.getMethod!= null) {
                currentHashCode += theGetMethod.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theSetMethod;
            theSetMethod = this.getSetMethod();
            if (this.setMethod!= null) {
                currentHashCode += theSetMethod.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean thePrimitive;
            thePrimitive = this.isPrimitive();
            if (this.primitive!= null) {
                currentHashCode += thePrimitive.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theSynthetic;
            theSynthetic = this.isSynthetic();
            if (this.synthetic!= null) {
                currentHashCode += theSynthetic.hashCode();
            }
        }
        return currentHashCode;
    }

}
