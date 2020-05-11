package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "fieldPaths"
)

@JsonTypeIdResolver(ActionResolver.class)
public abstract class OneToManyAction extends Action implements Serializable {

    private final static long serialVersionUID = 1L;
    
    protected String fieldPath;
    private int fieldPathCount;

    @JsonProperty("fieldPath")
    public String getFieldPath() {
        return fieldPath;
    }
    
    @JsonPropertyDescription("The fieldPath")
    @AtlasActionProperty(title = "fieldPath", type = FieldType.STRING)
    public void setFieldPath(String value) {
        this.fieldPath = value;
    }
    
    //How to avoid exposing this field to UI?
    @JsonProperty("fieldPathCount")
    public int getFieldPathCount() {
        return fieldPathCount;
    }
    
    @JsonPropertyDescription("fieldPath Count ")
    @AtlasActionProperty(title = "fieldPathCount", type = FieldType.INTEGER)
    public void setFieldPathCount(int value) {
        this.fieldPathCount = value;
    }
}
