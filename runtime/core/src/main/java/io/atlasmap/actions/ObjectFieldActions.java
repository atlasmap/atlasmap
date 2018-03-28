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
package io.atlasmap.actions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Contains;
import io.atlasmap.v2.Equals;
import io.atlasmap.v2.FieldType;

@SuppressWarnings({"squid:S3776",     // Cognitive complexity of method
    "squid:S1118",     // Add private constructor
    "squid:S1226",     // Introduce new variable
    "squid:S3358" })   // Extract nested ternary
public class ObjectFieldActions implements AtlasFieldAction {

    @AtlasFieldActionInfo(name = "Contains", sourceType = FieldType.ANY, targetType = FieldType.BOOLEAN, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Boolean contains(Action action, Object input) {
        if (!(action instanceof Contains)) {
            throw new IllegalArgumentException("Action must be a Contains action");
        }

        Contains contains = (Contains) action;

        if (input == null) {
            return contains.getValue() == null;
        }

        if (input instanceof Collection) {
            return collectionContains((Collection<?>)input, contains);
        }
        if (input.getClass().isArray()) {
            return collectionContains(Arrays.asList((Object[])input), contains);
        }
        if (input instanceof Map<?, ?>) {
            if (collectionContains(((Map<?, ?>)input).values(), contains)) {
                return true;
            }
            return collectionContains(((Map<?, ?>)input).keySet(), contains);
        }
        if (contains.getValue() == null) {
            return false;
        }
        return input.toString().contains(contains.getValue());
    }

    @AtlasFieldActionInfo(name = "Equals", sourceType = FieldType.ANY, targetType = FieldType.BOOLEAN, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Boolean equals(Action action, Object input) {
        if (!(action instanceof Equals)) {
            throw new IllegalArgumentException("Action must be an Equals action");
        }

        Equals equals = (Equals) action;

        if (input == null) {
            return equals.getValue() == null;
        }

        if (input.getClass().isArray()) {
            return Arrays.asList((Object[])input).toString().equals(equals.getValue());
        }
        return input.toString().equals(equals.getValue());
    }

    @AtlasFieldActionInfo(name = "IsNull", sourceType = FieldType.ANY, targetType = FieldType.BOOLEAN, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Boolean isNull(Action action, Object input) {
        return input == null;
    }

    @AtlasFieldActionInfo(name = "Length", sourceType = FieldType.ANY, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.ALL, targetCollectionType = CollectionType.NONE)
    public static Integer length(Action action, Object input) {
        if (input == null) {
            return -1;
        }
        if (input instanceof Collection) {
            return ((Collection<?>)input).size();
        }
        if (input.getClass().isArray()) {
            return ((Object[])input).length;
        }
        if (input instanceof Map<?, ?>) {
            return ((Map<?, ?>)input).size();
        }
        return input.toString().length();
    }

    private static boolean collectionContains(Collection<?> collection, Contains contains) {
        for (Object item : collection) {
            if (item == null) {
                if (contains.getValue() == null) {
                    return true;
                }
            } else if (item.toString().equals(contains.getValue())) {
                return true;
            }
        }
        return false;
    }
}
