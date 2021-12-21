/*
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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * The jackson resolver class for the field action.
 */
public class ActionResolver extends TypeIdResolverBase {

    private static ActionResolver instance;
    private SimpleResolver delegate;
    private TypeFactory typeFactory;
    private ClassLoader classLoader;

    private ActionResolver() {
        init(ActionResolver.class.getClassLoader());
    }

    /**
     * Initializer.
     * @param cl class loader
     * @return this instance
     */
    public ActionResolver init(ClassLoader cl) {
        this.classLoader = cl;
        typeFactory = TypeFactory.defaultInstance().withClassLoader(classLoader);
        if (delegate == null) {
            delegate = new SimpleResolver();
        }
        delegate.setClassLoader(classLoader);
        delegate.setTypeFactory(typeFactory);
        delegate.init(typeFactory.constructType(Action.class));
        return this;
    }

    /**
     * Gets a singleton instance.
     * @return the instance
     */
    public static ActionResolver getInstance() {
        if (instance == null) {
            instance = new ActionResolver();
        }
        return instance;
    }

    /**
     * Resolve a field action ID from the field action model class.
     * @param aClass field action model class
     * @return field action ID
     */
    public String toId(Class<?> aClass) {
        return delegate.idFromValueAndType(null, aClass);
    }

    /**
     * Resolve a field action model class from the field action ID.
     * @param id field action ID
     * @return field action model class
     * @throws IOException unexpected error
     */
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

    /**
     * Sets the {@link TypeFactory}.
     * @param tf {@link TypeFactory}
     */
    public void setTypeFactory(TypeFactory tf) {
        this.typeFactory = tf;
    }

}
