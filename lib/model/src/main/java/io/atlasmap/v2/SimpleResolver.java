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
import java.util.HashMap;
import java.util.ServiceLoader;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SimpleResolver extends TypeIdResolverBase {

    protected final HashMap<Class<?>, String> typeToId = new HashMap<Class<?>, String>();
    protected final HashMap<String, JavaType> idToType = new HashMap<String, JavaType>();

    private JavaType baseType;
    private ClassLoader classLoader;
    private TypeFactory typeFactory;

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
        Class<?> baseClass = baseType.getRawClass();

        ServiceLoader<?> implementations = ServiceLoader.load(baseClass, this.classLoader != null ? this.classLoader : baseClass.getClassLoader());
        for (Object o : implementations) {
            Class<?> c = o.getClass();
            if (c != baseClass && baseClass.isAssignableFrom(c)) {
                JsonTypeName jsonAnnoation = c.getAnnotation(JsonTypeName.class);

                // Use short ids for classes in the same package..
                String id = null;
                if ( c.getPackage().getName().equals(baseClass.getPackage().getName()) ) {
                    if (jsonAnnoation != null && jsonAnnoation.value() != null) {
                        id = jsonAnnoation.value();
                    } else {
                        id = c.getSimpleName();
                    }
                } else {
                    // All other extensions need fully qualified names.
                    id = c.getName();
                }
                typeToId.put(c, id);
                idToType.put(id, typeFactory.constructSpecializedType(baseType, c));
            }
        }
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> aClass) {
        String rc = typeToId.get(aClass);
        if (rc == null) {
            throw new IllegalArgumentException("Invalid type: " + aClass);
        }
        return rc;
    }

    @Override
    public String idFromBaseType() {
        return idFromValueAndType(null, baseType.getRawClass());
    }

    @Override
    public JavaType typeFromId(DatabindContext databindContext, String id) throws IOException {
        JavaType rc = idToType.get(id);
        if (rc == null) {
            throw new IllegalArgumentException("Invalid type id: " + id);
        }
        return rc;
    }

    @Override
    public String getDescForKnownTypeIds() {
        return "valid values: " + idToType.keySet();
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public void setTypeFactory(TypeFactory tf) {
        this.typeFactory = tf;
    }
}
