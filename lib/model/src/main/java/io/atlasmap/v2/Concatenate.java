package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class Concatenate extends Action implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String delimiter;

    protected Boolean delimitingEmptyValues = true;

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
     * @return
     *     <code>true</code> if delimiting empty values
     */
    public Boolean getDelimitingEmptyValues() {
        return delimitingEmptyValues;
    }

    // !!!! Warning !!!! these setters must be kept in alphabetical order

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

    /**
     * @param delimitingEmptyValues
     *     <code>true</code> if delimiting empty values
     */
    @JsonPropertyDescription("Determines if delimiters are added around empty values")
    @AtlasActionProperty(title = "Delimit empty values", type = FieldType.BOOLEAN)
    public void setDelimitingEmptyValues(Boolean delimitingEmptyValues) {
        this.delimitingEmptyValues = delimitingEmptyValues;
    }
}
