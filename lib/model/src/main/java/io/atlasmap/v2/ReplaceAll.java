package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
public class ReplaceAll extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String match;

    protected String newString;

    /**
     * Gets the value of the match property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMatch() {
        return match;
    }

    /**
     * Sets the value of the match property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The string to match")
    @AtlasActionProperty(title = "Match", type = FieldType.STRING)
    public void setMatch(String value) {
        this.match = value;
    }

    /**
     * Gets the value of the newString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewString() {
        return newString;
    }

    /**
     * Sets the value of the newString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The string to replace with")
    @AtlasActionProperty(title = "New string", type = FieldType.STRING)
    public void setNewString(String value) {
        this.newString = value;
    }

}
