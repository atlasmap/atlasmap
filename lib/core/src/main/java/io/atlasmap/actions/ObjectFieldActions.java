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

@SuppressWarnings({"squid:S3776",     // Cognitive complexity of method
    "squid:S1118",     // Add private constructor
    "squid:S1226",     // Introduce new variable
    "squid:S3358" })   // Extract nested ternary
public class ObjectFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor
    public static Integer count(Count action, List<Object> inputs) {
        if (inputs == null) {
            return 0;
        }
        return inputs.size();
    }

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

    @AtlasActionProcessor
    public static Boolean isNull(IsNull action, Object input) {
        return input == null;
    }

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
