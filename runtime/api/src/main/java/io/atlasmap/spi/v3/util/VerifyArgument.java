/**
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.atlasmap.spi.v3.util;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
public interface VerifyArgument {

    public static void isNotNull(String name, Object argument) {
        if (argument == null) {
            throw new AtlasRuntimeException("The '%s' argument must not be null", name);
        }
    }

    public static void isNotEmpty(String name, Object argument) {
        isNotNull(name, argument);
        if ((argument instanceof Collection && ((Collection<?>)argument).isEmpty())
            || argument.toString().isEmpty()
            || (argument instanceof Map && ((Map<?, ?>)argument).isEmpty())
            || (argument.getClass().isArray() && ((Object[])argument).length == 0)) {
            throw new AtlasRuntimeException("The '%s' argument must not be empty", name);
        }
    }

    /**
     * @param name
     * @param argument
     * @param lowerBound
     * @param upperBound
     * @throws AtlasRuntimeException if argument is not between the supplied lowerBound and upperBound inclusively
     */
    public static void isBetween(String name, int argument, int lowerBound, int upperBound) {
        if (argument < lowerBound || argument > upperBound) {
            throw new AtlasRuntimeException("The '%s' argument must be between %d and %d", name, lowerBound, upperBound);
        }
    }

    /**
     * @param name
     * @param argument
     * @param type
     * @return The supplied argument cast to the supplied type
     * @throws AtlasRuntimeException if argument is not an instance of the supplied type
     */
    @SuppressWarnings("unchecked")
    public static <T> T isInstanceOf(String name, Object argument, Class<T> type) {
        if (!type.isInstance(argument)) {
            throw new AtlasRuntimeException("The '%s' argument must be an instance of %s", name, type);
        }
        return (T)argument;
    }
}
