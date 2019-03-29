package io.atlasmap.v2;

import java.io.Serializable;

public class ConvertVolumeUnit extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected VolumeUnitType fromUnit;

    protected VolumeUnitType toUnit;

    /**
     * Gets the value of the fromUnit property.
     * 
     * @return
     *     possible object is
     *     {@link VolumeUnitType }
     *     
     */
    public VolumeUnitType getFromUnit() {
        return fromUnit;
    }

    /**
     * Sets the value of the fromUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link VolumeUnitType }
     *     
     */
    public void setFromUnit(VolumeUnitType value) {
        this.fromUnit = value;
    }

    /**
     * Gets the value of the toUnit property.
     * 
     * @return
     *     possible object is
     *     {@link VolumeUnitType }
     *     
     */
    public VolumeUnitType getToUnit() {
        return toUnit;
    }

    /**
     * Sets the value of the toUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link VolumeUnitType }
     *     
     */
    public void setToUnit(VolumeUnitType value) {
        this.toUnit = value;
    }

}
