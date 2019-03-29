package io.atlasmap.java.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.atlasmap.v2.Field;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class JavaEnumField extends Field implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String name;

    protected Integer ordinal;

    protected String className;

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
        final JavaEnumField that = ((JavaEnumField) object);
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
            String theClassName;
            theClassName = this.getClassName();
            if (this.className!= null) {
                currentHashCode += theClassName.hashCode();
            }
        }
        return currentHashCode;
    }

}
