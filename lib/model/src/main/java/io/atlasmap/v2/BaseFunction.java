package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "@type")
@JsonTypeIdResolver(FunctionResolver.class)
public abstract class BaseFunction implements Serializable, Function {

    private static final long serialVersionUID = 1L;

    @JsonProperty("@type")
    public String getType() {
        return FunctionResolver.getInstance().toId(getClass());
    }

}
