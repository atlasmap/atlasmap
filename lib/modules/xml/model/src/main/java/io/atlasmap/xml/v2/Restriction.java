package io.atlasmap.xml.v2;

import java.io.Serializable;

public class Restriction implements Serializable {

    private final static long serialVersionUID = 1L;

    protected RestrictionType type;

    protected String value;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link RestrictionType }
     *     
     */
    public RestrictionType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link RestrictionType }
     *     
     */
    public void setType(RestrictionType value) {
        this.type = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final Restriction that = ((Restriction) object);
        {
            RestrictionType leftType;
            leftType = this.getType();
            RestrictionType rightType;
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
        {
            String leftValue;
            leftValue = this.getValue();
            String rightValue;
            rightValue = that.getValue();
            if (this.value!= null) {
                if (that.value!= null) {
                    if (!leftValue.equals(rightValue)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.value!= null) {
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
            RestrictionType theType;
            theType = this.getType();
            if (this.type!= null) {
                currentHashCode += theType.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theValue;
            theValue = this.getValue();
            if (this.value!= null) {
                currentHashCode += theValue.hashCode();
            }
        }
        return currentHashCode;
    }

}
