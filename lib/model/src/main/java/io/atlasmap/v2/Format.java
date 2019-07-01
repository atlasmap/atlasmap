package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
public class Format extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String template;

    /**
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The template string")
    @AtlasActionProperty(title = "Template", type = FieldType.STRING)
    public void setTemplate(String value) {
        this.template = value;
    }

}
