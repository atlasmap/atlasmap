/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.atlasmap.java.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.java.core.accessor.FieldAccessor;
import io.atlasmap.java.core.accessor.GetterAccessor;
import io.atlasmap.java.core.accessor.JavaChildAccessor;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;

public class ClassHelper {

    public static List<String> getterMethodNames(String fieldName) {
        List<String> opts = new ArrayList<String>();
        opts.add(getMethodNameFromFieldName(fieldName));
        opts.add(isMethodNameFromFieldName(fieldName));
        return opts;
    }

    public static String getMethodNameFromFieldName(String fieldName) {
        return "get" + StringUtil.capitalizeFirstLetter(fieldName);
    }

    public static String isMethodNameFromFieldName(String fieldName) {
        return "is" + StringUtil.capitalizeFirstLetter(fieldName);
    }

    public static Method detectGetterMethod(Class<?> clazz, String methodName) throws NoSuchMethodException {

        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                return method;
            }
        }

        throw new NoSuchMethodException(
                String.format("No matching getter method for class=%s method=%s", clazz.getName(), methodName));
    }

    public static Map<String, Method> detectAllGetterMethods(Class<?> clazz) throws Exception {
        Map<String, Method> answer = new HashMap<>();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0
             && method.getReturnType() != Void.class) {
                 answer.put(StringUtil.getFieldNameFromGetter(method.getName()), method);
            } else if (method.getName().startsWith("is") && method.getParameterTypes().length == 0
             && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                 answer.put(StringUtil.getFieldNameFromGetter(method.getName()), method);
            }
        }
        return answer;
    }

    public static List<Field> detectAllJavaFields(Class<?> clazz) {
        List<Field> answer = new ArrayList<>();
        Class<?> targetClazz = clazz;
        while (targetClazz != null && targetClazz != Object.class) {
            try {
                Field[] fields = targetClazz.getDeclaredFields();
                answer.addAll(Arrays.asList(fields));
            } catch (Exception e) {
                e.getMessage(); // ignore
                targetClazz = targetClazz.getSuperclass();
            }
        }
        return answer;
    }

    public static Method detectSetterMethod(Class<?> clazz, String methodName, Class<?> paramType)
            throws NoSuchMethodException {
        List<Method> candidates = new ArrayList<Method>();

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                candidates.add(method);
            }
        }

        String paramTypeClassName = paramType == null ? null : paramType.getName();

        if (candidates.size() == 0) {
            throw new NoSuchMethodException(
                    String.format("No matching setter found for class=%s method=%s paramType=%s", clazz.getName(),
                            methodName, paramTypeClassName));
        }

        if (paramType != null) {
            for (Method candidate : candidates) {
                // Getter and setter w/ same returnType & paramType
                if (candidate.getParameterTypes()[0].isAssignableFrom(paramType)) {
                    return candidate;
                }
            }
            throw new NoSuchMethodException(
                    String.format("No matching setter found for class=%s method=%s paramType=%s", clazz.getName(),
                            methodName, paramTypeClassName));
        }

        // paramType is null, let's do some more digging...

        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        Method getter = null;
        Class<?> returnType = null;
        for (String prefix : Arrays.asList("get", "is")) {
            try {
                getter = detectGetterMethod(clazz, methodName.replace("set", prefix));
                returnType = getter.getReturnType();
                break;
            } catch (NoSuchMethodException nsme) {
                // System.out.println("\t\t could not find getter " + methodName.replace("set",
                // prefix));
            }
        }

        // Solid match
        for (Method candidate : candidates) {
            // Getter and setter w/ same returnType & paramType
            Class<?> candidateReturnType = candidate.getParameterTypes()[0];
            if (returnType == null) {
                if (candidateReturnType == null) {
                    return candidate;
                }
            } else if (returnType.isAssignableFrom(candidateReturnType)) {
                return candidate;
            }
        }

        // Not as good of a match .. find one with a matching converter
        /*
         * for (Method candidate : candidates) {
         * if(candidate.getParameterTypes()[0].equals(String.class)) { return candidate;
         * } }
         */

        // Yikes! User should specify type, or provide a converter
        throw new NoSuchMethodException(String.format("Unable to auto-detect setter class=%s method=%s paramType=%s",
                clazz.getName(), methodName, paramTypeClassName));
    }

    public static Class<?> detectClassFromTypeArgument(Type type) {
        return detectClassFromTypeArgumentAt(type, 0);
    }

    public static Class<?> detectClassFromTypeArgumentAt(Type type, int pos) {
        if (type == null || !(type instanceof ParameterizedType)) {
            return Object.class;
        }
        ParameterizedType genericType = (ParameterizedType) type;
        Type[] typeArgs = genericType.getActualTypeArguments();
        if (typeArgs == null || typeArgs.length <= pos) {
            return Object.class;
        }
        return typeArgs[pos] instanceof Class ? (Class<?>) typeArgs[pos] : Object.class;
    }

    public static Method lookupGetterMethod(Object object, String name) {
        List<String> getters = getterMethodNames(name);
        Method getterMethod = null;
        for (String getter : getters) {
            try {
                getterMethod = detectGetterMethod(object.getClass(), getter);
                break;
            } catch (NoSuchMethodException e) {
                // exhaust options
            }
        }

        if (getterMethod == null) {
            return null;
        } else {
            getterMethod.setAccessible(true);
            return getterMethod;
        }
    }

    public static Field lookupJavaField(Object source, String fieldName) {
        Class<?> targetClazz = source != null ? source.getClass() : null;
        while (targetClazz != null && targetClazz != Object.class) {
            try {
                Field field = targetClazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (Exception e) {
                e.getMessage(); // ignore
                targetClazz = targetClazz.getSuperclass();
            }
        }
        return null;
    }

    public static JavaChildAccessor lookupAccessor(Object source, String name) {
        if (source == null || AtlasUtil.isEmpty(name)) {
            return null;
        }
        Method m = lookupGetterMethod(source, name);
        if (m != null) {
            return new GetterAccessor(source, name, m);
        }
        Field f = lookupJavaField(source, name);
        if (f != null) {
            return new FieldAccessor(source, name, f);
        }
        return null;
    }

	public static List<JavaChildAccessor> lookupAllAccessors(Object source) throws Exception {
        List<JavaChildAccessor> answer = new ArrayList<>();
        if (source == null) {
            return answer;
        }
        Set<String> names = new HashSet<>();
        Map<String, Method> getters = detectAllGetterMethods(source.getClass());
        getters.forEach((k, v) -> {
            answer.add(new GetterAccessor(source, k, v));
            names.add(k);

        });
        List<Field> fields = detectAllJavaFields(source.getClass());
        fields.forEach(f -> {
            if (!names.contains(f.getName())) {
                answer.add(new FieldAccessor(source, f.getName(), f));
            }
        });
        return answer;
	}

}
