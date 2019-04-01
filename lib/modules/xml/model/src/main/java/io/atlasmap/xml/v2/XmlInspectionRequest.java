package io.atlasmap.xml.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.atlasmap.v2.StringList;

@JsonRootName("XmlInspectionRequest")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class XmlInspectionRequest implements Serializable {

    private final static long serialVersionUID = 1L;

    protected StringList fieldNameBlacklist;

    protected StringList typeNameBlacklist;

    protected StringList namespaceBlacklist;

    protected String xmlData;

    protected String uri;

    protected InspectionType type;

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
     * Gets the value of the typeNameBlacklist property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getTypeNameBlacklist() {
        return typeNameBlacklist;
    }

    /**
     * Sets the value of the typeNameBlacklist property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setTypeNameBlacklist(StringList value) {
        this.typeNameBlacklist = value;
    }

    /**
     * Gets the value of the namespaceBlacklist property.
     * 
     * @return
     *     possible object is
     *     {@link StringList }
     *     
     */
    public StringList getNamespaceBlacklist() {
        return namespaceBlacklist;
    }

    /**
     * Sets the value of the namespaceBlacklist property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringList }
     *     
     */
    public void setNamespaceBlacklist(StringList value) {
        this.namespaceBlacklist = value;
    }

    /**
     * Gets the value of the xmlData property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getXmlData() {
        return xmlData;
    }

    /**
     * Sets the value of the xmlData property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setXmlData(String value) {
        this.xmlData = value;
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
        final XmlInspectionRequest that = ((XmlInspectionRequest) object);
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
            StringList leftTypeNameBlacklist;
            leftTypeNameBlacklist = this.getTypeNameBlacklist();
            StringList rightTypeNameBlacklist;
            rightTypeNameBlacklist = that.getTypeNameBlacklist();
            if (this.typeNameBlacklist!= null) {
                if (that.typeNameBlacklist!= null) {
                    if (!leftTypeNameBlacklist.equals(rightTypeNameBlacklist)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.typeNameBlacklist!= null) {
                    return false;
                }
            }
        }
        {
            StringList leftNamespaceBlacklist;
            leftNamespaceBlacklist = this.getNamespaceBlacklist();
            StringList rightNamespaceBlacklist;
            rightNamespaceBlacklist = that.getNamespaceBlacklist();
            if (this.namespaceBlacklist!= null) {
                if (that.namespaceBlacklist!= null) {
                    if (!leftNamespaceBlacklist.equals(rightNamespaceBlacklist)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.namespaceBlacklist!= null) {
                    return false;
                }
            }
        }
        {
            String leftXmlData;
            leftXmlData = this.getXmlData();
            String rightXmlData;
            rightXmlData = that.getXmlData();
            if (this.xmlData!= null) {
                if (that.xmlData!= null) {
                    if (!leftXmlData.equals(rightXmlData)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.xmlData!= null) {
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
            StringList theFieldNameBlacklist;
            theFieldNameBlacklist = this.getFieldNameBlacklist();
            if (this.fieldNameBlacklist!= null) {
                currentHashCode += theFieldNameBlacklist.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            StringList theTypeNameBlacklist;
            theTypeNameBlacklist = this.getTypeNameBlacklist();
            if (this.typeNameBlacklist!= null) {
                currentHashCode += theTypeNameBlacklist.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            StringList theNamespaceBlacklist;
            theNamespaceBlacklist = this.getNamespaceBlacklist();
            if (this.namespaceBlacklist!= null) {
                currentHashCode += theNamespaceBlacklist.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theXmlData;
            theXmlData = this.getXmlData();
            if (this.xmlData!= null) {
                currentHashCode += theXmlData.hashCode();
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
