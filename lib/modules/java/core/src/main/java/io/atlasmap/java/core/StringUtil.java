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
package io.atlasmap.java.core;

import io.atlasmap.core.AtlasUtil;

/**
 * The collection of utility methods for Java Document handling.
 */
public class StringUtil {

    /**
     * Capitalizes the first letter.
     * @param sentence String
     * @return capitalized
     */
    public static String capitalizeFirstLetter(String sentence) {
        if (AtlasUtil.isEmpty(sentence)) {
            return sentence;
        }
        if (sentence.length() == 1) {
            return String.valueOf(sentence.charAt(0)).toUpperCase();
        }
        return String.valueOf(sentence.charAt(0)).toUpperCase() + sentence.substring(1);
    }

    /**
     * Gets the field name from the getter method.
     * @param getter getter method name
     * @return field name
     */
    public static String getFieldNameFromGetter(String getter) {
        if (AtlasUtil.isEmpty(getter)) {
            return getter;
        }

        String subGetter;
        if (getter.startsWith("get")) {
            if (getter.length() <= "get".length()) {
                return getter;
            }
            subGetter = getter.substring("get".length());
        } else if (getter.startsWith("is")) {
            if (getter.length() <= "is".length()) {
                return getter;
            }
            subGetter = getter.substring("is".length());
        } else {
            return getter;
        }

        return String.valueOf(subGetter.charAt(0)).toLowerCase() + subGetter.substring(1);
    }

    /**
     * Gets the field name from the setter method.
     * @param setter setter method name
     * @return field name
     */
    public static String getFieldNameFromSetter(String setter) {
        if (AtlasUtil.isEmpty(setter)) {
            return setter;
        }

        String subSetter;
        if (setter.startsWith("set")) {
            if (setter.length() <= "set".length()) {
                return setter;
            }
            subSetter = setter.substring("set".length());
        } else {
            return setter;
        }

        return String.valueOf(subSetter.charAt(0)).toLowerCase() + subSetter.substring(1);
    }

}
