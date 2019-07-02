package io.atlasmap.maven.test;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.atlasmap.v2.Action;
import io.atlasmap.v2.AtlasActionProperty;
import io.atlasmap.v2.FieldType;

public class PaulsFieldActionsModel extends Action implements Serializable {

    private final static long serialVersionUID = 1L;
    private String paulsParam = "";
    /**
     * Example of a custom field action with a string parameter.
     * 
     * @param value
     *     allowed object is
     *     {@link String}
     *     
     */
    @JsonPropertyDescription("Paul's custom field action parameter to display")
    @AtlasActionProperty(title = "Paul's custom field action string parameter", type = FieldType.STRING)
    public void setPaulsParam(String value) {
	paulsParam = value;
    }

    public String getPaulsParam() {
	return paulsParam;
    }
}
