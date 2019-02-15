package io.atlasmap.v2;

import java.io.Serializable;

public class LookupEntry implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String sourceValue;

    protected FieldType sourceType;

    protected String targetValue;

    protected FieldType targetType;

    /**
     * Gets the value of the sourceValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceValue() {
        return sourceValue;
    }

    /**
     * Sets the value of the sourceValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceValue(String value) {
        this.sourceValue = value;
    }

    /**
     * Gets the value of the sourceType property.
     * 
     * @return
     *     possible object is
     *     {@link FieldType }
     *     
     */
    public FieldType getSourceType() {
        return sourceType;
    }

    /**
     * Sets the value of the sourceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldType }
     *     
     */
    public void setSourceType(FieldType value) {
        this.sourceType = value;
    }

    /**
     * Gets the value of the targetValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetValue() {
        return targetValue;
    }

    /**
     * Sets the value of the targetValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetValue(String value) {
        this.targetValue = value;
    }

    /**
     * Gets the value of the targetType property.
     * 
     * @return
     *     possible object is
     *     {@link FieldType }
     *     
     */
    public FieldType getTargetType() {
        return targetType;
    }

    /**
     * Sets the value of the targetType property.
     * 
     * @param value
     *     allowed object is
     *     {@link FieldType }
     *     
     */
    public void setTargetType(FieldType value) {
        this.targetType = value;
    }

}
