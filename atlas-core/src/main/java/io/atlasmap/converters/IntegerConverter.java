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

public class IntegerConverter implements AtlasPrimitiveConverter<Integer> {

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.INTEGER, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.RANGE)
    public Boolean convertToBoolean(Integer value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // 1 == True, 0 == False
        if (value == 0 || value == 1) {
            return value == 1;
        } else {
            // any other value
            throw new AtlasConversionException(String.format("Integer %s cannot be converted to a Boolean", value));
        }
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.INTEGER, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Byte convertToByte(Integer value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(new AtlasUnsupportedException("Integer to Byte conversion is not supported"));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.INTEGER, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.RANGE)
    public Character convertToCharacter(Integer value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value < Character.MIN_VALUE || value > Character.MAX_VALUE) {
            throw new AtlasConversionException(String.format("Integer %s is greater than Character.MAX_VALUE or less than Character.MIN_VALUE", value));
        }
        
        final int radix = 10;
        return Character.forDigit(value.intValue(), radix);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.INTEGER, targetType = FieldType.DOUBLE)
    public Double convertToDouble(Integer value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value.doubleValue() == 0.0d || value.doubleValue() == -0.0d) {
            return value.doubleValue();
        }
        return value.doubleValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.INTEGER, targetType = FieldType.FLOAT, concerns = AtlasConversionConcern.RANGE)
    public Float convertToFloat(Integer value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        if ((value.floatValue() == 0.0f) || (value.floatValue() == -0.0f)) {
            return value.floatValue();
        }
        return value.floatValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.INTEGER, targetType = FieldType.INTEGER)
    public Integer convertToInteger(Integer value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        //we want a copy of value
        return Integer.valueOf(value);
    }

    @Override
	@AtlasConversionInfo(sourceType = FieldType.INTEGER, targetType = FieldType.LONG)
    public Long convertToLong(Integer value) throws AtlasConversionException {
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
	@AtlasConversionInfo(sourceType = FieldType.INTEGER, targetType = FieldType.SHORT, concerns = AtlasConversionConcern.RANGE)
    public Short convertToShort(Integer value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Short.MAX_VALUE || value < Short.MIN_VALUE) {
            throw new AtlasConversionException(String.format("Integer %s is greater than Short.MAX_VALUE or less than Short.MIN_VALUE", value));
        }
        return value.shortValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
	@AtlasConversionInfo(sourceType = FieldType.INTEGER, targetType = FieldType.STRING)
    public String convertToString(Integer value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}
