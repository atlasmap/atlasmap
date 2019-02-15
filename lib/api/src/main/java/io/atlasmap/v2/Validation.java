package io.atlasmap.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("Validation")
public class Validation implements Serializable {

    private final static long serialVersionUID = 1L;
    protected String message;

    protected String id;

    protected ValidationScope scope;

    protected ValidationStatus status;

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link ValidationScope }
     *     
     */
    public ValidationScope getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValidationScope }
     *     
     */
    public void setScope(ValidationScope value) {
        this.scope = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link ValidationStatus }
     *     
     */
    public ValidationStatus getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link ValidationStatus }
     *     
     */
    public void setStatus(ValidationStatus value) {
        this.status = value;
    }

}
