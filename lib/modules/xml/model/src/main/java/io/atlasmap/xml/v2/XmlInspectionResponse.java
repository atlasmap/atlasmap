package io.atlasmap.xml.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonRootName("XmlInspectionResponse")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class XmlInspectionResponse implements Serializable {

    private final static long serialVersionUID = 1L;

    protected XmlDocument xmlDocument;

    protected String errorMessage;

    protected Long executionTime;

    /**
     * Gets the value of the xmlDocument property.
     * 
     * @return
     *     possible object is
     *     {@link XmlDocument }
     *     
     */
    public XmlDocument getXmlDocument() {
        return xmlDocument;
    }

    /**
     * Sets the value of the xmlDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlDocument }
     *     
     */
    public void setXmlDocument(XmlDocument value) {
        this.xmlDocument = value;
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    /**
     * Gets the value of the executionTime property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getExecutionTime() {
        return executionTime;
    }

    /**
     * Sets the value of the executionTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setExecutionTime(Long value) {
        this.executionTime = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final XmlInspectionResponse that = ((XmlInspectionResponse) object);
        {
            XmlDocument leftXmlDocument;
            leftXmlDocument = this.getXmlDocument();
            XmlDocument rightXmlDocument;
            rightXmlDocument = that.getXmlDocument();
            if (this.xmlDocument!= null) {
                if (that.xmlDocument!= null) {
                    if (!leftXmlDocument.equals(rightXmlDocument)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.xmlDocument!= null) {
                    return false;
                }
            }
        }
        {
            String leftErrorMessage;
            leftErrorMessage = this.getErrorMessage();
            String rightErrorMessage;
            rightErrorMessage = that.getErrorMessage();
            if (this.errorMessage!= null) {
                if (that.errorMessage!= null) {
                    if (!leftErrorMessage.equals(rightErrorMessage)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.errorMessage!= null) {
                    return false;
                }
            }
        }
        {
            Long leftExecutionTime;
            leftExecutionTime = this.getExecutionTime();
            Long rightExecutionTime;
            rightExecutionTime = that.getExecutionTime();
            if (this.executionTime!= null) {
                if (that.executionTime!= null) {
                    if (!leftExecutionTime.equals(rightExecutionTime)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.executionTime!= null) {
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
            XmlDocument theXmlDocument;
            theXmlDocument = this.getXmlDocument();
            if (this.xmlDocument!= null) {
                currentHashCode += theXmlDocument.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theErrorMessage;
            theErrorMessage = this.getErrorMessage();
            if (this.errorMessage!= null) {
                currentHashCode += theErrorMessage.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Long theExecutionTime;
            theExecutionTime = this.getExecutionTime();
            if (this.executionTime!= null) {
                currentHashCode += theExecutionTime.hashCode();
            }
        }
        return currentHashCode;
    }

}
