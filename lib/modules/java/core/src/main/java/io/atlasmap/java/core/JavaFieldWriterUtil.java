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
package io.atlasmap.java.core;

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesSupport;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.v2.CollectionType;

public class JavaFieldWriterUtil {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(JavaFieldWriterUtil.class);
    private AtlasConversionService conversionService = null;
    private ClassLoader classLoader;
    private Map<Class<?>, Class<?>> defaultCollectionImplClasses = new HashMap<>();

    public JavaFieldWriterUtil(AtlasConversionService conversionService) {
        this.conversionService = conversionService;
        // TODO support hierarchical class loader
        this.classLoader = Thread.currentThread().getContextClassLoader();
        defaultCollectionImplClasses.put(BeanContext.class, BeanContextServicesSupport.class);
        defaultCollectionImplClasses.put(BeanContextServices.class, BeanContextServicesSupport.class);
        defaultCollectionImplClasses.put(BlockingDeque.class, LinkedBlockingDeque.class);
        defaultCollectionImplClasses.put(BlockingQueue.class, LinkedBlockingQueue.class);
        defaultCollectionImplClasses.put(Collection.class, LinkedList.class);
        defaultCollectionImplClasses.put(ConcurrentMap.class, ConcurrentHashMap.class);
        defaultCollectionImplClasses.put(ConcurrentNavigableMap.class, ConcurrentSkipListMap.class);
        defaultCollectionImplClasses.put(Deque.class, ArrayDeque.class);
        defaultCollectionImplClasses.put(List.class, LinkedList.class);
        defaultCollectionImplClasses.put(Map.class, HashMap.class);
        defaultCollectionImplClasses.put(NavigableSet.class, TreeSet.class);
        defaultCollectionImplClasses.put(NavigableMap.class, TreeMap.class);
        defaultCollectionImplClasses.put(Queue.class, LinkedList.class);
        defaultCollectionImplClasses.put(Set.class, HashSet.class);
        defaultCollectionImplClasses.put(SortedSet.class, TreeSet.class);
        defaultCollectionImplClasses.put(SortedMap.class, TreeMap.class);
        defaultCollectionImplClasses.put(TransferQueue.class, LinkedTransferQueue.class);
    }

