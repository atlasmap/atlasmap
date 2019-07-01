package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
public class Equals extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String value;

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
    @JsonPropertyDescription("The string to compare with")
    @AtlasActionProperty(title = "value", type = FieldType.STRING)
    public void setValue(String value) {
        this.value = value;
    }

}
