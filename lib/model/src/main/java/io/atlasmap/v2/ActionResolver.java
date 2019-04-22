package io.atlasmap.v2;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ActionResolver implements TypeIdResolver {

    private static SimpleResolver INSTANCE = new SimpleResolver();
    static {
        INSTANCE.init(TypeFactory.defaultInstance().constructType(Action.class));
    }

    private ActionResolver() {
    }

    public static String toId(Class<?> aClass) {
        return INSTANCE.idFromValueAndType(null, aClass);
    }

    public static Class<? extends Action> fromId(String id) throws IOException {
        return (Class<? extends Action>) INSTANCE.typeFromId(null, id).getRawClass();
    }

    @Override
    public void init(JavaType baseType) {
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return INSTANCE.getMechanism();
    }

    @Override
    public String idFromValue(Object value) {
        return INSTANCE.idFromValue(value);
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> aClass) {
        return INSTANCE.idFromValueAndType(value, aClass);
    }

    @Override
    public String idFromBaseType() {
        return INSTANCE.idFromBaseType();
    }

    @Override
    public JavaType typeFromId(DatabindContext databindContext, String id) throws IOException {
        return INSTANCE.typeFromId(databindContext, id);
    }

    @Override
    public String getDescForKnownTypeIds() {
        return INSTANCE.getDescForKnownTypeIds();
    }
}
