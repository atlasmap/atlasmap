package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
public class PadStringRight extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String padCharacter;

    protected Integer padCount;

    /**
     * Gets the value of the padCharacter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPadCharacter() {
        return padCharacter;
    }

    /**
     * Sets the value of the padCharacter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The character to fill padding")
    @AtlasActionProperty(title = "Padding character", type = FieldType.STRING)
    public void setPadCharacter(String value) {
        this.padCharacter = value;
    }

    /**
     * Gets the value of the padCount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPadCount() {
        return padCount;
    }

    /**
     * Sets the value of the padCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    @JsonPropertyDescription("The number of padding character to fill")
    @AtlasActionProperty(title = "Padding count", type = FieldType.STRING)
    public void setPadCount(Integer value) {
        this.padCount = value;
    }

}
