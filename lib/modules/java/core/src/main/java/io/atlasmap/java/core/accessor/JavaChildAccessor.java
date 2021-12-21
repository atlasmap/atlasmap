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

/**
 * The accessor for the Java child field.
 */
public abstract class JavaChildAccessor {

    private Object parent;
    private String name;
    private CollectionType collectionType;
    private List<Object> collectionValues;
    private Class<?> fieldClass;

    /**
     * A constructor.
     * @param parent parent
     * @param name name
     */
    public JavaChildAccessor(Object parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * Gets the raw field value.
     * @return value
     * @throws AtlasException unexpected error
     */
    public abstract Object getRawValue() throws AtlasException;

    /**
     * Gets the raw generic type of the field.
     * @return generic type
     * @throws AtlasException unexpected error
     */
    public abstract Type getRawGenericType() throws AtlasException;

    /**
     * Gets the raw class.
     * @return class
     * @throws AtlasException unexpected error
     */
    public abstract Class<?> getRawClass() throws AtlasException;

    /**
     * Gets the parent object.
     * @return parent
     */
    public Object getParentObject() {
        return this.parent;
    }

    /**
     * Gets the field name.
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the collection type.
     * @return collection type.
     * @throws AtlasException unexpected error
     */
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

    /**
     * Gets the collection values.
     * @return collection values
     * @throws AtlasException unexpected error
     */
    public List<?> getCollectionValues() throws AtlasException {
        getCollectionType();
        return this.collectionValues;
    }

    /**
     * Gets the field class.
     * @return class
     * @throws AtlasException unexpected error
     */
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

    /**
     * Gets the value.
     * @return value
     * @throws AtlasException unexpected error
     */
    public Object getValue() throws AtlasException {
        if (getCollectionType() == CollectionType.NONE) {
            return getRawValue();
        }
        return this.collectionValues.size() > 0 ? this.collectionValues.get(0) : null;
    }

    /**
     * Gets the value at the specified index in the collection if it's a collection.
     * Otherwise it returs the raw value.
     * @param pos index
     * @return value
     * @throws AtlasException unexpected error
     */
    public Object getValueAt(int pos) throws AtlasException {
        if (getCollectionType() == CollectionType.NONE) {
            return getRawValue();
        }
        return this.collectionValues.size() > pos ? this.collectionValues.get(pos) : null;
    }

}
