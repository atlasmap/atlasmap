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
package io.atlasmap.java.inspect;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;

public class JavaConstructService {

    private static final Logger LOG = LoggerFactory.getLogger(JavaConstructService.class);
    private AtlasConversionService atlasConversionService = null;

    public Object constructClass(JavaClass javaClass)
            throws ConstructException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return constructClass(javaClass, null);
    }

    public Object constructClass(JavaClass javaClass, List<String> pathFilters)
            throws ConstructException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        validateJavaClass(javaClass);

        if (getConversionService().isPrimitive(javaClass.getClassName())) {
            throw new ConstructPrimitiveException(
                    "Unable to instantiate a Java primitive: " + javaClass.getClassName());
        }

        if (javaClass.getCollectionType() != null) {
            switch (javaClass.getCollectionType()) {
            case ARRAY:
                return instantiateArray(javaClass, pathFilters);
            case LIST:
                return instantiateList(javaClass, pathFilters);
            case MAP:
                return instantiateMap(javaClass, pathFilters);
            default:
                throw new ConstructUnsupportedException(
                        String.format("Unsupported collectionType for instantiation c=%s cType=%s",
                                javaClass.getClassName(), javaClass.getCollectionType().value()));
            }
        }

        return constructClassIgnoreCollection(javaClass, pathFilters);
    }

    protected Object constructClassIgnoreCollection(JavaClass javaClass, List<String> pathFilters)
            throws ConstructException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Object targetObject = instantiateClass(javaClass.getClassName());
        filterFields(javaClass, pathFilters);

        if (javaClass.getJavaFields() == null || javaClass.getJavaFields().getJavaField() == null
                || javaClass.getJavaFields().getJavaField().isEmpty()) {
            return targetObject;
        }

        for (JavaField f : javaClass.getJavaFields().getJavaField()) {
            if (!(f instanceof JavaClass)) {
                continue;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Constructing complex child p=%s c=%s", f.getPath(), f.getClassName()));
            }

            Object parentObject = targetObject;

            /*
             * We aren't using the path for construction for now JavaPath javaPath = new
             * JavaPath(f.getPath()); if(javaPath.hasParent()) {
             *
             * }
             */

            Method setter = null;
            boolean doSetter = true;
            if (f.getGetMethod() != null) {
                Method getter = null;
                Object getterResult = null;
                try {
                    getter = ClassHelper.detectGetterMethod(parentObject.getClass(), f.getGetMethod());
                    getter.setAccessible(true);
                    getterResult = getter.invoke(parentObject);
                } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
                    LOG.warn(String.format("Error invoking getter for field p=%s c=%s msg=%s", f.getPath(),
                            f.getClassName(), e.getMessage()), e);
                    continue;
                }

                if (getterResult != null) {
                    doSetter = false;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Field instantiated by parent class p=%s c=%s", f.getPath(),
                                f.getClassName()));
                    }
                }
            }

            // instantiate class
            if (doSetter && f.getSetMethod() != null) {
                try {
                    setter = ClassHelper.detectSetterMethod(parentObject.getClass(), f.getSetMethod(), null);
                    setter.setAccessible(true);
                    setter.invoke(parentObject, constructClass((JavaClass) f, pathFilters));
                } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
                    LOG.warn(String.format("Error invoking setter for field p=%s c=%s msg=%s", f.getPath(),
                            f.getClassName(), e.getMessage()), e);
                    continue;
                }
            }
        }
        return targetObject;
    }

    protected Object instantiateClass(String className)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> c = Class.forName(className);
        return c.newInstance();
    }

    protected Object instantiateArray(JavaClass javaClass, List<String> pathFilters)
            throws ConstructException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Constructing array c=%s size=%s", javaClass.getClassName(),
                    javaClass.getArraySize()));
        }
        Object objectArray = Array.newInstance(Class.forName(javaClass.getClassName()), javaClass.getArraySize());
        for (int i = 0; i < javaClass.getArraySize(); i++) {
            ((Object[]) objectArray)[i] = constructClassIgnoreCollection(javaClass, pathFilters);
        }
        return objectArray;
    }

    protected Object instantiateList(JavaClass javaClass, List<String> pathFilters)
            throws ConstructException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Constructing list c=%s", javaClass.getCollectionClassName()));
        }
        Class<?> collectionClass = Class.forName(javaClass.getCollectionClassName());
        return collectionClass.newInstance();
    }

    protected Object instantiateMap(JavaClass javaClass, List<String> pathFilters)
            throws ConstructException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Constructing map c=%s", javaClass.getCollectionClassName()));
        }
        Class<?> collectionClass = Class.forName(javaClass.getCollectionClassName());
        return collectionClass.newInstance();
    }

    protected void validateJavaClass(JavaClass javaClass) throws ConstructException {
        if (javaClass == null) {
            throw new ConstructInvalidException("JavaClass cannot be null");
        }

        if (javaClass.getClassName() == null || javaClass.getClassName().trim().length() < 1) {
            throw new ConstructInvalidException("JavaClass.className must be specified");
        }

        // TODO: Fix the modifiers problem
        /*
         * if(javaClass.getModifiers() != null && javaClass.getModifiers().getModifier()
         * != null && javaClass.getModifiers().getModifier().size() > 0) { for(Modifier
         * modifier : javaClass.getModifiers().getModifier()) {
         * if(Arrays.asList(Modifier.ABSTRACT).contains(modifier) &&
         * !javaClass.getName().equals(javaClass.getClassName())) { throw new
         * ConstructInvalidException(String.
         * format("Unable to instantate abstract class c=%s",
         * javaClass.getClassName())); } } }
         */
    }

    public AtlasConversionService getConversionService() {
        return atlasConversionService;
    }

    public void setConversionService(AtlasConversionService atlasConversionService) {
        this.atlasConversionService = atlasConversionService;
    }

    public static void filterFields(JavaClass javaClass, List<String> filteredPaths) {
        if (filteredPaths == null || filteredPaths.size() == 0) {
            return;
        }

        if (javaClass == null || javaClass.getJavaFields() == null || javaClass.getJavaFields().getJavaField() == null
                || javaClass.getJavaFields().getJavaField().isEmpty()) {
            return;
        }

        List<JavaField> remove = new ArrayList<JavaField>();
        for (JavaField jf : javaClass.getJavaFields().getJavaField()) {
            if (!filteredPaths.contains(jf.getPath())) {
                remove.add(jf);
            } else if (jf instanceof JavaClass) {
                filterFields((JavaClass) jf, filteredPaths);
            }
        }

        for (JavaField jf : remove) {
            javaClass.getJavaFields().getJavaField().remove(jf);
        }
    }
}
