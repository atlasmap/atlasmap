package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
public class Append extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String string;

    /**
     * Gets the value of the string property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getString() {
        return string;
    }

    /**
     * Sets the value of the string property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The string to append after input string")
    @AtlasActionProperty(title = "String", type = FieldType.STRING)
    public void setString(String value) {
        this.string = value;
    }

}