    public Object instantiateObject(Class<?> clz) throws AtlasException {
        if (clz == null) {
            throw new AtlasException("Cannot instantiate null class");
        }
        // TODO https://github.com/atlasmap/atlasmap/issues/48
        // - Allow default implementation for abstract target field
        // TODO support hierarchical class loader
        Class<?> clazz = clz;
        if (clazz.isArray()) {
            return Array.newInstance(clazz.getComponentType(), 0);
        }

        if (this.defaultCollectionImplClasses.get(clazz) != null) {
            clazz = this.defaultCollectionImplClasses.get(clazz);
        }
        try {
            Constructor<?> constructor = null;
            if (clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers())) {
                // Nested class requires an instance of enclosing class to instantiate
                Object enclosing = clazz.getEnclosingClass()
                                        .getDeclaredConstructor(new Class[0])
                                        .newInstance(new Object[0]);
                constructor = clazz.getDeclaredConstructor(new Class[] { enclosing.getClass() });
                constructor.setAccessible(true);
                return constructor.newInstance(new Object[] { enclosing });
            } else {
                constructor = clazz.getDeclaredConstructor(new Class[0]);
                constructor.setAccessible(true);
                return constructor.newInstance(new Object[0]);
            }
        } catch (Exception e) {
            throw new AtlasException("Could not instantiate class: " + clazz.getName(), e);
        }
    }

    public Class<?> loadClass(String name) throws AtlasException {
        try {
            return this.classLoader.loadClass(name);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    public Class<?> getDefaultCollectionImplClass(CollectionType type) {
        if (type == CollectionType.LIST) {
            return this.defaultCollectionImplClasses.get(List.class);
        } else if (type == CollectionType.MAP) {
            return this.defaultCollectionImplClasses.get(Map.class);
        }
        return null;
    }

    public Object getChildObject(Object parentObject, SegmentContext segment) throws AtlasException {
        String fieldName = segment.getName();
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Retrieving child '" + fieldName + "'.\n\tparentObject: " + parentObject);
        }

        if (parentObject == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cannot find child '" + fieldName + "', parent is null.");
            }
            return null;
        }

        Method getterMethod = resolveGetterMethod(parentObject.getClass(), fieldName);
        if (getterMethod == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(
                        "Unable to detect getter method for: %s on parent: %s",
                        fieldName, parentObject));
            }
            return null;
        }

        getterMethod.setAccessible(true);
        Object childObject;
        try {
            childObject = getterMethod.invoke(parentObject);
        } catch (Exception e) {
            throw new AtlasException(e);
        }

        if (LOG.isDebugEnabled()) {
            if (childObject == null) {
                LOG.debug("Could not find child object for path: " + fieldName);
            } else {
                LOG.debug("Found child object for path '" + fieldName + "': " + childObject);
            }
        }

        return childObject;
    }

    public Object createComplexChildObject(Object parentObject, SegmentContext segmentContext, Class<?> clazz) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating object for segment:'{} \n\tparentObject: {} \n\tclass: {}",
            segmentContext, parentObject, clazz.getName());
        }

        try {
            Method setterMethod = resolveSetterMethod(parentObject, segmentContext, null);
            Object targetObject = instantiateObject(clazz);
            setterMethod.setAccessible(true);
            setterMethod.invoke(parentObject, targetObject);
            return targetObject;
        } catch (Exception e) {
            try {
                java.lang.reflect.Field field = resolveField(parentObject.getClass(), segmentContext.getName());
                field.setAccessible(true);
                Object targetObject = instantiateObject(clazz);
                field.set(parentObject, targetObject);
                return targetObject;
            } catch (Exception e2) {
                String parentClassName = parentObject == null ? null : parentObject.getClass().getName();
                throw new AtlasException("Unable to create value for segment: " + segmentContext.getExpression() + " parentObject: "
                        + parentClassName, e2);
            }
        }
    }

    public Object createComplexChildObject(Object parentObject, SegmentContext segmentContext) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating object for segment:'{} \n\tparentObject: {}", segmentContext, parentObject);
        }

        Class<?> clazz;
        try {
            Method setterMethod = resolveSetterMethod(parentObject, segmentContext, null);
            clazz = setterMethod.getParameterTypes()[0];
            Object targetObject = instantiateObject(clazz);
            setterMethod.setAccessible(true);
            setterMethod.invoke(parentObject, targetObject);
            return targetObject;
        } catch (Exception e) {
            try {
                java.lang.reflect.Field field = resolveField(parentObject.getClass(), segmentContext.getName());
                field.setAccessible(true);
                clazz = field.getType();
                Object targetObject = instantiateObject(clazz);
                field.set(parentObject, targetObject);
                return targetObject;
            } catch (Exception e2) {
                String parentClassName = parentObject == null ? null : parentObject.getClass().getName();
                throw new AtlasException("Unable to create value for segment: " + segmentContext.getExpression() + " parentObject: "
                        + parentClassName, e2);
            }
        }
    }

    public void setChildObject(Object parentObject, Object childObject, SegmentContext segmentContext) throws AtlasException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting object for segment:'" + segmentContext.getExpression() + "'.\n\tchildObject: " + childObject
                    + "\n\tparentObject: " + parentObject);
        }

        try {
            Class<?> childClass = childObject == null ? null : childObject.getClass();
            Object targetObject = parentObject;
            try {
                Method setterMethod = resolveSetterMethod(parentObject, segmentContext, childClass);
                Class<?> targetClass = setterMethod.getParameterTypes()[0];

                if (childObject != null) {
                    childObject = conversionService.convertType(childObject, null, targetClass, null);
                }

                // We already know we have a 1 paramter setter here
                if (childObject == null && conversionService.isPrimitive(targetClass)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Not setting null value for primitive method paramter for segment:'" + segmentContext.getExpression()
                                + "'.\n\tchildObject: " + childObject + "\n\tparentObject: " + parentObject);
                    }
                    return;
                }
                setterMethod.setAccessible(true);
                setterMethod.invoke(targetObject, childObject);
            } catch (Exception e) {
                java.lang.reflect.Field field = resolveField(targetObject.getClass(), segmentContext.getName());
                if (field == null) {
                    String parentClassName = parentObject == null ? null : parentObject.getClass().getName();
                    String childClassName = childObject == null ? null : childObject.getClass().getName();
                    throw new AtlasException(String.format(
                        "Unable to set value for segment: %s parentObject: %s childObject: %s",
                    segmentContext.getExpression(), parentClassName, childClassName), e);
                }
                if (childObject != null) {
                    childObject = conversionService.convertType(childObject, null, field.getType(), null);
                }
                if (childObject == null && conversionService.isPrimitive(field.getType())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Not setting null value for primitive method paramter for segment:'" + segmentContext.getExpression()
                                + "'.\n\tchildObject: " + childObject + "\n\tparentObject: " + parentObject);
                    }
                    return;
                }
                field.setAccessible(true);
                field.set(targetObject, childObject);
            }
        } catch (Exception e) {
            String parentClassName = parentObject == null ? null : parentObject.getClass().getName();
            String childClassName = childObject == null ? null : childObject.getClass().getName();
            throw new AtlasException(String.format(
                "Unable to set value for segment: %s parentObject: %s childObject: %s",
                segmentContext, parentClassName, childClassName), e);
        }
    }

    public Class<?> resolveChildClass(Object parentObject, SegmentContext segment) throws AtlasException {
        try {
            Method setterMethod = resolveSetterMethod(parentObject, segment, null);
            return setterMethod.getParameterTypes()[0];
        } catch (NoSuchMethodException e) {
            try {
                java.lang.reflect.Field field = resolveField(parentObject.getClass(), segment.getName());
                field.setAccessible(true);
                return field.getType();
            } catch (Exception e2) {
                String parentClassName = parentObject == null ? null : parentObject.getClass().getName();
                throw new AtlasException("Unable to create value for segment: " + segment + " parentObject: "
                        + parentClassName, e2);
            }
        }
    }

    public Object getCollectionItem(Object collectionObject, SegmentContext segmentContext) throws AtlasException {
        int index = segmentContext.getCollectionIndex();
        if (segmentContext.getCollectionType() == CollectionType.ARRAY) {
            return Array.getLength(collectionObject) > index ? Array.get(collectionObject, index) : null;
        } else if (segmentContext.getCollectionType() == CollectionType.LIST) {
            if (collectionObject instanceof List) {
                List<?> list = (List<?>)collectionObject;
                return list.size() > index ? list.get(index) : null;
            } else {
                LOG.warn("Converting non-List Collection into array - order might not be preserved: segment={}",
                    segmentContext.getExpression());
                Object[] array = ((Collection<?>)collectionObject).toArray();
                return array.length > index ? array[index] : null;
            }
        } else if (segmentContext.getCollectionType() == CollectionType.MAP) {
            throw new AtlasException("TODO: java.util.Map is not yet supported, segment: " + segmentContext.getExpression());
        }
        throw new AtlasException("Cannot determine collection type from segment: " + segmentContext.getExpression());
    }

    public Object adjustCollectionSize(Object collectionObject, SegmentContext segmentContext) throws AtlasException {
        Object answer = collectionObject;
        Integer index = segmentContext.getCollectionIndex();
        if (segmentContext.getCollectionType() == CollectionType.MAP) {
            LOG.warn("It doesn't make sense to adjust the size of {}, Ignoring... {}", CollectionType.MAP, segmentContext);
            return answer;
        }
        if (index == null) {
            throw new AtlasException(String.format(
                    "No index was specified for adjusting collection size, segment=%s", segmentContext.getExpression()));
        }

        if (answer.getClass().isArray()) {
            if (Array.getLength(answer) < (index + 1)) {
                Object newArray = Array.newInstance(answer.getClass().getComponentType(), segmentContext.getCollectionIndex() + 1);
                // copy pre-existing items over to new array
                for (int i = 0; i < Array.getLength(answer); i++) {
                    Array.set(newArray, i, Array.get(answer, i));
                }
                answer = newArray;
            }
        } else if (answer instanceof List) {
            List<?> list = (List<?>) answer;
            while (list.size() < index + 1) {
                int size = list.size();
                list.add(null);
            }
        } else if (answer instanceof Collection) {
            LOG.warn("Collection object other than List doesn't support indexed operation. Ignoring... "
                    + "segment: {} \n\tparentObject: {}", segmentContext, collectionObject);
        }
        return answer;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object createComplexCollectionItem(Object collectionObject, Class<?> itemType, SegmentContext segmentContext) throws AtlasException {
        Integer index = segmentContext.getCollectionIndex();
        if (segmentContext.getCollectionType() != CollectionType.MAP && index == null) {
            throw new AtlasException(String.format(
                    "No index was specified for setting collection item, segment=%s", segmentContext.getExpression()));
        }

        if (collectionObject.getClass().isArray()) {
            if (index >= Array.getLength(collectionObject)) {
                throw new AtlasException("Cannot fit item in array, array size: " + Array.getLength(collectionObject)
                        + ", item index: " + index + ", segment: " + segmentContext);
            }
            Class<?> clazz = collectionObject.getClass().getComponentType();
            if (clazz.isPrimitive()) {
                return Array.get(collectionObject, index);
            }
            Object answer = instantiateObject(itemType != null ? itemType : clazz);
            Array.set(collectionObject, index, answer);
            return answer;
        } else if (collectionObject instanceof Collection) {
            Object newItem = null;
            newItem = instantiateObject(itemType);
            Collection collection = (Collection) collectionObject;
            if (collection instanceof List) {
                if (index > collection.size()) {
                    throw new AtlasException("Cannot fit item in list, list size: " + collection.size()
                            + ", item index: " + index + ", segment: " + segmentContext);
                }
                List list = (List) collection;
                list.set(index, newItem);
            } else if (index == collection.size()) {
                collection.add(newItem);
            } else {
                LOG.warn(String.format(
                        "Writing into non-List collection - it will be added as a last element anyway. "
                                + "segment: %s \n\tparentObject: %s\n\tchild: %s",
                        segmentContext, collectionObject, newItem));
                collection.add(newItem);
            }
            return newItem;
        } else if (collectionObject instanceof Map) {
            throw new AtlasException("TODO: Cannot yet handle adding children to maps");
        }
        throw new AtlasException("Cannot determine collection type for: " + collectionObject);
    }

    public Object createComplexCollectionItem(Object parentObject, Object collectionObject, SegmentContext segmentContext) throws AtlasException {
        Class<?> itemClazz = resolveCollectionItemClass(parentObject, segmentContext);
        return createComplexCollectionItem(collectionObject, itemClazz, segmentContext);
    }

    public Class<?> resolveCollectionItemClass(Object parentObject, SegmentContext segmentContext) throws AtlasException {
        Class<?> itemType = null;
        Method getterMethod = resolveGetterMethod(parentObject.getClass(), segmentContext.getName());
        try {
            Type genericType = null;
            if (getterMethod != null) {
                genericType = getterMethod.getGenericReturnType();
            } else {
                java.lang.reflect.Field field = resolveField(parentObject.getClass(), segmentContext.getName());
                if (field == null) {
                    throw new AtlasException(String.format(
                        "Failed to create a collection item, parent class={}, field name={}",
                        parentObject.getClass(), segmentContext.getName()));
                }
                genericType = field.getGenericType();
            }
            if (genericType instanceof Class) {
                if (((Class<?>)genericType).isArray()) {
                    itemType = ((Class<?>)genericType).getComponentType();
                } else {
                    itemType = Object.class;
                }
            } else if (genericType instanceof ParameterizedType
                    && ((ParameterizedType) genericType).getActualTypeArguments().length > 0) {
                String typeArg = ((ParameterizedType) genericType).getActualTypeArguments()[0].getTypeName();
                itemType = classLoader.loadClass(typeArg);
            } else {
                itemType = Object.class;
            }
        } catch (Throwable t) {
            throw new AtlasException(String.format(
                "Failed to resolve collection item class, parent class={}, field name={}",
                parentObject.getClass(), segmentContext.getName()), t);
        }
        return itemType;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setCollectionItem(Object collectionObject, Object item, SegmentContext segmentContext)
            throws AtlasException {
        Integer index = segmentContext.getCollectionIndex();
        if (segmentContext.getCollectionType() != CollectionType.MAP && index == null) {
            throw new AtlasException(String.format(
                "No index was specified for setting collection item, segment=%s", segmentContext.getExpression()));
        }

        if (collectionObject.getClass().isArray()) {
            if (index >= Array.getLength(collectionObject)) {
                throw new AtlasException("Cannot fit item in array, array size: " + Array.getLength(collectionObject)
                        + ", item index: " + index + ", segment: " + segmentContext);
            }
            try {
                Array.set(collectionObject, index, item);
            } catch (Exception e) {
                String parentClass = collectionObject == null ? null : collectionObject.getClass().getName();
                String childClass = item == null ? null : item.getClass().getName();
                throw new AtlasException("Could not set child class '" + childClass + "' on parent '" + parentClass
                        + "' for: " + segmentContext, e);
            }
            return;
        } else if (collectionObject instanceof Collection) {
            Collection collection = (Collection) collectionObject;
            if (collection instanceof List) {
                if (index > collection.size()) {
                    throw new AtlasException("Cannot fit item in list, list size: " + collection.size()
                            + ", item index: " + index + ", segment: " + segmentContext);
                }
                List list = (List) collection;
                list.set(index, item);
            } else if (index == collection.size()) {
                collection.add(item);
            } else {
                LOG.warn(String.format(
                        "Writing into non-List collection - it will be added as a last element anyway. "
                                + "segment: %s \n\tparentObject: %s\n\tchild: %s",
                        segmentContext, collectionObject, item));
                collection.add(item);
            }
            return;
        } else if (collectionObject instanceof Map) {
            throw new AtlasException("TODO: Cannot yet handle adding children to maps");
        }
        throw new AtlasException("Cannot determine collection type for: " + collectionObject);
    }

    public Map<Class<?>, Class<?>> getDefaultCollectionImplClasses() {
        return this.defaultCollectionImplClasses;
    }

    private Method resolveGetterMethod(Class<?> clz, String fieldName) {
        List<String> getters = ClassHelper.getterMethodNames(fieldName);
        List<Class<?>> classTree = resolveMappableClasses(clz);
        Method getterMethod = null;
        for (Class<?> clazz : classTree) {
            for (String getter : getters) {
                try {
                    return ClassHelper.detectGetterMethod(clazz, getter);
                } catch (NoSuchMethodException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Looking for getter for '{}' on this class: {}",
                            fieldName, clazz.getName(), e);
                    }
                }
            }
        }
        return getterMethod;
    }

    private Method resolveSetterMethod(Object sourceObject, SegmentContext segmentContext, Class<?> targetType)
            throws NoSuchMethodException {
        String setterMethodName = "set" + capitalizeFirstLetter(segmentContext.getName());
        List<Class<?>> classTree = resolveMappableClasses(sourceObject.getClass());

        Method m = null;
        for (Class<?> clazz : classTree) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Looking for setter '" + setterMethodName + "' on this class: " + clazz.getName());
            }
            try {
                m = ClassHelper.detectSetterMethod(clazz, setterMethodName, targetType);
                if (m != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found setter '" + setterMethodName + "' on this class: " + clazz.getName());
                    }
                    return m;
                }
            } catch (NoSuchMethodException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Did not find setter '" + setterMethodName + "' on this class: " + clazz.getName(), e);
                }
            }

            // Try the boxUnboxed version
            if (conversionService.isPrimitive(targetType) || conversionService.isBoxedPrimitive(targetType)) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Looking for boxed setter '" + setterMethodName + "' on this class: "
                                + clazz.getName());
                    }
                    m = ClassHelper.detectSetterMethod(clazz, setterMethodName,
                            conversionService.boxOrUnboxPrimitive(targetType));
                    if (m != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Found setter '" + setterMethodName + "' on this class: " + clazz.getName());
                        }
                        return m;
                    }
                } catch (NoSuchMethodException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Did not find setter '" + setterMethodName + "' on this class: " + clazz.getName(),
                                e);
                    }
                }
            }
        }

        throw new NoSuchMethodException("Unable to resolve expected setter '" + setterMethodName + "' for segment: "
                + segmentContext.getExpression() + ", on object: " + sourceObject);
    }

    private java.lang.reflect.Field resolveField(Class<?> clz, String name) {
        List<Class<?>> classTree = resolveMappableClasses(clz);
        for (Class<?> clazz : classTree) {
            try {
                return clazz.getDeclaredField(name);
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    private static String capitalizeFirstLetter(String string) {
        if (StringUtil.isEmpty(string)) {
            return string;
        }
        if (string.length() == 1) {
            return String.valueOf(string.charAt(0)).toUpperCase();
        }
        return String.valueOf(string.charAt(0)).toUpperCase() + string.substring(1);
    }

    private List<Class<?>> resolveMappableClasses(Class<?> clazz) {
        List<Class<?>> classTree = new ArrayList<>();
        classTree.add(clazz);
        Class<?> superClazz = clazz.getSuperclass();
        while (superClazz != null) {
            if (JdkPackages.contains(superClazz.getPackage().getName())) {
                superClazz = null;
            } else {
                classTree.add(superClazz);
                superClazz = superClazz.getSuperclass();
            }
        }

        // DON'T reverse.. prefer child -> parent -> grandparent
        // List<Class<?>> reverseTree = classTree.subList(0, classTree.size());
        // Collections.reverse(reverseTree);
        // return reverseTree;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found " + classTree.size() + " mappable classes for class '"
                    + clazz.getName() + "': " + classTree);
        }

        return classTree;
    }

}
