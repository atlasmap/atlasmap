package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class Repeat extends Action implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer count;

    public Integer getCount() {
        return count;
    }

    @JsonPropertyDescription("count ")
    @AtlasActionProperty(title = "count", type = FieldType.INTEGER)
    public void setCount(Integer count) {
        this.count = count;
    }
}

