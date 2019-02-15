package io.atlasmap.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public abstract class BaseMapping implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String alias;

    protected String description;

    protected MappingType mappingType;

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the mappingType property.
     * 
     * @return
     *     possible object is
     *     {@link MappingType }
     *     
     */
    public MappingType getMappingType() {
        return mappingType;
    }

    /**
     * Sets the value of the mappingType property.
     * 
     * @param value
     *     allowed object is
     *     {@link MappingType }
     *     
     */
    public void setMappingType(MappingType value) {
        this.mappingType = value;
    }

}
