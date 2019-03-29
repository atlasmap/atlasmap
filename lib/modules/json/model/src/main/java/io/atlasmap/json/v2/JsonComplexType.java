package io.atlasmap.json.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class JsonComplexType extends JsonField implements Serializable {

    private final static long serialVersionUID = 1L;

    protected JsonFields jsonFields;

    protected String uri;

    /**
     * Gets the value of the jsonFields property.
     * 
     * @return
     *     possible object is
     *     {@link JsonFields }
     *     
     */
    public JsonFields getJsonFields() {
        return jsonFields;
    }

    /**
     * Sets the value of the jsonFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link JsonFields }
     *     
     */
    public void setJsonFields(JsonFields value) {
        this.jsonFields = value;
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
        final JsonComplexType that = ((JsonComplexType) object);
        {
            JsonFields leftJsonFields;
            leftJsonFields = this.getJsonFields();
            JsonFields rightJsonFields;
            rightJsonFields = that.getJsonFields();
            if (this.jsonFields!= null) {
                if (that.jsonFields!= null) {
                    if (!leftJsonFields.equals(rightJsonFields)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.jsonFields!= null) {
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
            JsonFields theJsonFields;
            theJsonFields = this.getJsonFields();
            if (this.jsonFields!= null) {
                currentHashCode += theJsonFields.hashCode();
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
