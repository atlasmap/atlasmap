package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class ItemAt extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected Integer index;

    /**
     * Gets the value of the index property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    @JsonPropertyDescription("The collection index to pick an item from")
    @AtlasActionProperty(title = "Index", type = FieldType.STRING)
    public void setIndex(Integer value) {
        this.index = value;
    }

}
