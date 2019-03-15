package io.atlasmap.v2;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class Json {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new AtlasJsonModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .configure(SerializationFeature.WRAP_ROOT_VALUE, true)
        .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true)
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .setSerializationInclusion(Include.NON_NULL);

    private Json() {
    }

    public static ObjectMapper mapper() {
        return OBJECT_MAPPER;
    }

    public static ObjectMapper withClassLoader(ClassLoader classLoader) {
        return OBJECT_MAPPER.setTypeFactory(TypeFactory.defaultInstance().withClassLoader(classLoader));
    }
}
