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
package io.atlasmap.java.inspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.java.core.ClassHelper;
import io.atlasmap.java.core.JdkPackages;
import io.atlasmap.java.core.StringUtil;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.java.v2.ModifierList;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.StringList;

public class ClassInspectionService {

    public static final int MAX_REENTRY_LIMIT = 1;
    public static final int MAX_ARRAY_DIM_LIMIT = 256; // JVM specification

    private static final Logger LOG = LoggerFactory.getLogger(ClassInspectionService.class);
    // limit

    private List<String> listClasses = new ArrayList<>(
            Arrays.asList("java.util.List", "java.util.ArrayList", "java.util.LinkedList", "java.util.Vector",
                    "java.util.Stack", "java.util.AbstractList", "java.util.AbstractSequentialList"));
    private List<String> mapClasses = new ArrayList<>(Arrays.asList("java.util.Map", "java.util.HashMap",
            "java.util.TreeMap", "java.util.Hashtable", "java.util.IdentityHashMap", "java.util.LinkedHashMap",
            "java.util.LinkedHashMap", "java.util.SortedMap", "java.util.WeakHashMap", "java.util.Properties",
            "java.util.concurrent.ConcurrentHashMap", "java.util.concurrent.ConcurrentMap"));
    private AtlasConversionService atlasConversionService = null;
    private List<String> fieldExclusions = new ArrayList<>(Arrays.asList("serialVersionUID"));
    private List<String> classNameExclusions = new ArrayList<>();
    private Boolean disablePackagePrivateOnlyFields = false;
    private Boolean disableProtectedOnlyFields = false;
    private Boolean disablePrivateOnlyFields = false;
    private Boolean disablePublicOnlyFields = false;
    private Boolean disablePublicGetterSetterFields = false;

    public List<String> getMapClasses() {
        return this.mapClasses;
    }

    public List<String> getListClasses() {
        return this.listClasses;
    }

    public List<String> getClassNameExclusions() {
        return this.classNameExclusions;
    }

    public List<String> getFieldExclusions() {
        return this.fieldExclusions;
    }

    public Boolean getDisableProtectedOnlyFields() {
        return disableProtectedOnlyFields;
    }

    public void setDisableProtectedOnlyFields(Boolean disableProtectedOnlyFields) {
        this.disableProtectedOnlyFields = disableProtectedOnlyFields;
    }

    public Boolean getDisablePackagePrivateOnlyFields() {
        return disablePackagePrivateOnlyFields;
    }

    public void setDisablePackagePrivateOnlyFields(Boolean disablePackagePrivateOnlyFields) {
        this.disablePackagePrivateOnlyFields = disablePackagePrivateOnlyFields;
    }

    public Boolean getDisablePrivateOnlyFields() {
        return disablePrivateOnlyFields;
    }

    public void setDisablePrivateOnlyFields(Boolean disablePrivateOnlyFields) {
        this.disablePrivateOnlyFields = disablePrivateOnlyFields;
    }

    public Boolean getDisablePublicOnlyFields() {
        return disablePublicOnlyFields;
    }

    public void setDisablePublicOnlyFields(Boolean disablePublicOnlyFields) {
        this.disablePublicOnlyFields = disablePublicOnlyFields;
    }

    public Boolean getDisablePublicGetterSetterFields() {
        return disablePublicGetterSetterFields;
    }

    public void setDisablePublicGetterSetterFields(Boolean disablePublicGetterSetterFields) {
        this.disablePublicGetterSetterFields = disablePublicGetterSetterFields;
    }

    public JavaClass inspectClass(String className, CollectionType collectionType, String collectionClassName) throws ClassNotFoundException {
        // Use a loader for this class for now
        ClassLoader classLoader = getClass().getClassLoader();
        return inspectClass(classLoader, className, collectionType, collectionClassName);
    }

    public JavaClass inspectClass(ClassLoader classLoader, String className, CollectionType collectionType, String collectionClassName)
        throws ClassNotFoundException {
        Class<?> clazz = classLoader.loadClass(className);
        return inspectClass(classLoader, clazz, collectionType, collectionClassName);
    }

