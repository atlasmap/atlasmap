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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import io.atlasmap.api.AtlasException;

public class GetterAccessor extends JavaChildAccessor {

    private Method getter;

    public GetterAccessor(Object parent, String name, Method getter) {
        super(parent, name);
        getter.setAccessible(true);
        this.getter = getter;
    }

    @Override
    public Object getRawValue() throws AtlasException {
        try {
            return getter.invoke(getParentObject(), new Object[0]);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    @Override
    public Class<?> getRawClass() {
        return getter.getReturnType();
    }

    @Override
    public Type getRawGenericType() throws AtlasException {
        return getter.getGenericReturnType();
    }

}
