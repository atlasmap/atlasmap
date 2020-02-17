/**
 * Copyright (C) 2020 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.java.core.accessor;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import io.atlasmap.api.AtlasException;

public class FieldAccessor extends JavaChildAccessor {

    private Field field;

    public FieldAccessor(Object parent, String name, Field field) {
        super(parent, name);
        field.setAccessible(true);
        this.field = field;
    }

    @Override
    public Object getRawValue() throws AtlasException {
        try {
            return field.get(getParentObject());
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    @Override
    public Class<?> getRawClass() throws AtlasException {
        return field.getType();
    }

    @Override
    public Type getRawGenericType() throws AtlasException {
        return field.getGenericType();
    }

}
