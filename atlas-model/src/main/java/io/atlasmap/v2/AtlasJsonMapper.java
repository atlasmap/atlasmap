package io.atlasmap.v2;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class AtlasJsonMapper extends ObjectMapper {

    private static final long serialVersionUID = 6675369226992343500L;

    public AtlasJsonMapper() {
        registerModule(new AtlasJsonModule());
        enable(SerializationFeature.INDENT_OUTPUT);
        configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        setSerializationInclusion(Include.NON_NULL);
    }
}