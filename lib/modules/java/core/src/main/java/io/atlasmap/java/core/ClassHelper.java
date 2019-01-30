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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
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

    public static List<Object> getParentObjectsForPath(AtlasInternalSession session, Object targetObject, AtlasPath pathUtil)
            throws AtlasException {
        try {
            if (targetObject == null) {
                return null;
            }

            if (pathUtil == null) {
                return Arrays.asList(targetObject);
            }

            if (pathUtil.isRoot() && !pathUtil.hasCollectionRoot()) {
                return Arrays.asList(targetObject);
            }

            AtlasPath parentPath = pathUtil.getLastSegmentParentPath();
            if (parentPath == null) {
                parentPath = pathUtil;
            }

            List<Object> parents = new LinkedList<>();
            parents.add(targetObject);
            if (pathUtil.hasCollectionRoot()) {
                // handling topmost collection
                SegmentContext rootSegment = pathUtil.getRootSegment();
                List<Object> children = new LinkedList<>();
                Integer index = rootSegment.getCollectionIndex();
                if (index == null) {
                    if (targetObject instanceof Collection) {
                        children.addAll((Collection<?>)targetObject);
                    } else if (targetObject.getClass().isArray()) {
                        children = Arrays.asList((Object[])targetObject);
                    } else {
                        children.add(targetObject);
                    }
                } else if (rootSegment.getCollectionType() == CollectionType.LIST) {
                    children.add(((Collection<?>) targetObject).toArray(new Object[0])[index]);
                } else if (rootSegment.getCollectionType() == CollectionType.ARRAY) {
                    children.add(Array.get(targetObject, index));
                }
                parents = children;
            }

            for (SegmentContext segment : parentPath.getSegments(false)) {
                List<String> getters = getterMethodNames(segment.getName());
                Method getterMethod = null;
                for (String getter : getters) {
                    try {
                        getterMethod = detectGetterMethod(parents.get(0).getClass(), getter);
                        break;
                    } catch (NoSuchMethodException e) {
                        // exhaust options
                    }
                }

                if (getterMethod == null) {
                    throw new NoSuchMethodException("Unable to detect getter method for " + segment);
                }

                getterMethod.setAccessible(true);
                List<Object> children = new LinkedList<>();
                for (Object parentObject : parents) {
                    if (parentObject == null) {
                        children.add(null);
                        AtlasUtil.addAudit(session, null, String.format(
                                "Assigning null value for path=%s due to null parent", pathUtil),
                                pathUtil.toString(), AuditStatus.WARN, null);
                        continue;
                    }
                    Object child = getterMethod.invoke(parentObject);
                    if (segment.getCollectionType() == CollectionType.NONE) {
                        children.add(child);
                    } else {
                        Integer index = segment.getCollectionIndex();
                        if (index == null) {
                            if (child instanceof Collection) {
                                children.addAll((Collection<?>)child);
                            } else if (child.getClass().isArray()) {
                                children = Arrays.asList(child);
                            } else {
                                children.add(child);
                            }
                        } else if (segment.getCollectionType() == CollectionType.ARRAY) {
                            children.add(((Collection<?>) parentObject).toArray(new Object[0])[index]);
                        } else if (segment.getCollectionType() == CollectionType.LIST) {
                            children.add(Array.get(parentObject, index));
                        }
                    }
                }
                parents = children;
            }

            return parents;
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

}
