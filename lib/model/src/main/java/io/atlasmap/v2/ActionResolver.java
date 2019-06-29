package io.atlasmap.v2;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ActionResolver implements TypeIdResolver {

    private static ActionResolver instance;
    private SimpleResolver delegate;

    private ActionResolver() {
        this(null);
    }

    private ActionResolver(ClassLoader classLoader) {
        delegate = new SimpleResolver();
        delegate.setClassLoader(classLoader);
        delegate.init(TypeFactory.defaultInstance().constructType(Action.class));
    }

    public static ActionResolver getInstance(ClassLoader classLoader) {
        instance = new ActionResolver(classLoader);
        return instance;
    }

    public static ActionResolver getInstance() {
        if (instance == null) {
            instance = new ActionResolver();
        }
        return instance;
    }

    public String toId(Class<?> aClass) {
        return delegate.idFromValueAndType(null, aClass);
    }

    public Class<? extends Action> fromId(String id) throws IOException {
        return (Class<? extends Action>) delegate.typeFromId(null, id).getRawClass();
    }

    @Override
    public void init(JavaType baseType) {
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return delegate.getMechanism();
    }

    @Override
    public String idFromValue(Object value) {
        return delegate.idFromValue(value);
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> aClass) {
        return delegate.idFromValueAndType(value, aClass);
    }

    @Override
    public String idFromBaseType() {
        return delegate.idFromBaseType();
    }

    @Override
    public JavaType typeFromId(DatabindContext databindContext, String id) throws IOException {
        return delegate.typeFromId(databindContext, id);
    }

    @Override
    public String getDescForKnownTypeIds() {
        return delegate.getDescForKnownTypeIds();
    }
}
