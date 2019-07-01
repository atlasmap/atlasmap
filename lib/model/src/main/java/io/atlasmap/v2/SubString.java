package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class SubString extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected Integer startIndex;

    protected Integer endIndex;

    /**
     * Gets the value of the startIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the value of the startIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    @JsonPropertyDescription("The start index to substring")
    @AtlasActionProperty(title = "Start index", type = FieldType.STRING)
    public void setStartIndex(Integer value) {
        this.startIndex = value;
    }

    /**
     * Gets the value of the endIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getEndIndex() {
        return endIndex;
    }

    /**
     * Sets the value of the endIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    @JsonPropertyDescription("The end index to substring")
    @AtlasActionProperty(title = "End index", type = FieldType.STRING)
    public void setEndIndex(Integer value) {
        this.endIndex = value;
    }

}
