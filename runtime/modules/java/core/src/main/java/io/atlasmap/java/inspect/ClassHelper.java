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
package io.atlasmap.java.inspect;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;

public class ClassHelper {

    private static final char PKG_SEPARATOR = '.';

    private static final char DIR_SEPARATOR = '/';

    private static final String CLASS_FILE_SUFFIX = ".class";

    private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

    public static List<String> getterMethodNames(String fieldName) {
        List<String> opts = new ArrayList<String>();
        opts.add(getMethodNameFromFieldName(fieldName));
        opts.add(isMethodNameFromFieldName(fieldName));
        return opts;
    }

    private static String getMethodNameFromFieldName(String fieldName) {
        return "get" + StringUtil.capitalizeFirstLetter(fieldName);
    }

    private static String isMethodNameFromFieldName(String fieldName) {
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

    public static Object parentObjectForPath(Object targetObject, AtlasPath pathUtil, boolean skipCollectionWrapper)
            throws AtlasException {
        try {
            if (targetObject == null) {
                return null;
            }

            if (pathUtil == null) {
                return targetObject;
            }

            if (!pathUtil.hasParent() && !pathUtil.hasCollection()) {
                return targetObject;
            }

            Object parentObject = targetObject;
            AtlasPath parentPath = pathUtil.getLastSegmentParentPath();

            if (parentPath == null) {
                parentPath = pathUtil;
            }

            for (String segment : parentPath.getSegments()) {
                List<String> getters = getterMethodNames(AtlasPath.cleanPathSegment(segment));
                Method getterMethod = null;
                for (String getter : getters) {
                    try {
                        getterMethod = detectGetterMethod(parentObject.getClass(), getter);
                        break;
                    } catch (NoSuchMethodException e) {
                        // exhaust options
                    }
                }

                if (getterMethod == null) {
                    throw new NoSuchMethodException("Unable to detect getter method for " + segment);
                }

                getterMethod.setAccessible(true);
                parentObject = getterMethod.invoke(parentObject);

                if (skipCollectionWrapper) {
                    if (AtlasPath.isListSegment(segment) && pathUtil.isIndexedCollection()) {
                        int index = AtlasPath.indexOfSegment(segment);
                        parentObject = ((List<?>) parentObject).get(index);
                    } else if (AtlasPath.isArraySegment(segment)) {
                        int index = AtlasPath.indexOfSegment(segment);
                        parentObject = Array.get(parentObject, index);
                    }
                }
            }

            return parentObject;
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    static List<Class<?>> find(String scannedPackage) {
        String scannedPath = scannedPackage.replace(PKG_SEPARATOR, DIR_SEPARATOR);
        URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
        if (scannedUrl == null) {
            throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage));
        }
        File scannedDir = new File(scannedUrl.getFile());
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (File file : scannedDir.listFiles()) {
            classes.addAll(find(file, scannedPackage));
        }
        return classes;
    }

    private static List<Class<?>> find(File file, String scannedPackage) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        String resource = scannedPackage + PKG_SEPARATOR + file.getName();
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                classes.addAll(find(child, resource));
            }
        } else if (resource.endsWith(CLASS_FILE_SUFFIX)) {
            int endIndex = resource.length() - CLASS_FILE_SUFFIX.length();
            String className = resource.substring(0, endIndex);
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException ignore) {
            }
        }
        return classes;
    }
}
