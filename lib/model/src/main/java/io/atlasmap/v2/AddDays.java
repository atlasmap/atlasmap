package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class AddDays extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected Integer days;

    /**
     * Gets the value of the days property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDays() {
        return days;
    }

    /**
     * Sets the value of the days property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    @JsonPropertyDescription("The number of days to add")
    @AtlasActionProperty(title = "Days", type = FieldType.STRING)
    public void setDays(Integer value) {
        this.days = value;
    }

}
