package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class Expression extends Action implements Serializable {

    private final static long serialVersionUID = 1L;

    protected String expression;

    /**
     * Gets the value of the string property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Sets the value of the string property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @JsonPropertyDescription("The expression string to evaluate")
    @AtlasActionProperty(title = "Expression", type = FieldType.STRING)
    public void setExpression(String expression) {
        this.expression = expression;
    }

}
