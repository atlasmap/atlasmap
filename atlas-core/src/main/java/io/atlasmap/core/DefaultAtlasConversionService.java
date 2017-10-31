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
package io.atlasmap.core;

import io.atlasmap.api.AtlasConverter;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultAtlasConversionService implements AtlasConversionService {

    private static Logger logger = LoggerFactory.getLogger(DefaultAtlasConversionService.class);
    private static DefaultAtlasConversionService instance = null;
    private Map<String, AtlasConverter<?>> converters = null;

    private static final Set<String> PRIMITIVE_CLASSNAMES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("boolean", "byte", "char", "double", "float", "int", "long", "short")));

    private static final Set<FieldType> PRIMITIVE_FIELDTYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(FieldType.BOOLEAN, FieldType.BYTE, FieldType.CHAR,
                    FieldType.DECIMAL, FieldType.DOUBLE, FieldType.FLOAT, FieldType.INTEGER,
                    FieldType.LONG, FieldType.SHORT, FieldType.STRING)));

    private static final Set<String> BOXED_PRIMITIVE_CLASSNAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("java.lang.Boolean", "java.lang.Byte", "java.lang.Character", "java.lang.Double",
                    "java.lang.Float", "java.lang.Integer", "java.lang.Long", "java.lang.Short", "java.lang.String")));

    private DefaultAtlasConversionService() {
    }

    public static DefaultAtlasConversionService getInstance() {
        if (instance == null) {
            instance = new DefaultAtlasConversionService();
            instance.init();
        }
        return instance;
    }

    public static Set<String> listPrimitiveClassNames() {
        return PRIMITIVE_CLASSNAMES;
    }

    @Override
    public Optional<AtlasConverter<?>> findMatchingConverter(FieldType source, FieldType target) {

        Optional<AtlasConverter<?>> primitiveConverter = Optional.empty();
        Optional<AtlasConverter<?>> customConverter = Optional.empty();

        List<AtlasPrimitiveConverter<?>> primitiveConverters = converters.values().stream()
                .filter(p -> p instanceof AtlasPrimitiveConverter).map(p -> (AtlasPrimitiveConverter<?>) p)
                .collect(Collectors.toList());
        primitiveConverter = checkPrimitiveConverters(primitiveConverters, source, target);

        List<AtlasConverter<?>> customConverters = converters.values().stream()
                .filter(not(p -> p instanceof AtlasPrimitiveConverter)).collect(Collectors.toList());
        customConverter = checkCustomConverters(customConverters, source, target);

        // prefer the custom converter over the primitive
        if (primitiveConverter.isPresent() && !customConverter.isPresent()) {
            return primitiveConverter;
        } else {
            return customConverter;
        }
    }

    @Override
    public Optional<AtlasConverter<?>> findMatchingConverter(String sourceClassName, String targetClassName) {
        // assuming only custom converters define sourceClassName / targetClassName and
        // must match exactly.
        List<AtlasConverter<?>> customConverters = converters.values().stream()
                .filter(not(p -> p instanceof AtlasPrimitiveConverter)).collect(Collectors.toList());
        for (AtlasConverter<?> converter : customConverters) {
            if (findConverterByMethodAnnotationClassName(sourceClassName, targetClassName, converter)) {
                return Optional.of(converter);
            }
        }
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Optional<Method> findMatchingMethod(FieldType source, FieldType target, AtlasConverter customConverter) {
        Method[] methods = customConverter.getClass().getMethods();
        // assuming only one
        return Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(AtlasConversionInfo.class)
                        && (method.getAnnotation(AtlasConversionInfo.class) != null)
                        && (method.getAnnotation(AtlasConversionInfo.class).sourceType().compareTo(source) == 0)
                        && (method.getAnnotation(AtlasConversionInfo.class).targetType().compareTo(target) == 0))
                .findFirst();
    }

    private Optional<AtlasConverter<?>> checkCustomConverters(List<AtlasConverter<?>> customConverters, FieldType source,
            FieldType target) {
        if (source == null || target == null) {
            // TODO: investigate how we handle when sType -> tType (null -> something and
            // something -> null)
            return Optional.empty();
        }

        for (AtlasConverter<?> customConverter : customConverters) {
            if (findConverterByMethodAnnotationSourceType(source, target, customConverter)) {
                return Optional.of(customConverter);
            }
        }

        return Optional.empty();
    }

    private Optional<AtlasConverter<?>> checkPrimitiveConverters(List<AtlasPrimitiveConverter<?>> primitiveConverters,
            FieldType source, FieldType target) {
        if (source == null || target == null) {
            // TODO: investigate how we handle when sType -> tType (null -> something and
            // something -> null)
            return Optional.empty();
        }
        for (AtlasPrimitiveConverter<?> primitiveConverter : primitiveConverters) {
            // get all the methods --> getAnnotations of Type AtlasConversionInfo
            if (findConverterByMethodAnnotationSourceType(source, target, primitiveConverter)) {
                return Optional.of(primitiveConverter);
            }
        }
        return Optional.empty();
    }

    private boolean findConverterByMethodAnnotationSourceType(FieldType source, FieldType target,
            AtlasConverter<?> customConverter) {
        Method[] methods = customConverter.getClass().getMethods();
        return Arrays.stream(methods).map(method -> method.getAnnotation(AtlasConversionInfo.class))
                .anyMatch(atlasConversionInfo -> (atlasConversionInfo != null
                        && atlasConversionInfo.sourceType().compareTo(source) == 0
                        && atlasConversionInfo.targetType().compareTo(target) == 0));
    }

    private boolean findConverterByMethodAnnotationClassName(String sourceClassName, String targetClassName,
            AtlasConverter<?> customConverter) {
        Method[] methods = customConverter.getClass().getMethods();
        return Arrays.stream(methods).map(method -> method.getAnnotation(AtlasConversionInfo.class))
                .anyMatch(atlasConversionInfo -> (atlasConversionInfo != null
                        && atlasConversionInfo.sourceClassName().equals(sourceClassName)
                        && atlasConversionInfo.targetClassName().equals(targetClassName)));
    }

    private void init() {
        loadConverters();
    }

    @SuppressWarnings("rawtypes")
    private void loadConverters() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        final ServiceLoader<AtlasConverter> converterServiceLoader = ServiceLoader.load(AtlasConverter.class,
                classLoader);
        Map<String, AtlasConverter<?>> tmp = new LinkedHashMap<>();
        for (final AtlasConverter<?> atlasConverter : converterServiceLoader) {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading converter : " + atlasConverter.getClass().getCanonicalName());
            }
            tmp.put(atlasConverter.getClass().getCanonicalName(), atlasConverter);
        }
        if (!tmp.isEmpty()) {
            converters = Collections.unmodifiableMap(tmp);
        }
    }

    private static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }

    @Override
    public Object copyPrimitive(Object sourceValue) {

        if (sourceValue == null) {
            return null;
        }

        Class<?> clazz = sourceValue.getClass();
        if (clazz == null) {
            return clazz;
        } else if (boolean.class.getName().equals(clazz.getName())) {
            return Boolean.valueOf((boolean) sourceValue);
        } else if (Boolean.class.getName().equals(clazz.getName())) {
            return Boolean.valueOf((Boolean) sourceValue);
        } else if (byte.class.getName().equals(clazz.getName())) {
            return Byte.valueOf((byte) sourceValue);
        } else if (Byte.class.getName().equals(clazz.getName())) {
            return Byte.valueOf((Byte) sourceValue);
        } else if (char.class.getName().equals(clazz.getName())) {
            return Character.valueOf((char) sourceValue);
        } else if (Character.class.getName().equals(clazz.getName())) {
            return Character.valueOf((Character) sourceValue);
        } else if (double.class.getName().equals(clazz.getName())) {
            return Double.valueOf((double) sourceValue);
        } else if (Double.class.getName().equals(clazz.getName())) {
            return Double.valueOf((Double) sourceValue);
        } else if (float.class.getName().equals(clazz.getName())) {
            return Float.valueOf((float) sourceValue);
        } else if (Float.class.getName().equals(clazz.getName())) {
            return Float.valueOf((Float) sourceValue);
        } else if (int.class.getName().equals(clazz.getName())) {
            return Integer.valueOf((int) sourceValue);
        } else if (Integer.class.getName().equals(clazz.getName())) {
            return Integer.valueOf((Integer) sourceValue);
        } else if (long.class.getName().equals(clazz.getName())) {
            return Long.valueOf((long) sourceValue);
        } else if (Long.class.getName().equals(clazz.getName())) {
            return Long.valueOf((Long) sourceValue);
        } else if (short.class.getName().equals(clazz.getName())) {
            return Short.valueOf((short) sourceValue);
        } else if (Short.class.getName().equals(clazz.getName())) {
            return Short.valueOf((Short) sourceValue);
        }

        // can't count on java copy
        return sourceValue;
    }

    @Override
    public Object convertType(Object sourceValue, FieldType sourceType, FieldType targetType, String customClassName)
            throws AtlasConversionException {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object convertType(Object sourceValue, FieldType origSourceType, FieldType targetType)
            throws AtlasConversionException {
        FieldType sourceType = null;

        if (origSourceType == null && sourceValue != null) {
            sourceType = fieldTypeFromClass(sourceValue.getClass());
        } else {
            sourceType = FieldType.fromValue(origSourceType.value());
        }

        if (sourceType == null && targetType == null) {
            throw new AtlasConversionException("AutoConversion requires sourceType and targetType be specified");
        }

        if (sourceType.equals(targetType)) {
            return sourceValue;
        }

        Optional<AtlasConverter<?>> converter = findMatchingConverter(sourceType, targetType);
        if (!converter.isPresent()) {
            throw new AtlasConversionException(
                    "Converter not found for sourceType: " + sourceType + " targetType: " + targetType);
        }

        AtlasConverter<?> atlasConverter = converter.get();
        if (isPrimitive(sourceType) && isPrimitive(targetType)) {
            switch (targetType) {
            case BOOLEAN:
                return ((AtlasPrimitiveConverter) atlasConverter).convertToBoolean(sourceValue);
            case BYTE:
                return ((AtlasPrimitiveConverter) atlasConverter).convertToByte(sourceValue);
            case CHAR:
                return ((AtlasPrimitiveConverter) atlasConverter).convertToCharacter(sourceValue);
            case DOUBLE:
                return ((AtlasPrimitiveConverter) atlasConverter).convertToDouble(sourceValue);
            case FLOAT:
                return ((AtlasPrimitiveConverter) atlasConverter).convertToFloat(sourceValue);
            case INTEGER:
                return ((AtlasPrimitiveConverter) atlasConverter).convertToInteger(sourceValue);
            case LONG:
                return ((AtlasPrimitiveConverter) atlasConverter).convertToLong(sourceValue);
            case SHORT:
                return ((AtlasPrimitiveConverter) atlasConverter).convertToShort(sourceValue);
            case STRING:
                return ((AtlasPrimitiveConverter) atlasConverter).convertToString(sourceValue);
            default:
                throw new AtlasConversionException(
                        "AutoConversion is not supported for sT=" + sourceType + " tT=" + targetType);
            }
        } else {
            // TODO: Support non-primitive auto conversion
            throw new AtlasConversionException("AutoConversion of non-primitives is not supported");
        }

    }

    @Override
    public Boolean isPrimitive(String className) {
        if (className == null) {
            return false;
        }
        if (PRIMITIVE_CLASSNAMES.contains(className)) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean isPrimitive(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        if (PRIMITIVE_CLASSNAMES.contains(clazz.getCanonicalName())) {
            return true;
        }
        return false;
    }

    @Override
    public Boolean isPrimitive(FieldType fieldType) {
        if (fieldType == null) {
            return false;
        }
        return PRIMITIVE_FIELDTYPES.contains(fieldType);
    }

    @Override
    public Boolean isBoxedPrimitive(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return BOXED_PRIMITIVE_CLASSNAMES.contains(clazz.getCanonicalName());
    }

    @Override
    public Class<?> boxOrUnboxPrimitive(Class<?> clazz) {
        if (clazz == null) {
            return clazz;
        } else if (boolean.class.getName().equals(clazz.getName())) {
            return Boolean.class;
        } else if (Boolean.class.getName().equals(clazz.getName())) {
            return boolean.class;
        } else if (byte.class.getName().equals(clazz.getName())) {
            return Byte.class;
        } else if (Byte.class.getName().equals(clazz.getName())) {
            return byte.class;
        } else if (char.class.getName().equals(clazz.getName())) {
            return Character.class;
        } else if (Character.class.getName().equals(clazz.getName())) {
            return char.class;
        } else if (double.class.getName().equals(clazz.getName())) {
            return Double.class;
        } else if (Double.class.getName().equals(clazz.getName())) {
            return double.class;
        } else if (float.class.getName().equals(clazz.getName())) {
            return Float.class;
        } else if (Float.class.getName().equals(clazz.getName())) {
            return float.class;
        } else if (int.class.getName().equals(clazz.getName())) {
            return Integer.class;
        } else if (Integer.class.getName().equals(clazz.getName())) {
            return int.class;
        } else if (long.class.getName().equals(clazz.getName())) {
            return Long.class;
        } else if (Long.class.getName().equals(clazz.getName())) {
            return long.class;
        } else if (short.class.getName().equals(clazz.getName())) {
            return Short.class;
        } else if (Short.class.getName().equals(clazz.getName())) {
            return short.class;
        }
        return clazz;
    }

    @Override
    public FieldType fieldTypeFromClass(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        return fieldTypeFromClass(clazz.getName());
    }

    @Override
    public FieldType fieldTypeFromClass(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }

        switch (className) {
        case "boolean":
            return FieldType.BOOLEAN;
        case "java.lang.Boolean":
            return FieldType.BOOLEAN;
        case "byte":
            return FieldType.BYTE;
        case "java.lang.Byte":
            return FieldType.BYTE;
        case "char":
            return FieldType.CHAR;
        case "java.lang.Character":
            return FieldType.CHAR;
        case "double":
            return FieldType.DOUBLE;
        case "java.lang.Double":
            return FieldType.DOUBLE;
        case "float":
            return FieldType.FLOAT;
        case "java.lang.Float":
            return FieldType.FLOAT;
        case "int":
            return FieldType.INTEGER;
        case "java.lang.Integer":
            return FieldType.INTEGER;
        case "long":
            return FieldType.LONG;
        case "java.lang.Long":
            return FieldType.LONG;
        case "short":
            return FieldType.SHORT;
        case "java.lang.Short":
            return FieldType.SHORT;
        case "java.lang.String":
            return FieldType.STRING;
        case "java.time.Year":
        case "java.time.Month":
        case "java.time.YearMonth":
        case "java.time.MonthDay":
        case "java.time.LocalDate":
            return FieldType.DATE;
        case "java.time.LocalTime":
            return FieldType.TIME;
        case "java.time.LocalDateTime":
            return FieldType.DATE_TIME;
        case "java.sql.Date":
        case "java.util.Date":
        case "java.time.ZonedDateTime":
            return FieldType.DATE_TIME_TZ;
        default:
            return FieldType.COMPLEX;
        }
    }

    @Override
    public Class<?> classFromFieldType(FieldType fieldType) {
        if (fieldType == null) {
            return null;
        }

        switch (fieldType) {
        case BOOLEAN:
            return Boolean.class;
        case BYTE:
            return Byte.class;
        case CHAR:
            return java.lang.Character.class;
        case DOUBLE:
            return java.lang.Double.class;
        case FLOAT:
            return java.lang.Float.class;
        case INTEGER:
            return java.lang.Integer.class;
        case LONG:
            return java.lang.Long.class;
        case SHORT:
            return java.lang.Short.class;
        case STRING:
            return java.lang.String.class;
        case DATE:
            return java.time.LocalDate.class;
        case TIME:
            return java.time.LocalTime.class;
        case DATE_TIME:
            return java.time.LocalDateTime.class;
        case DATE_TZ:
        case TIME_TZ:
        case DATE_TIME_TZ:
            return java.time.ZonedDateTime.class;
        // TODO: need to fix the default return type for non-primitive
        default:
            return java.lang.Object.class;
        }
    }

}
