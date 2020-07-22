/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.v2;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class FunctionResolver extends TypeIdResolverBase {

    private static FunctionResolver instance;
    private SimpleResolver delegate;

    private FunctionResolver() {
        this(null);
    }

    private FunctionResolver(ClassLoader classLoader) {
        delegate = new SimpleResolver();
        delegate.setClassLoader(classLoader);
        delegate.init(TypeFactory.defaultInstance().constructType(Function.class));
    }

    public static FunctionResolver getInstance(ClassLoader classLoader) {
        instance = new FunctionResolver(classLoader);
        return instance;
    }

    public static FunctionResolver getInstance() {
        if (instance == null) {
            instance = new FunctionResolver();
        }
        return instance;
    }

    public String toId(Class<?> aClass) {
        return delegate.idFromValueAndType(null, aClass);
    }

    public Class<? extends Function> fromId(String id) throws IOException {
        return (Class<? extends Function>) delegate.typeFromId(null, id).getRawClass();
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
