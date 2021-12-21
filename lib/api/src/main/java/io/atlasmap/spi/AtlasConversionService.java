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
package io.atlasmap.spi;

import java.util.Optional;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.v2.FieldType;

/**
 * The AtlasMap conversion service resolves the appropriate {@link AtlasConverter} to convert the field value and apply.
 */
public interface AtlasConversionService {

    /**
     * Finds matching {@link AtlasConverter}.
     * @param source source type
     * @param target target type
     * @return converter
     */
    Optional<AtlasConverter<?>> findMatchingConverter(FieldType source, FieldType target);

    /**
     * Finds matching {@link AtlasConverter}.
     * @param sourceClassName source class name
     * @param targetClassName target class name
     * @return converter
     */
    Optional<AtlasConverter<?>> findMatchingConverter(String sourceClassName, String targetClassName);

    /**
     * Copies the primitive value.
     * @param sourceValue source value
     * @return copied value
     */
    Object copyPrimitive(Object sourceValue);

    /**
     * Perform type conversion with specifying {@link FieldType}. This method focuses on
     * conversion between different field types with using AtlasMap field type representative classes.
     * Use {@link #convertType(Object, String, Class, String)} to convert into specific Java class.
     * @see #convertType(Object, String, Class, String)
     *
     * @param sourceValue source value to convert
     * @param origSourceType {@link FieldType} of source field
     * @param targetType {@link FieldType} of target field
     * @return converted value
     * @throws AtlasConversionException conversion failed
     */
    Object convertType(Object sourceValue, FieldType origSourceType, FieldType targetType)
            throws AtlasConversionException;

    /**
     * Perform type conversion with specifying {@link FieldType}. This method focuses on
     * conversion between different field types with using AtlasMap field type representative classes.
     * Use {@link #convertType(Object, String, Class, String)} to convert into specific Java class.
     * @see #convertType(Object, String, Class, String)
     *
     * @param sourceValue source value to convert
     * @param sourceFormat source value format
     * @param targetType {@link FieldType} of target field
     * @param targetFormat target value format
     * @return converted value
     * @throws AtlasConversionException conversion failed
     */
    Object convertType(Object sourceValue, String sourceFormat, FieldType targetType, String targetFormat)
            throws AtlasConversionException;

    /**
     * Perform type conversion with specifying target {@link Class} regardless of AtlasMap {@link FieldType}.
     * @see #convertType(Object, FieldType, FieldType)
     *
     * @param sourceValue source value to convert
     * @param sourceFormat source value format
     * @param targetType {@link Class} of target value
     * @param targetFormat target value format
     * @return converted value
     * @throws AtlasConversionException conversion failed
     */
    Object convertType(Object sourceValue, String sourceFormat, Class<?> targetType, String targetFormat)
            throws AtlasConversionException;

    /**
     * Check if the convertion is available for the specified value.
     * @param sourceValue source value
     * @param targetType target type
     * @return true if available
     */
    boolean isConvertionAvailableFor(Object sourceValue, Class<?> targetType);

    /**
     * Boxes or unboxes the primitive value.
     * @param clazz class
     * @return result
     */
    Class<?> boxOrUnboxPrimitive(Class<?> clazz);

    /**
     * Boxes or unboxes the primitive value.
     * @param clazzName class name
     * @return result
     */
    Class<?> boxOrUnboxPrimitive(String clazzName);

    /**
     * Gets the class from {@link FieldType}.
     * @param fieldType field type
     * @return class
     */
    Class<?> classFromFieldType(FieldType fieldType);

    /**
     * Gets the {@link FieldType} from class.
     * @param clazz class
     * @return field type
     */
    FieldType fieldTypeFromClass(Class<?> clazz);

    /**
     * Gets the {@link FieldType} from class name.
     * @param className class name
     * @return field type
     */
    FieldType fieldTypeFromClass(String className);

    /**
     * Check if the class is primitive.
     * @param className class name
     * @return true if it's primitive class
     */
    Boolean isPrimitive(String className);

    /**
     * Check if the class is primitive.
     * @param clazz class
     * @return true if it's primitive class
     */
    Boolean isPrimitive(Class<?> clazz);

    /**
     * Check if the field type is primitive.
     * @param fieldType field type
     * @return true if it's primitive type
     */
    Boolean isPrimitive(FieldType fieldType);

    /**
     * Check if the class is a boxed primitive.
     * @param clazz class
     * @return true if it's boxed primitive
     */
    Boolean isBoxedPrimitive(Class<?> clazz);

    /**
     * Check if the source and target field types are assignable.
     * @param source source type
     * @param target target type
     * @return true if it's assignable
     */
    Boolean isAssignableFieldType(FieldType source, FieldType target);

}
