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

public class DoubleConverter implements AtlasPrimitiveConverter<Double> {

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.DOUBLE, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.RANGE)
    public Boolean convertToBoolean(Double value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value == 0.0 || value == 1.0) {
            if (value == 1.0) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        }
        throw new AtlasConversionException(String.format("Double %s cannot be converted to a Boolean", value));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.DOUBLE, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Byte convertToByte(Double value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(new AtlasUnsupportedException("Double to Byte conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.DOUBLE, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.RANGE)
    public Character convertToCharacter(Double value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        if (value < Character.MIN_VALUE || value > Character.MAX_VALUE) {
            throw new AtlasConversionException(String.format("Double %s is greater than Character.MAX_VALUE or less than Character.MIN_VALUE", value));
        }

        return (char) value.doubleValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.DOUBLE, targetType = FieldType.DOUBLE)
    public Double convertToDouble(Double value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        //we want a copy of the value.
        return Double.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.DOUBLE, targetType = FieldType.FLOAT, concerns = AtlasConversionConcern.RANGE)
    public Float convertToFloat(Double value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Float.MAX_VALUE || (value < Float.MIN_VALUE && value != 0)) {
            throw new AtlasConversionException(String.format("Double %s is greater than Float.MAX_VALUE or less than Float.MIN_VALUE", value));
        }
        return value.floatValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.DOUBLE, targetType = FieldType.INTEGER, concerns = AtlasConversionConcern.RANGE)
    public Integer convertToInteger(Double value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Integer.MAX_VALUE) {
            throw new AtlasConversionException(String.format("Double %s is greater than Integer.MAX_VALUE", value));
        }
        return value.intValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.DOUBLE, targetType = FieldType.LONG, concerns = AtlasConversionConcern.RANGE)
    public Long convertToLong(Double value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Long.MAX_VALUE) {
            throw new AtlasConversionException(String.format("Double %s is greater than Long.MAX_VALUE", value));
        }
        return value.longValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.DOUBLE, targetType = FieldType.SHORT, concerns = AtlasConversionConcern.RANGE)
    public Short convertToShort(Double value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Short.MAX_VALUE || value < Short.MIN_VALUE) {
            throw new AtlasConversionException(String.format("Double %s is greater than Short.MAX_VALUE or less than Short.MIN_VALUE", value));
        }
        return value.shortValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.DOUBLE, targetType = FieldType.BOOLEAN)
    public String convertToString(Double value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}
