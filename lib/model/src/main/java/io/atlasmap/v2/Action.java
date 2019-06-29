package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "@type"
)
@JsonTypeIdResolver(ActionResolver.class)
public abstract class Action implements Serializable, FieldAction {

    private final static long serialVersionUID = 1L;

    @JsonProperty("@type")
    public String getType() {
        return ActionResolver.getInstance().toId(getClass());
    }

}
