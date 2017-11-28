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
package io.atlasmap.converters;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

public class ByteConverter implements AtlasPrimitiveConverter<Byte> {

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.BOOLEAN)
    public Boolean convertToBoolean(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value.byteValue() != 0;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.BYTE)
    public Byte convertToByte(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return new Byte(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.CHAR)
    public Character convertToCharacter(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return (char) value.byteValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.DOUBLE)
    public Double convertToDouble(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return (double) value;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.FLOAT)
    public Float convertToFloat(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return (float) value;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.INTEGER)
    public Integer convertToInteger(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return (int) value;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.LONG)
    public Long convertToLong(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return (long) value;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.SHORT)
    public Short convertToShort(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return (short) value;
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.STRING)
    public String convertToString(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.NUMBER)
    public Number convertToNumber(Byte value) throws AtlasConversionException {
        return convertToShort(value);
    }
}
