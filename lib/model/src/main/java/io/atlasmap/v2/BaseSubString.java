package io.atlasmap.v2;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionParameter;

abstract class BaseSubString extends Action implements AtlasFieldAction {

    private final static long serialVersionUID = 1L;

    @AtlasFieldActionParameter(type = FieldType.INTEGER)
    private Integer startIndex;
    @AtlasFieldActionParameter(type = FieldType.INTEGER)
    private Integer endIndex;

    protected String subString(String input, Integer startIndex, Integer endIndex) {
        if (endIndex == null) {
            return input.substring(startIndex);
        }

        return input.substring(startIndex, endIndex);
    }

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
    public void setEndIndex(Integer value) {
        this.endIndex = value;
    }

}
