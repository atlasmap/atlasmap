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
package io.atlasmap.spi;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConverter;

public interface AtlasPrimitiveConverter<T> extends AtlasConverter<T> {

    Boolean convertToBoolean(T value) throws AtlasConversionException;

    Byte convertToByte(T value) throws AtlasConversionException;

    Character convertToCharacter(T value) throws AtlasConversionException;

    Double convertToDouble(T value) throws AtlasConversionException;

    Float convertToFloat(T value) throws AtlasConversionException;

    Integer convertToInteger(T value) throws AtlasConversionException;

    Long convertToLong(T value) throws AtlasConversionException;

    Short convertToShort(T value) throws AtlasConversionException;

    String convertToString(T value) throws AtlasConversionException;

}
