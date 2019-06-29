package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class Concatenate extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String delimiter;

    /**
     * Gets the value of the delimiter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the value of the delimiter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The delimiter string to concatenate input strings with")
    @AtlasActionProperty(title = "Delimiter", type = FieldType.STRING)
    public void setDelimiter(String value) {
        this.delimiter = value;
    }

}
