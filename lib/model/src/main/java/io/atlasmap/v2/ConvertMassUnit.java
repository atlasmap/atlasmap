package io.atlasmap.v2;

import java.io.Serializable;

public class ConvertMassUnit extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected MassUnitType fromUnit;

    protected MassUnitType toUnit;

    /**
     * Gets the value of the fromUnit property.
     * 
     * @return
     *     possible object is
     *     {@link MassUnitType }
     *     
     */
    public MassUnitType getFromUnit() {
        return fromUnit;
    }

    /**
     * Sets the value of the fromUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link MassUnitType }
     *     
     */
    public void setFromUnit(MassUnitType value) {
        this.fromUnit = value;
    }

    /**
     * Gets the value of the toUnit property.
     * 
     * @return
     *     possible object is
     *     {@link MassUnitType }
     *     
     */
    public MassUnitType getToUnit() {
        return toUnit;
    }

    /**
     * Sets the value of the toUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link MassUnitType }
     *     
     */
    public void setToUnit(MassUnitType value) {
        this.toUnit = value;
    }

}
