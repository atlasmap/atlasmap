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

public class FloatConverter implements AtlasPrimitiveConverter<Float> {

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.RANGE)
    public Boolean convertToBoolean(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value == 0.0 || value == 1.0) {
            if (value == 1) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
        throw new AtlasConversionException(String.format("Float %s could not be converted to a Boolean", value));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Byte convertToByte(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(new AtlasUnsupportedException("Float to Byte conversion is not supported"));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.RANGE)
    public Character convertToCharacter(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        if (value < Character.MIN_VALUE || value > Character.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("Float %s is greater than Character.MAX_VALUE or is less than Character.MIN_VALUE", value));
        }

        return (char) value.intValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.DOUBLE)
    public Double convertToDouble(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        if (value == 0.0f) {
            return value.doubleValue();
        }

        return value.doubleValue();
    }

    /**
     *
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.FLOAT)
    public Float convertToFloat(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // we want a copy of value
        return value.floatValue();
    }

    /**
     *
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.INTEGER, concerns = AtlasConversionConcern.RANGE)
    public Integer convertToInteger(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("Float %s is greater than Integer.MAX_VALUE or is less than Integer.MIN_VALUE", value));
        }
        return value.intValue();
    }

    /**
     *
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.LONG, concerns = AtlasConversionConcern.RANGE)
    public Long convertToLong(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
            throw new AtlasConversionException(
                    String.format("Float %s is greater than Long.MAX_VALUE or is less than Long.MIN_VALUE", value));
        }
        return value.longValue();
    }

    /**
     *
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.SHORT, concerns = AtlasConversionConcern.RANGE)
    public Short convertToShort(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Short.MAX_VALUE || value < Short.MIN_VALUE) {
            throw new AtlasConversionException(
                    String.format("Float %s is greater than Short.MAX_VALUE  or is less than Short.MIN_VALUE", value));
        }
        return value.shortValue();
    }

    /**
     *
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.STRING)
    public String convertToString(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}
