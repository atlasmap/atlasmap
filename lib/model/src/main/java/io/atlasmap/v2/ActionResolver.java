package io.atlasmap.v2;

import java.io.IOException;

import com.fasterxml.jackson.databind.type.TypeFactory;

public class ActionResolver {

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
}
