package io.atlasmap.v2;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonRootName("ProcessMappingResponse")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class ProcessMappingResponse implements Serializable {

    private final static long serialVersionUID = 1L;

    protected Mapping mapping;

    protected Audits audits;

    protected AtlasMappingResult atlasMappingResult;

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
     * Gets the value of the audits property.
     * 
     * @return
     *     possible object is
     *     {@link Audits }
     *     
     */
    public Audits getAudits() {
        return audits;
    }

    /**
     * Sets the value of the audits property.
     * 
     * @param value
     *     allowed object is
     *     {@link Audits }
     *     
     */
    public void setAudits(Audits value) {
        this.audits = value;
    }

    /**
     * Gets the value of the atlasMappingResult property.
     * 
     * @return
     *     possible object is
     *     {@link AtlasMappingResult }
     *     
     */
    public AtlasMappingResult getAtlasMappingResult() {
        return atlasMappingResult;
    }

    /**
     * Sets the value of the atlasMappingResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link AtlasMappingResult }
     *     
     */
    public void setAtlasMappingResult(AtlasMappingResult value) {
        this.atlasMappingResult = value;
    }

}
