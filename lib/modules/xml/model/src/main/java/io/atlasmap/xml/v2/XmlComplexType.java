package io.atlasmap.xml.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class XmlComplexType extends XmlField implements Serializable {

    private final static long serialVersionUID = 1L;

    protected XmlEnumFields xmlEnumFields;

    protected XmlFields xmlFields;

    protected Boolean annotation;

    protected Boolean annonymous;

    protected Boolean enumeration;

    protected String uri;

    /**
     * Gets the value of the xmlEnumFields property.
     * 
     * @return
     *     possible object is
     *     {@link XmlEnumFields }
     *     
     */
    public XmlEnumFields getXmlEnumFields() {
        return xmlEnumFields;
    }

    /**
     * Sets the value of the xmlEnumFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlEnumFields }
     *     
     */
    public void setXmlEnumFields(XmlEnumFields value) {
        this.xmlEnumFields = value;
    }

    /**
     * Gets the value of the xmlFields property.
     * 
     * @return
     *     possible object is
     *     {@link XmlFields }
     *     
     */
    public XmlFields getXmlFields() {
        return xmlFields;
    }

    /**
     * Sets the value of the xmlFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlFields }
     *     
     */
    public void setXmlFields(XmlFields value) {
        this.xmlFields = value;
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
        final XmlComplexType that = ((XmlComplexType) object);
        {
            XmlEnumFields leftXmlEnumFields;
            leftXmlEnumFields = this.getXmlEnumFields();
            XmlEnumFields rightXmlEnumFields;
            rightXmlEnumFields = that.getXmlEnumFields();
            if (this.xmlEnumFields!= null) {
                if (that.xmlEnumFields!= null) {
                    if (!leftXmlEnumFields.equals(rightXmlEnumFields)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.xmlEnumFields!= null) {
                    return false;
                }
            }
        }
        {
            XmlFields leftXmlFields;
            leftXmlFields = this.getXmlFields();
            XmlFields rightXmlFields;
            rightXmlFields = that.getXmlFields();
            if (this.xmlFields!= null) {
                if (that.xmlFields!= null) {
                    if (!leftXmlFields.equals(rightXmlFields)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.xmlFields!= null) {
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
            XmlEnumFields theXmlEnumFields;
            theXmlEnumFields = this.getXmlEnumFields();
            if (this.xmlEnumFields!= null) {
                currentHashCode += theXmlEnumFields.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            XmlFields theXmlFields;
            theXmlFields = this.getXmlFields();
            if (this.xmlFields!= null) {
                currentHashCode += theXmlFields.hashCode();
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
            String theUri;
            theUri = this.getUri();
            if (this.uri!= null) {
                currentHashCode += theUri.hashCode();
            }
        }
        return currentHashCode;
    }

}
