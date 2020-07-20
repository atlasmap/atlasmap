package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("io.atlasmap.v2.PropertyField")
public class PropertyField extends Field implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String scope;

    /**
     * Gets the value of the scope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the value of the scope property.
     * 
     * @param scope
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

}
