
package io.atlasmap.xml.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.atlasmap.v2.Document;

@JsonRootName("XmlDocument")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class XmlDocument extends Document implements Serializable {

    private final static long serialVersionUID = 1L;

    protected XmlNamespaces xmlNamespaces;

    /**
     * Gets the value of the xmlNamespaces property.
     * 
     * @return
     *     possible object is
     *     {@link XmlNamespaces }
     *     
     */
    public XmlNamespaces getXmlNamespaces() {
        return xmlNamespaces;
    }

    /**
     * Sets the value of the xmlNamespaces property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlNamespaces }
     *     
     */
    public void setXmlNamespaces(XmlNamespaces value) {
        this.xmlNamespaces = value;
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
        final XmlDocument that = ((XmlDocument) object);
        {
            XmlNamespaces leftXmlNamespaces;
            leftXmlNamespaces = this.getXmlNamespaces();
            XmlNamespaces rightXmlNamespaces;
            rightXmlNamespaces = that.getXmlNamespaces();
            if (this.xmlNamespaces!= null) {
                if (that.xmlNamespaces!= null) {
                    if (!leftXmlNamespaces.equals(rightXmlNamespaces)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.xmlNamespaces!= null) {
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
            XmlNamespaces theXmlNamespaces;
            theXmlNamespaces = this.getXmlNamespaces();
            if (this.xmlNamespaces!= null) {
                currentHashCode += theXmlNamespaces.hashCode();
            }
        }
        return currentHashCode;
    }

}
