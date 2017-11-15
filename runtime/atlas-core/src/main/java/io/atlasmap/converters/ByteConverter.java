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

import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

public class ByteConverter implements AtlasPrimitiveConverter<Byte> {

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Boolean convertToBoolean(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(
                new AtlasUnsupportedException("Byte to Boolean conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Byte convertToByte(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(new AtlasUnsupportedException("Byte to Byte conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Character convertToCharacter(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(
                new AtlasUnsupportedException("Byte to Character conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.DOUBLE, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Double convertToDouble(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(
                new AtlasUnsupportedException("Byte to Double conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.FLOAT, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Float convertToFloat(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(new AtlasUnsupportedException("Byte to Float conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.INTEGER, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Integer convertToInteger(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(
                new AtlasUnsupportedException("Byte to Integer conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.LONG, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Long convertToLong(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(new AtlasUnsupportedException("Byte to Long conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.SHORT, concerns = AtlasConversionConcern.UNSUPPORTED)
    public Short convertToShort(Byte value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(new AtlasUnsupportedException("Byte to Short conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     * @throws AtlasUnsupportedException
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
