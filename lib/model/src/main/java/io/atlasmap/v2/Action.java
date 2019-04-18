package io.atlasmap.v2;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
    property = "@type"
)
@JsonTypeIdResolver(SimpleResolver.class)
public abstract class Action implements Serializable, FieldAction {

    private final static long serialVersionUID = 1L;

}
