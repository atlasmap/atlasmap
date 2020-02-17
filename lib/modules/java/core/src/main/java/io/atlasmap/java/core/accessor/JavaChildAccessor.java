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

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import io.atlasmap.api.AtlasException;
import io.atlasmap.java.core.ClassHelper;
import io.atlasmap.v2.CollectionType;

public abstract class JavaChildAccessor {

    private Object parent;
    private String name;
    private CollectionType collectionType;
    private List<Object> collectionValues;
    private Class<?> fieldClass;


    public JavaChildAccessor(Object parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public abstract Object getRawValue() throws AtlasException;

    public abstract Type getRawGenericType() throws AtlasException;

    public abstract Class<?> getRawClass() throws AtlasException;

    public Object getParentObject() {
        return this.parent;
    }

    public String getName() {
        return this.name;
    }

    public CollectionType getCollectionType() throws AtlasException {
        if (this.collectionType != null) {
            return this.collectionType;
        }

        if (getRawClass().isArray()) {
            this.collectionType = CollectionType.ARRAY;
            this.collectionValues = new ArrayList<>();
            for (int i=0; getRawValue() != null && i<Array.getLength(getRawValue()); i++) {
                this.collectionValues.add(Array.get(getRawValue(), i));
            }
        } else if (getRawValue() instanceof Collection) {
            this.collectionType = CollectionType.LIST;
            if (getRawValue() instanceof List) {
                this.collectionValues = (List<Object>)this.getRawValue();
            } else {
                this.collectionValues = Arrays.asList(Collection.class.cast(getRawValue()).toArray());
            }
        } else if (getRawValue() instanceof Map) {
            // TODO java.util.Map support
            this.collectionType = CollectionType.MAP;
            this.collectionValues = Arrays.asList(Map.class.cast(getRawValue()).values().toArray());
        } else {
            this.collectionType = CollectionType.NONE;
        }
        return this.collectionType;
    }

    public List<?> getCollectionValues() throws AtlasException {
        getCollectionType();
        return this.collectionValues;
    }

    public Class<?> getFieldClass() throws AtlasException {
        if (this.fieldClass != null) {
            return this.fieldClass;
        }

        if (getCollectionType() == CollectionType.NONE) {
             this.fieldClass = getRawClass();
        } else if (getCollectionType() == CollectionType.ARRAY) {
            this.fieldClass = getRawClass().getComponentType();
        } else if (getCollectionType() == CollectionType.LIST) {
            return ClassHelper.detectClassFromTypeArgument(getRawGenericType());
        } else if (getCollectionType() == CollectionType.MAP) {
            // TODO java.util.Map support
            return ClassHelper.detectClassFromTypeArgumentAt(getRawGenericType(), 1);
        }
        return this.fieldClass;
    }

    public Object getValue() throws AtlasException {
        if (getCollectionType() == CollectionType.NONE) {
            return getRawValue();
        }
        return this.collectionValues.size() > 0 ? this.collectionValues.get(0) : null;
    }

    public Object getValueAt(int pos) throws AtlasException {
        if (getCollectionType() == CollectionType.NONE) {
            return getRawValue();
        }
        return this.collectionValues.size() > pos ? this.collectionValues.get(pos) : null;
    }

}