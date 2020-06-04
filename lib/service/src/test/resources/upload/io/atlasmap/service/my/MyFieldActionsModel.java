package io.atlasmap.service.my;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import io.atlasmap.v2.Action;
import io.atlasmap.v2.AtlasActionProperty;
import io.atlasmap.v2.FieldType;

public class MyFieldActionsModel extends Action implements Serializable {

    private String param;

    /**
     * Example of a custom field action with a string parameter.
     * 
     * @param value
     *     allowed object is
     *     {@link String}
     *     
     */
    @JsonPropertyDescription("My custom field action parameter to display")
    @AtlasActionProperty(title = "My custom field action string parameter", type = FieldType.STRING)
    public void setParam(String value) {
        param = value;
    }

    public String getParam() {
    return param;
    }

}
