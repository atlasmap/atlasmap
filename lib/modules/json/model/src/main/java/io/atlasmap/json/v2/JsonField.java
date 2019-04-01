package io.atlasmap.json.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.atlasmap.v2.Field;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class JsonField extends Field implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String name;

    protected Boolean primitive;

    protected String typeName;

    protected Boolean userCreated;

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
        final JsonField that = ((JsonField) object);
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
        {
            Boolean leftUserCreated;
            leftUserCreated = this.isUserCreated();
            Boolean rightUserCreated;
            rightUserCreated = that.isUserCreated();
            if (this.userCreated!= null) {
                if (that.userCreated!= null) {
                    if (!leftUserCreated.equals(rightUserCreated)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.userCreated!= null) {
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
            Boolean thePrimitive;
            thePrimitive = this.isPrimitive();
            if (this.primitive!= null) {
                currentHashCode += thePrimitive.hashCode();
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
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theUserCreated;
            theUserCreated = this.isUserCreated();
            if (this.userCreated!= null) {
                currentHashCode += theUserCreated.hashCode();
            }
        }
        return currentHashCode;
    }

}
