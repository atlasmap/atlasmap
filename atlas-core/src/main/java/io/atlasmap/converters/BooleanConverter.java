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
package io.atlasmap.converters;

import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

public class BooleanConverter implements AtlasPrimitiveConverter<Boolean> {

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.BOOLEAN)
    public Boolean convertToBoolean(Boolean value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // we want a new object
        return new Boolean(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Byte convertToByte(Boolean value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(
                new AtlasUnsupportedException("Boolean to Byte conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.CHAR)
    public Character convertToCharacter(Boolean value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return (char) (value ? 1 : 0);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.DOUBLE)
    public Double convertToDouble(Boolean value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value ? 1.0d : 0.0d;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.FLOAT)
    public Float convertToFloat(Boolean value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        return value ? 1.0f : 0.0f;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.INTEGER)
    public Integer convertToInteger(Boolean value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value ? 1 : 0;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.LONG)
    public Long convertToLong(Boolean value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value ? 1l : 0l;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.SHORT)
    public Short convertToShort(Boolean value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return (short) (value ? 1 : 0);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BOOLEAN, targetType = FieldType.STRING)
    public String convertToString(Boolean value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return String.valueOf((value ? "true" : "false"));
    }

}
