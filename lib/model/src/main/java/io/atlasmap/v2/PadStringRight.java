package io.atlasmap.v2;

import java.io.Serializable;
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
    public void setPadCount(Integer value) {
        this.padCount = value;
    }

}
