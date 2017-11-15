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

public class ShortConverter implements AtlasPrimitiveConverter<Short> {

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.RANGE)
    public Boolean convertToBoolean(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value == 0 || value == 1) {
            if (value == 1) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
        throw new AtlasConversionException(String.format("Short %s cannot be converted to a Boolean", value));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Byte convertToByte(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(new AtlasUnsupportedException("Short to Byte conversion is not supported"));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.RANGE)
    public Character convertToCharacter(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        if (value < Character.MIN_VALUE) {
            throw new AtlasConversionException(String.format("Short %s is less than Character.MIN_VALUE", value));
        }
        return Character.valueOf((char) value.shortValue());
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.DOUBLE)
    public Double convertToDouble(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.FLOAT)
    public Float convertToFloat(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value.floatValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.INTEGER)
    public Integer convertToInteger(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.LONG)
    public Long convertToLong(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value.longValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.SHORT)
    public Short convertToShort(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // we want a copy of the value
        return new Short(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.STRING)
    public String convertToString(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}
