package io.atlasmap.java.core.accessor;

import java.lang.reflect.Type;

import io.atlasmap.api.AtlasException;

public class RootAccessor extends JavaChildAccessor {

    public RootAccessor(Object parent) {
        super(parent, null);
    }

    @Override
    public Object getRawValue() throws AtlasException {
        return getParentObject();
    }

    @Override
    public Type getRawGenericType() throws AtlasException {
        return null;
    }

    @Override
    public Class<?> getRawClass() throws AtlasException {
        return getParentObject().getClass();
    }

}