package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionParameter;

abstract class BasePadString extends Action implements AtlasFieldAction
{
    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter
    private String padCharacter;
    @AtlasFieldActionParameter(type = FieldType.INTEGER)
    private Integer padCount;

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