    public JavaClass inspectClass(String className, CollectionType collectionType, String collectionClassName, String classpath) throws InspectionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inspecting class: " + className + ", classPath: " + classpath);
        }
        if (className == null || classpath == null) {
            throw new InspectionException("ClassName and Classpath must be specified");
        }

        JavaClass d = null;
        try {
            JarClassLoader jcl = new JarClassLoader(new String[] { "target/reference-jars" });
            Class<?> clazz = jcl.loadClass(className);
            d = inspectClass(jcl, clazz, collectionType, collectionClassName);
        } catch (ClassNotFoundException cnfe) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Class was not found: " + className);
            }
            d = AtlasJavaModelFactory.createJavaClass();
            d.setClassName(className);
            d.setStatus(FieldStatus.NOT_FOUND);
        }
        return d;
    }

    public JavaClass inspectClass(Class<?> clazz, CollectionType collectionType, String collectionClassName) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class must be specified");
        }

        return inspectClass(clazz.getClassLoader(), clazz, collectionType, collectionClassName);
    }

    public JavaClass inspectClass(ClassLoader classLoader, Class<?> clazz, CollectionType collectionType, String collectionClassName) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class must be specified");
        }

        JavaClass javaClass = AtlasJavaModelFactory.createJavaClass();
        javaClass.setCollectionType(collectionType);
        String rootPath = AtlasPath.PATH_SEPARATOR;
        if (collectionType == CollectionType.LIST) {
            rootPath += AtlasPath.PATH_LIST_START + AtlasPath.PATH_LIST_END;
        } else if (collectionType == CollectionType.ARRAY) {
            rootPath += AtlasPath.PATH_ARRAY_START + AtlasPath.PATH_ARRAY_END;
        } else if (collectionType == CollectionType.MAP) {
            rootPath += AtlasPath.PATH_MAP_START + AtlasPath.PATH_MAP_END;
        }
        if (collectionClassName != null && !collectionClassName.isEmpty()) {
            javaClass.setCollectionClassName(collectionClassName);
        }
        javaClass.setPath(rootPath);
        Set<String> cachedClasses = new HashSet<>();
        cachedClasses.add(clazz.getName()); // we cache ourself
        inspectClass(classLoader, clazz, javaClass, cachedClasses, rootPath);
        javaClass.setFieldType(getConversionService().fieldTypeFromClass(javaClass.getClassName()));
        return javaClass;
    }

    private void inspectClass(ClassLoader classLoader, Class<?> clazz, JavaClass javaClass, Set<String> cachedClasses,
            String pathPrefix) {

        Class<?> clz = clazz;
        if (clazz.isArray()) {
            javaClass.setArrayDimensions(detectArrayDimensions(clazz));
            javaClass.setCollectionType(CollectionType.ARRAY);
            clz = detectArrayClass(clazz);
            if (!javaClass.getPath().endsWith(AtlasPath.PATH_ARRAY_END)) {
                javaClass.setPath(javaClass.getPath() + AtlasPath.PATH_ARRAY_START + AtlasPath.PATH_ARRAY_END);
            }
        } else {
            clz = clazz;
        }

        if (isFieldMap(clz.getName())) {
            javaClass.setCollectionType(CollectionType.MAP);
        }

        javaClass.setClassName(clz.getName());
        javaClass.setCanonicalClassName(clz.getCanonicalName());
        javaClass.setPackageName((clz.getPackage() != null ? clz.getPackage().getName() : null));
        javaClass.setAnnotation(clz.isAnnotation());
        javaClass.setAnnonymous(clz.isAnonymousClass());
        javaClass.setEnumeration(clz.isEnum());
        javaClass.setInterface(clz.isInterface());
        javaClass.setLocalClass(clz.isLocalClass());
        javaClass.setMemberClass(clz.isMemberClass());
        javaClass.setPrimitive(clz.isPrimitive());
        javaClass.setSynthetic(clz.isSynthetic());

        if (javaClass.getUri() == null) {
            javaClass.setUri(String.format(AtlasJavaModelFactory.URI_FORMAT, AtlasUtil.escapeForUri(clz.getName())));
        }

        if (clz.isPrimitive() || JdkPackages.contains(clz.getPackage().getName())) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Skipping class " + clz.getName() + " which is a Jdk core class");
            }
            return;
        }

        // Process super class fields and methods first, so child class fields
        // and methods override
        Class<?> tmpClazz = clz;
        Class<?> superClazz = tmpClazz.getSuperclass();
        while (superClazz != null) {
            if (JdkPackages.contains(superClazz.getPackage().getName())) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Skipping SuperClass " + superClazz.getName() + " which is a Jdk core class");
                }
                superClazz = null;
            } else {
                inspectClassFields(classLoader, superClazz, javaClass, cachedClasses, pathPrefix);
                inspectClassMethods(classLoader, superClazz, javaClass, cachedClasses, pathPrefix);
                tmpClazz = superClazz;
                superClazz = tmpClazz.getSuperclass();
            }
        }

        inspectClassFields(classLoader, clz, javaClass, cachedClasses, pathPrefix);

        Object[] enumConstants = clz.getEnumConstants();
        if (enumConstants != null) {
            javaClass.setEnumeration(true);
            for (Object o : enumConstants) {
                JavaEnumField out = new JavaEnumField();
                if (o instanceof Enum) {
                    Enum<?> in = (Enum<?>) o;
                    out.setName(in.name());
                    out.setOrdinal(in.ordinal());
                    javaClass.getJavaEnumFields().getJavaEnumField().add(out);
                    out.setStatus(FieldStatus.SUPPORTED);
                } else {
                    out.setClassName(o.getClass().getName());
                    out.setStatus(FieldStatus.ERROR);
                }
            }
        } else {
            javaClass.setEnumeration(false);
        }

        inspectClassMethods(classLoader, clz, javaClass, cachedClasses, pathPrefix);

        if (javaClass.getModifiers() == null) {
            javaClass.setModifiers(new ModifierList());
        } else {
            javaClass.getModifiers().getModifier().clear();
        }
        javaClass.getModifiers().getModifier().addAll(detectModifiers(clz.getModifiers()));

        // TODO: annotations, generics, enums, class modifiers (public,
        // synchronized, etc),
        // more of these here:
        // https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html#isPrimitive--
        // TODO: exceptions
        // TODO: lists
        // return javaClass;
    }

    private JavaField inspectGetMethod(ClassLoader classLoader, Method m, JavaField s, Set<String> cachedClasses,
            String pathPrefix) {
        JavaField field = s;

        field.setName(StringUtil.getFieldNameFromGetter(m.getName()));

        if (pathPrefix != null && pathPrefix.length() > 0) {
            field.setPath(pathPrefix
                    + (pathPrefix.endsWith(AtlasPath.PATH_SEPARATOR) ? "" : AtlasPath.PATH_SEPARATOR)
                    + StringUtil.getFieldNameFromGetter(m.getName()));
        } else {
            field.setPath(StringUtil.getFieldNameFromGetter(m.getName()));
        }

        if (m.getParameterCount() != 0) {
            field.setStatus(FieldStatus.UNSUPPORTED);
            return field;
        }

        if (m.getReturnType().equals(Void.TYPE)) {
            field.setStatus(FieldStatus.UNSUPPORTED);
            return field;
        }

        Class<?> returnType = m.getReturnType();
        if (returnType.isArray()) {
            field.setCollectionType(CollectionType.ARRAY);
            field.setArrayDimensions(detectArrayDimensions(returnType));
            field.setPath(field.getPath() + AtlasPath.PATH_ARRAY_START + AtlasPath.PATH_ARRAY_END);
            returnType = detectArrayClass(returnType);
        } else if (Collection.class.isAssignableFrom(returnType)) {
            field.setCollectionType(CollectionType.LIST);
            field.setCollectionClassName(returnType.getName());
            field.setPath(field.getPath() + AtlasPath.PATH_LIST_START + AtlasPath.PATH_LIST_END);
            returnType = detectListClassFromMethodReturn(m);
        }

        field.setClassName(returnType.getName());
        field.setCanonicalClassName(returnType.getCanonicalName());
        field.setGetMethod(m.getName());
        field.setFieldType(getConversionService().fieldTypeFromClass(returnType));
        if (getConversionService().isPrimitive(returnType) || getConversionService().isBoxedPrimitive(returnType)) {
            field.setPrimitive(true);
            field.setStatus(FieldStatus.SUPPORTED);
        } else if (field.getFieldType() != FieldType.COMPLEX) {
            field.setPrimitive(false);
            field.setStatus(FieldStatus.SUPPORTED);
        } else {
            field.setPrimitive(false);

            Class<?> complexClazz = null;
            JavaClass tmpField = convertJavaFieldToJavaClass(field);
            field = tmpField;

            if (returnType.getName() == null) {
                field.setStatus(FieldStatus.UNSUPPORTED);
            } else if (!cachedClasses.contains(returnType.getName())) {
                try {
                    complexClazz = classLoader.loadClass(returnType.getName());
                    cachedClasses.add(returnType.getName());
                    inspectClass(classLoader, complexClazz, tmpField, cachedClasses, field.getPath());
                    if (tmpField.getStatus() == null) {
                        field.setStatus(FieldStatus.SUPPORTED);
                    }
                } catch (ClassNotFoundException cnfe) {
                    field.setStatus(FieldStatus.NOT_FOUND);
                }
            } else {
                field.setStatus(FieldStatus.CACHED);
            }
        }

        return field;
    }

    private JavaField inspectSetMethod(ClassLoader classLoader, Method m, JavaField s, Set<String> cachedClasses,
            String pathPrefix) {
        JavaField field = s;

        field.setName(StringUtil.getFieldNameFromSetter(m.getName()));

        if (pathPrefix != null && pathPrefix.length() > 0) {
            field.setPath(pathPrefix
                    + (pathPrefix.endsWith(AtlasPath.PATH_SEPARATOR) ? "" : AtlasPath.PATH_SEPARATOR)
                    + StringUtil.getFieldNameFromSetter(m.getName()));
        } else {
            field.setPath(StringUtil.getFieldNameFromSetter(m.getName()));
        }

        if (m.getParameterCount() != 1) {
            field.setStatus(FieldStatus.UNSUPPORTED);
            return field;
        }

        if (!m.getReturnType().equals(Void.TYPE)) {
            field.setStatus(FieldStatus.UNSUPPORTED);
            return field;
        }

        Class<?>[] params = m.getParameterTypes();
        if (params == null || params.length != 1) {
            field.setStatus(FieldStatus.UNSUPPORTED);
            return field;
        }

        Class<?> paramType = params[0];
        if (paramType.isArray()) {
            field.setCollectionType(CollectionType.ARRAY);
            field.setArrayDimensions(detectArrayDimensions(paramType));
            field.setPath(field.getPath() + AtlasPath.PATH_ARRAY_START + AtlasPath.PATH_ARRAY_END);
            paramType = detectArrayClass(paramType);
        } else if (Collection.class.isAssignableFrom(paramType)) {
            field.setCollectionType(CollectionType.LIST);
            field.setCollectionClassName(paramType.getName());
            field.setPath(field.getPath() + AtlasPath.PATH_LIST_START + AtlasPath.PATH_LIST_END);
            paramType = detectListClassFromMethodParameter(m);
        }

        field.setClassName(paramType.getName());
        field.setCanonicalClassName(paramType.getCanonicalName());
        field.setSetMethod(m.getName());
        field.setFieldType(getConversionService().fieldTypeFromClass(paramType));
        if (getConversionService().isPrimitive(paramType) || getConversionService().isBoxedPrimitive(paramType)) {
            field.setPrimitive(true);
            field.setStatus(FieldStatus.SUPPORTED);
        } else if (field.getFieldType() != FieldType.COMPLEX) {
            field.setPrimitive(false);
            field.setStatus(FieldStatus.SUPPORTED);
        } else {
            field.setPrimitive(false);

            Class<?> complexClazz = null;
            JavaClass tmpField = convertJavaFieldToJavaClass(field);
            field = tmpField;

            if (paramType.getName() == null) {
                field.setStatus(FieldStatus.UNSUPPORTED);
            } else if (!cachedClasses.contains(paramType.getName())) {
                try {
                    complexClazz = classLoader.loadClass(paramType.getName());
                    cachedClasses.add(paramType.getName());
                    inspectClass(classLoader, complexClazz, tmpField, cachedClasses, field.getPath());
                    if (tmpField.getStatus() == null) {
                        field.setStatus(FieldStatus.SUPPORTED);
                    }
                } catch (ClassNotFoundException cnfe) {
                    field.setStatus(FieldStatus.NOT_FOUND);
                }
            } else {
                field.setStatus(FieldStatus.CACHED);
            }
        }

        return field;
    }

    private JavaField inspectField(ClassLoader classLoader, Field f, Set<String> cachedClasses, String pathPrefix) {

        JavaField s = AtlasJavaModelFactory.createJavaField();
        Class<?> clazz = f.getType();
        s.setName(f.getName());

        if (pathPrefix != null && pathPrefix.length() > 0) {
            s.setPath(pathPrefix
                    + (pathPrefix.endsWith(AtlasPath.PATH_SEPARATOR) ? "" : AtlasPath.PATH_SEPARATOR)
                    + f.getName());
        } else {
            s.setPath(f.getName());
        }

        if (isFieldMap(clazz.getName())) {
            s.setCollectionType(CollectionType.MAP);
            s.setPath(s.getPath() + AtlasPath.PATH_MAP_START + AtlasPath.PATH_MAP_END);
        }

        if (clazz.isArray()) {
            s.setCollectionType(CollectionType.ARRAY);
            s.setArrayDimensions(detectArrayDimensions(clazz));
            s.setPath(s.getPath() + AtlasPath.PATH_ARRAY_START + AtlasPath.PATH_ARRAY_END);
            clazz = detectArrayClass(clazz);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            s.setCollectionType(CollectionType.LIST);
            s.setCollectionClassName(clazz.getName());
            s.setPath(s.getPath() + AtlasPath.PATH_LIST_START + AtlasPath.PATH_LIST_END);
            try {
                clazz = detectListClass(classLoader, f);
                if (clazz == null) {
                    s.setStatus(FieldStatus.ERROR);
                    return s;
                }
            } catch (ClassCastException | ClassNotFoundException cce) {
                LOG.debug("Error detecting inner listClass: " + cce.getMessage() + " for field: " + f.getName(), cce);
                s.setStatus(FieldStatus.ERROR);
                return s;
            }
        }

        s.setFieldType(getConversionService().fieldTypeFromClass(clazz));
        if (getConversionService().isPrimitive(clazz) || getConversionService().isBoxedPrimitive(clazz)) {
            s.setPrimitive(true);
            s.setStatus(FieldStatus.SUPPORTED);
        } else if (s.getFieldType() != FieldType.COMPLEX) {
            s.setPrimitive(false);
            s.setStatus(FieldStatus.SUPPORTED);
        } else {
            s.setPrimitive(false);

            Class<?> complexClazz = null;
            JavaClass tmpField = convertJavaFieldToJavaClass(s);
            s = tmpField;

            if (clazz.getName() == null) {
                s.setStatus(FieldStatus.UNSUPPORTED);
            } else if (!cachedClasses.contains(clazz.getName())) {
                try {
                    complexClazz = classLoader.loadClass(clazz.getName());
                    cachedClasses.add(clazz.getName());
                    inspectClass(classLoader, complexClazz, tmpField, cachedClasses, s.getPath());
                    if (tmpField.getStatus() == null) {
                        s.setStatus(FieldStatus.SUPPORTED);
                    }
                } catch (ClassNotFoundException cnfe) {
                    s.setStatus(FieldStatus.NOT_FOUND);
                }
            } else {
                s.setStatus(FieldStatus.CACHED);
            }
        }

        s.setClassName(clazz.getName());
        s.setCanonicalClassName(clazz.getCanonicalName());
        s.setSynthetic(f.isSynthetic());

        Annotation[] annotations = f.getAnnotations();
        if (annotations != null) {
            for (Annotation a : annotations) {
                if (s.getAnnotations() == null) {
                    s.setAnnotations(new StringList());
                }
                s.getAnnotations().getString().add(a.annotationType().getName());
            }
        }

        if (s.getModifiers() == null) {
            s.setModifiers(new ModifierList());
        }
        s.getModifiers().getModifier().addAll(detectModifiers(f.getModifiers()));

        List<String> pTypes = detectParameterizedTypes(f, false);
        if (pTypes != null) {
            if (s.getParameterizedTypes() == null) {
                s.setParameterizedTypes(new StringList());
            }
            s.getParameterizedTypes().getString().addAll(pTypes);
        }

        populateGetterSetter(clazz, f, s);
        return s;
    }

    private void populateGetterSetter(Class<?> clazz, Field reflectionField, JavaField atlasField) {
        try {
            String getterName = "get" + StringUtil.capitalizeFirstLetter(reflectionField.getName());
            reflectionField.getDeclaringClass().getMethod(getterName);
            atlasField.setGetMethod(getterName);
        } catch (NoSuchMethodException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No 'get' method for field named: " + reflectionField.getName() + " in class: "
                        + reflectionField.getDeclaringClass().getName());
            }
        }
        if (atlasField.getGetMethod() == null && ("boolean".equals(atlasField.getClassName())
                || "java.lang.Boolean".equals(atlasField.getClassName()))) {
            try {
                String getterName = "is" + StringUtil.capitalizeFirstLetter(reflectionField.getName());
                reflectionField.getDeclaringClass().getMethod(getterName);
                atlasField.setGetMethod(getterName);
            } catch (NoSuchMethodException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No 'is' method for field named: " + reflectionField.getName() + " in class: "
                            + reflectionField.getDeclaringClass().getName());
                }
            }
        }
        try {
            String setterName = "set" + StringUtil.capitalizeFirstLetter(reflectionField.getName());
            reflectionField.getDeclaringClass().getMethod(setterName, clazz);
            atlasField.setSetMethod(setterName);
        } catch (NoSuchMethodException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No 'set' method for field named: " + reflectionField.getName() + " in class: "
                        + reflectionField.getDeclaringClass().getName());
            }
        }
    }

    private void inspectClassFields(ClassLoader classLoader, Class<?> clazz, JavaClass javaClass,
            Set<String> cachedClasses, String pathPrefix) {
        Set<String> existing = javaClass.getJavaFields().getJavaField().stream()
            .map(JavaField::getName).collect(Collectors.toSet());
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null && !javaClass.isEnumeration()) {
            for (Field f : fields) {
                JavaField s = inspectField(classLoader, f, cachedClasses, pathPrefix);

                if (existing.contains(f.getName())) {
                    LOG.warn("Ignoring hidden Java field: " + s.getName());
                    continue;
                }
                if (getFieldExclusions().contains(f.getName())) {
                    s.setStatus(FieldStatus.EXCLUDED);
                }

                // skip synthetic members
                if (s.isSynthetic() != null && s.isSynthetic()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Synthetic field class detected: " + s.getName());
                    }
                    continue;
                }

                if (s.getGetMethod() == null && s.getSetMethod() == null) {
                    if (s.getModifiers().getModifier().contains(io.atlasmap.java.v2.Modifier.PRIVATE)
                            && !getDisablePrivateOnlyFields()) {
                        javaClass.getJavaFields().getJavaField().add(s);
                    } else if (s.getModifiers().getModifier().contains(io.atlasmap.java.v2.Modifier.PROTECTED)
                            && !getDisableProtectedOnlyFields()) {
                        javaClass.getJavaFields().getJavaField().add(s);
                    } else if (s.getModifiers().getModifier().contains(io.atlasmap.java.v2.Modifier.PUBLIC)
                            && !getDisablePublicOnlyFields()) {
                        javaClass.getJavaFields().getJavaField().add(s);
                    } else if (s.getModifiers().getModifier().contains(io.atlasmap.java.v2.Modifier.PACKAGE_PRIVATE)
                            && !getDisablePackagePrivateOnlyFields()) {
                        javaClass.getJavaFields().getJavaField().add(s);
                    }
                } else if (!getDisablePublicGetterSetterFields()) {
                    javaClass.getJavaFields().getJavaField().add(s);
                }
            }
        }
    }

    private void inspectClassMethods(ClassLoader classLoader, Class<?> clazz, JavaClass javaClass,
            Set<String> cachedClasses, String pathPrefix) {
        Method[] methods = clazz.getDeclaredMethods();
        if (methods != null && !javaClass.isEnumeration()) {
            for (Method m : methods) {
                JavaField s = AtlasJavaModelFactory.createJavaField();
                s.setName(m.getName());
                s.setSynthetic(m.isSynthetic());

                if (m.isVarArgs() || m.isBridge() || m.isSynthetic() || m.isDefault()) {
                    s.setStatus(FieldStatus.UNSUPPORTED);
                    LOG.warn("VarArg, Bridge, Synthetic or Default method " + m.getName() + " detected");
                    continue;
                }
                s.setSynthetic(m.isSynthetic());

                if (m.getName().startsWith("get") || m.getName().startsWith("is")) {
                    s = inspectGetMethod(classLoader, m, s, cachedClasses, pathPrefix);
                }

                if (m.getName().startsWith("set")) {
                    s = inspectSetMethod(classLoader, m, s, cachedClasses, pathPrefix);
                }

                boolean found = false;
                for (int i = 0; i < javaClass.getJavaFields().getJavaField().size(); i++) {
                    JavaField exists = javaClass.getJavaFields().getJavaField().get(i);
                    if (s.getName().equals(exists.getName())) {
                        found = true;

                        // Merge get/set method info for interfaces that don't
                        // have fields
                        if (exists.getGetMethod() == null && s.getGetMethod() != null) {
                            exists.setGetMethod(s.getGetMethod());
                        }
                        if (exists.getSetMethod() == null && s.getSetMethod() != null) {
                            exists.setSetMethod(s.getSetMethod());
                        }
                    }
                }

                if (found) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Field already defined for method: " + m.getName() + " class: " + clazz.getName());
                    }
                } else if (s.getGetMethod() != null || s.getSetMethod() != null) {
                    javaClass.getJavaFields().getJavaField().add(s);
                } else {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Ignoring non-field method: " + m.getName() + " class: " + clazz.getName());
                    }
                }
            }
        }
    }

    protected boolean isFieldMap(String fieldType) {
        return getMapClasses().contains(fieldType);
    }

    private Integer detectArrayDimensions(Class<?> clazz) {
        Integer arrayDim = Integer.valueOf(0);
        if (clazz == null) {
            return null;
        }

        if (!clazz.isArray()) {
            return arrayDim;
        }
        arrayDim++;

        Class<?> tmpClazz = clazz.getComponentType();
        while (tmpClazz != null && tmpClazz.isArray() && arrayDim < MAX_ARRAY_DIM_LIMIT) {
            arrayDim++;
            tmpClazz = tmpClazz.getComponentType();
        }
        return arrayDim;
    }

    private List<io.atlasmap.java.v2.Modifier> detectModifiers(int m) {
        List<io.atlasmap.java.v2.Modifier> modifiers = new ArrayList<>();
        if (Modifier.isAbstract(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.ABSTRACT);
        }
        if (Modifier.isFinal(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.FINAL);
        }
        if (Modifier.isInterface(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.INTERFACE);
        }
        if (Modifier.isNative(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.NATIVE);
        }
        if (Modifier.isPrivate(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.PRIVATE);
        }
        if (Modifier.isProtected(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.PROTECTED);
        }
        if (Modifier.isPublic(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.PUBLIC);
        }
        if (!Modifier.isPrivate(m) && !Modifier.isProtected(m) && !Modifier.isPublic(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.PACKAGE_PRIVATE);
        }
        if (Modifier.isStatic(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.STATIC);
        }
        if (Modifier.isStrict(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.STRICT);
        }
        if (Modifier.isSynchronized(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.SYNCHRONIZED);
        }
        if (Modifier.isTransient(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.TRANSIENT);
        }
        if (Modifier.isVolatile(m)) {
            modifiers.add(io.atlasmap.java.v2.Modifier.VOLATILE);
        }
        return modifiers;
    }

    private Class<?> detectListClass(ClassLoader classLoader, Field field) throws ClassNotFoundException {
        List<String> types = detectParameterizedTypes(field, true);
        if (types != null && !types.isEmpty()) {
            return classLoader.loadClass(types.get(0));
        }
        return null;
    }

    private Class<?> detectListClassFromMethodReturn(Method m) {
        return ClassHelper.detectClassFromTypeArgument(m.getGenericReturnType());
    }

    private Class<?> detectListClassFromMethodParameter(Method m) {
        return ClassHelper.detectClassFromTypeArgument(m.getGenericParameterTypes()[0]);
    }

    private Class<?> detectArrayClass(Class<?> clazz) {
        Integer arrayDim = new Integer(0);
        if (clazz == null) {
            return null;
        }

        if (!clazz.isArray()) {
            return clazz;
        }
        arrayDim++;

        Class<?> tmpClazz = clazz.getComponentType();
        while (tmpClazz != null && tmpClazz.isArray() && arrayDim < MAX_ARRAY_DIM_LIMIT) {
            arrayDim++;
            tmpClazz = tmpClazz.getComponentType();
        }
        return tmpClazz;
    }

    private List<String> detectParameterizedTypes(Field field, boolean onlyClasses) {
        List<String> pTypes = null;

        if (field == null || field.getGenericType() == null || !(field.getGenericType() instanceof ParameterizedType)) {
            return null;
        }
        Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
        if (types.length == 0) {
            return null;
        }

        for (Type t : types) {
            if (pTypes == null) {
                pTypes = new ArrayList<>();
            }

            if (!onlyClasses && t instanceof TypeVariable) {
                TypeVariable<?> tv = (TypeVariable<?>) t;
                // TODO: no current need, but we may want to have treatment for 'T'
                // tv.getTypeName()
                Type type = tv.getAnnotatedBounds()[0].getType();
                if (type instanceof Class) {
                    pTypes.add(((Class<?>) type).getName());
                } else {
                    pTypes.add(type.getTypeName());
                }
            }

            if (!onlyClasses && t instanceof WildcardType) {
                WildcardType wc = (WildcardType) t;
                Type[] upperBounds = wc.getUpperBounds();
                Type[] lowerBounds = wc.getLowerBounds();
                // TODO: No current need, but we may want to have treatment for '?'
                // wc.getTypeName()
                if (upperBounds != null && upperBounds.length > 0) {
                    pTypes.add(wc.getUpperBounds()[0].getClass().getName());
                } else if (lowerBounds != null && lowerBounds.length > 0) {
                    pTypes.add(wc.getLowerBounds()[0].getClass().getName());
                }
            }

            if (t instanceof Class) {
                pTypes.add(((Class<?>) t).getName());
            }
        }
        return pTypes;
    }

    private JavaClass convertJavaFieldToJavaClass(JavaField javaField) {
        JavaClass javaClass = AtlasJavaModelFactory.createJavaClass();
        javaClass.setArrayDimensions(javaField.getArrayDimensions());
        javaClass.setArraySize(javaField.getArraySize());
        javaClass.setCollectionClassName(javaField.getCollectionClassName());
        javaClass.setCollectionType(javaField.getCollectionType());
        javaClass.setDocId(javaField.getDocId());
        javaClass.setPrimitive(javaField.isPrimitive());
        javaClass.setSynthetic(javaField.isSynthetic());
        javaClass.setClassName(javaField.getClassName());
        javaClass.setGetMethod(javaField.getGetMethod());
        javaClass.setName(javaField.getName());
        javaClass.setPath(javaField.getPath());
        javaClass.setRequired(javaField.isRequired());
        javaClass.setSetMethod(javaField.getSetMethod());
        javaClass.setStatus(javaField.getStatus());
        javaClass.setFieldType(javaField.getFieldType());
        if (javaField.getClassName() != null) {
            javaClass.setUri(String.format(AtlasJavaModelFactory.URI_FORMAT, AtlasUtil.escapeForUri(javaField.getClassName())));
        }
        javaClass.setValue(javaField.getValue());
        javaClass.setAnnotations(javaField.getAnnotations());
        javaClass.setModifiers(javaField.getModifiers());
        javaClass.setParameterizedTypes(javaField.getParameterizedTypes());
        return javaClass;
    }

    private AtlasConversionService getConversionService() {
        return atlasConversionService;
    }

    public void setConversionService(AtlasConversionService atlasConversionService) {
        this.atlasConversionService = atlasConversionService;
    }

}
