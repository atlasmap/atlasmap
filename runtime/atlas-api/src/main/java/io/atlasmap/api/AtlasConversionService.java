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
package io.atlasmap.api;

import io.atlasmap.v2.FieldType;

import java.lang.reflect.Method;
import java.util.Optional;

public interface AtlasConversionService {

    Optional<AtlasConverter<?>> findMatchingConverter(FieldType source, FieldType target);

    Optional<AtlasConverter<?>> findMatchingConverter(String sourceClassName, String targetClassName);

    Optional<Method> findMatchingMethod(FieldType source, FieldType target, AtlasConverter<?> customConverter);

    Object copyPrimitive(Object sourceValue);

    Object convertType(Object sourceValue, FieldType sourceType, FieldType targetType) throws AtlasConversionException;

    Object convertType(Object sourceValue, FieldType sourceType, FieldType targetType, String customClassName)
            throws AtlasConversionException;

    Class<?> boxOrUnboxPrimitive(Class<?> clazz);

    Class<?> classFromFieldType(FieldType fieldType);

    FieldType fieldTypeFromClass(Class<?> clazz);

    FieldType fieldTypeFromClass(String className);

    Boolean isPrimitive(String className);

    Boolean isPrimitive(Class<?> clazz);

    Boolean isPrimitive(FieldType fieldType);

    Boolean isBoxedPrimitive(Class<?> clazz);

}
