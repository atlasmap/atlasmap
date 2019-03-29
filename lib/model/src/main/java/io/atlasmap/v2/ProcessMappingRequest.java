package io.atlasmap.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonRootName("ProcessMappingRequest")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class ProcessMappingRequest implements Serializable {

    private final static long serialVersionUID = 1L;

    protected Mapping mapping;

    protected AtlasMapping atlasMapping;

    /**
     * Gets the value of the mapping property.
     * 
     * @return
     *     possible object is
     *     {@link Mapping }
     *     
     */
    public Mapping getMapping() {
        return mapping;
    }

    /**
     * Sets the value of the mapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mapping }
     *     
     */
    public void setMapping(Mapping value) {
        this.mapping = value;
    }

    /**
     * Gets the value of the atlasMapping property.
     * 
     * @return
     *     possible object is
     *     {@link AtlasMapping }
     *     
     */
    public AtlasMapping getAtlasMapping() {
        return atlasMapping;
    }

    /**
     * Sets the value of the atlasMapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link AtlasMapping }
     *     
     */
    public void setAtlasMapping(AtlasMapping value) {
        this.atlasMapping = value;
    }

}
