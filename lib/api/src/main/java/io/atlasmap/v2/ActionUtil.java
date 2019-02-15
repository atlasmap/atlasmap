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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import io.atlasmap.spi.AtlasFieldActionParameter;

public class ActionUtil {

    public static final String COLLECTION_MUST_CONTAIN_NUMBERS_ERR_MSG = "The source collection/arry/map must only contain numbers";
    public static final String STRING_SEPARATOR_REGEX = "[\\s+\\:\\_\\+\\=\\-]+";
    public static final Pattern STRING_SEPARATOR_PATTERN = Pattern.compile(STRING_SEPARATOR_REGEX);

    public static Collection<?> collection(Object input) {
        if (input instanceof Collection) {
            return (Collection<?>) input;
        }
        if (input instanceof Map) {
            return ((Map<?, ?>) input).values();
        }
        if (input instanceof Number[]) {
            return Arrays.asList((Object[]) input);
        }
        if (input instanceof double[]) {
            double[] array = (double[]) input;
            List<Double> list = new ArrayList<>(array.length);
            for (double e : array) {
                list.add(e);
            }
            return list;
        }
        if (input instanceof float[]) {
            float[] array = (float[]) input;
            List<Float> list = new ArrayList<>(array.length);
            for (float e : array) {
                list.add(e);
            }
            return list;
        }
        if (input instanceof long[]) {
            long[] array = (long[]) input;
            List<Long> list = new ArrayList<>(array.length);
            for (long e : array) {
                list.add(e);
            }
            return list;
        }
        if (input instanceof int[]) {
            int[] array = (int[]) input;
            List<Integer> list = new ArrayList<>(array.length);
            for (int e : array) {
                list.add(e);
            }
            return list;
        }
        if (input instanceof byte[]) {
            byte[] array = (byte[]) input;
            List<Byte> list = new ArrayList<>(array.length);
            for (byte e : array) {
                list.add(e);
            }
            return list;
        }
        if (input instanceof String[]) {
            String[] array = (String[]) input;
            List<String> list = new ArrayList<>(array.length);
            for (String e : array) {
                list.add(e);
            }
            return list;
        }
        if (input.getClass().isArray()) {
            Object[] array = (Object[]) input;
            List<Object> list = new ArrayList<>(array.length);
            for (Object e : array) {
                list.add(e);
            }
            return list;
        }
        throw new IllegalArgumentException(
                "Illegal input[" + input + "]. Input must be a Collection, Map or array of numbers");
    }

    public static List<Field> declaredFields(Class<?> actionClass) {
        List<Field> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(actionClass.getDeclaredFields()));
        Class<?> superclass = actionClass.getSuperclass();
        if (superclass.getPackage().equals(actionClass.getPackage())) {
            fields.addAll(declaredFields(superclass));
        }
        return fields;
    }

    public static Map<String, Field> mapActionParametersByName(Class<?> actionClass) {
        Map<String, Field> parameters = new HashMap<>();
        for (Field field : declaredFields(actionClass)) {
            if (field.getAnnotation(AtlasFieldActionParameter.class) != null) {
                field.setAccessible(true);
                parameters.put(field.getName(), field);
            }
        }
        return parameters;
    }

    public static boolean requiresDoubleResult(Object object) {
        return object instanceof Double || object instanceof Float;
    }

    private ActionUtil() {
    }
}
