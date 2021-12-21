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
package io.atlasmap.actions;

import java.util.Collection;
import java.util.List;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.v2.Contains;
import io.atlasmap.v2.Count;
import io.atlasmap.v2.Equals;
import io.atlasmap.v2.IsNull;
import io.atlasmap.v2.ItemAt;
import io.atlasmap.v2.Length;

/**
 * The object field actions.
 */
public class ObjectFieldActions implements AtlasFieldAction {

    /**
     * Counts the number of source objects.
     * @param action action model
     * @param inputs a list of source objects
     * @return count
     */
    @AtlasActionProcessor
    public static Integer count(Count action, List<Object> inputs) {
        if (inputs == null) {
            return 0;
        }
        return inputs.size();
    }

    /**
     * Gets if the source objects contain the object specified as a parameter.
     * @param contains action model
     * @param inputs a list of source objects
     * @return true if it contains, or false
     */
    @AtlasActionProcessor
    public static Boolean contains(Contains contains, List<Object> inputs) {
        if (contains == null) {
            throw new IllegalArgumentException("Contains action must be specified");
        }
        if (inputs == null) {
            return contains.getValue() == null;
        }
        return collectionContains(inputs, contains);
    }

    /**
     * Gets if the source object is equal to the object specified as a parameter.
     * @param equals action model
     * @param input source
     * @return true if it's equal, or false
     */
    @AtlasActionProcessor
    public static Boolean equals(Equals equals, Object input) {
        if (equals == null) {
            throw new IllegalArgumentException("Equals action must be specified");
        }
        if (input == null) {
            return equals.getValue() == null;
        }

        return input.toString().equals(equals.getValue());
    }

    /**
     * Gets if the source object is null or not.
     * @param action action model
     * @param input source
     * @return true if it's null, or false
     */
    @AtlasActionProcessor
    public static Boolean isNull(IsNull action, Object input) {
        return input == null;
    }

    /**
     * Gets an item from the list of the source objects by specifying an index.
     * @param itemAt action model
     * @param inputs a list of source object
     * @return item
     */
    @AtlasActionProcessor
    public static Object itemAt(ItemAt itemAt, List<Object> inputs) {
        if (inputs == null) {
            return null;
        }

        Integer index = itemAt.getIndex() == null ? 0 : itemAt.getIndex();
        Object[] array = inputs.toArray(new Object[0]);
        if (array.length > index) {
            return array[index];
        } else {
            throw new ArrayIndexOutOfBoundsException(String.format(
                    "Collection '%s' has fewer (%s) than expected (%s)", array, array.length, index));
        }
    }

    /**
     * Gets the length of the string representation of the source object.
     * @param length action model
     * @param input source
     * @return length
     */
    @AtlasActionProcessor
    public static Integer length(Length length, Object input) {
        if (input == null) {
            return -1;
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
