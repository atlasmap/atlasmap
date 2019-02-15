package io.atlasmap.v2;

import java.lang.reflect.Method;

import io.atlasmap.spi.AtlasFieldActionInfo;

public interface FieldAction {

    default String getDisplayName() {
        for (Method method : getClass().getMethods()) {
            AtlasFieldActionInfo anno = method.getAnnotation(AtlasFieldActionInfo.class);
            if (anno != null) {
                return anno.name();
            }
        }
        return this.getClass().getSimpleName();
    }
}
