package io.atlasmap.xml.v2;

import java.io.Serializable;

public class XmlNamespace implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String alias;

    protected String uri;

    protected String locationUri;

    protected Boolean targetNamespace;

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
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
     * Gets the value of the locationUri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocationUri() {
        return locationUri;
    }

    /**
     * Sets the value of the locationUri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocationUri(String value) {
        this.locationUri = value;
    }

    /**
     * Gets the value of the targetNamespace property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTargetNamespace() {
        return targetNamespace;
    }

    /**
     * Sets the value of the targetNamespace property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTargetNamespace(Boolean value) {
        this.targetNamespace = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final XmlNamespace that = ((XmlNamespace) object);
        {
            String leftAlias;
            leftAlias = this.getAlias();
            String rightAlias;
            rightAlias = that.getAlias();
            if (this.alias!= null) {
                if (that.alias!= null) {
                    if (!leftAlias.equals(rightAlias)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.alias!= null) {
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
            String leftLocationUri;
            leftLocationUri = this.getLocationUri();
            String rightLocationUri;
            rightLocationUri = that.getLocationUri();
            if (this.locationUri!= null) {
                if (that.locationUri!= null) {
                    if (!leftLocationUri.equals(rightLocationUri)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.locationUri!= null) {
                    return false;
                }
            }
        }
        {
            Boolean leftTargetNamespace;
            leftTargetNamespace = this.isTargetNamespace();
            Boolean rightTargetNamespace;
            rightTargetNamespace = that.isTargetNamespace();
            if (this.targetNamespace!= null) {
                if (that.targetNamespace!= null) {
                    if (!leftTargetNamespace.equals(rightTargetNamespace)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.targetNamespace!= null) {
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
            String theAlias;
            theAlias = this.getAlias();
            if (this.alias!= null) {
                currentHashCode += theAlias.hashCode();
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
            String theLocationUri;
            theLocationUri = this.getLocationUri();
            if (this.locationUri!= null) {
                currentHashCode += theLocationUri.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Boolean theTargetNamespace;
            theTargetNamespace = this.isTargetNamespace();
            if (this.targetNamespace!= null) {
                currentHashCode += theTargetNamespace.hashCode();
            }
        }
        return currentHashCode;
    }

}
