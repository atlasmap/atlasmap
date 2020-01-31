package io.atlasmap.xml.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.StringList;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class XmlField extends Field implements Serializable {

    private final static long serialVersionUID = 1L;

    protected StringList annotations;

    protected Restrictions restrictions;

    protected String name;

    protected NodeType nodeType;

    protected Boolean primitive;

    protected String typeName;

    protected Boolean userCreated;

    protected Boolean attribute;

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
     * Gets the value of the restrictions property.
     * 
     * @return
     *     possible object is
     *     {@link Restrictions }
     *     
     */
    public Restrictions getRestrictions() {
        return restrictions;
    }

    /**
     * Sets the value of the restrictions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Restrictions }
     *     
     */
    public void setRestrictions(Restrictions value) {
        this.restrictions = value;
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
     * Gets the value of the nodeType property.
     * 
     * @return
     *     possible object is
     *     {@link NodeType }
     *     
     */
    public NodeType getNodeType() {
        return nodeType;
    }

    /**
     * Sets the value of the nodeType property.
     * 
     * @param value
     *     allowed object is
     *     {@link NodeType }
     *     
     */
    public void setNodeType(NodeType value) {
        this.nodeType = value;
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
        final XmlField that = ((XmlField) object);
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
            Restrictions leftRestrictions;
            leftRestrictions = this.getRestrictions();
            Restrictions rightRestrictions;
            rightRestrictions = that.getRestrictions();
            if (this.restrictions!= null) {
                if (that.restrictions!= null) {
                    if (!leftRestrictions.equals(rightRestrictions)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.restrictions!= null) {
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
            NodeType leftNodeType;
            leftNodeType = this.getNodeType();
            NodeType rightNodeType;
            rightNodeType = that.getNodeType();
            if (this.nodeType!= null) {
                if (that.nodeType!= null) {
                    if (!leftNodeType.equals(rightNodeType)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.nodeType!= null) {
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
        {
            Boolean leftAttribute = this.isAttribute();
            Boolean rightAttribute = that.isAttribute();
            if (this.attribute != null) {
                if (that.attribute != null) {
                    if (!leftAttribute.equals(rightAttribute)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.attribute != null) {
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
            Restrictions theRestrictions;
            theRestrictions = this.getRestrictions();
            if (this.restrictions!= null) {
                currentHashCode += theRestrictions.hashCode();
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
            NodeType theNodeType;
            theNodeType = this.getNodeType();
            if (this.nodeType!= null) {
                currentHashCode += theNodeType.hashCode();
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
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theAttribute = this.isAttribute();
            if (this.attribute != null) {
                currentHashCode += theAttribute.hashCode();
            }
        }
        return currentHashCode;
    }

}
