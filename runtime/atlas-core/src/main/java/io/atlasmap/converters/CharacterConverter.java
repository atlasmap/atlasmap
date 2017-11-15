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

public class CharacterConverter implements AtlasPrimitiveConverter<Character> {

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.CHAR, targetType = FieldType.BOOLEAN, concerns = {
            AtlasConversionConcern.RANGE })
    public Boolean convertToBoolean(Character value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // check for 1|0 or T|F|t|f (ASCII Integer value)
        if ((value == 49 || value == 48) || (value == 84 || value == 70) || (value == 116 || value == 102)) {
            if (value == 49 || value == 116 || value == 84) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
        // assuming anything not able to convert to a Boolean should be an exception
        // condition
        throw new AtlasConversionException(String.format("Character value %s cannot be converted to a Boolean", value));

    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.CHAR, targetType = FieldType.BYTE, concerns = {
            AtlasConversionConcern.UNSUPPORTED })
    public Byte convertToByte(Character value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        throw new AtlasConversionException(
                new AtlasUnsupportedException("Character to Byte conversion is not supported."));
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.CHAR, targetType = FieldType.CHAR)
    public Character convertToCharacter(Character value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // we want new Character from the value
        return new Character(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.CHAR, targetType = FieldType.DOUBLE)
    public Double convertToDouble(Character value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return Double.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.CHAR, targetType = FieldType.FLOAT)
    public Float convertToFloat(Character value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return Float.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.CHAR, targetType = FieldType.INTEGER)
    public Integer convertToInteger(Character value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.CHAR, targetType = FieldType.LONG)
    public Long convertToLong(Character value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return Long.valueOf(value);
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.CHAR, targetType = FieldType.SHORT, concerns = {
            AtlasConversionConcern.RANGE })
    public Short convertToShort(Character value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // only care if the char is larger than the short MAX
        if (value > Short.MAX_VALUE) {
            throw new AtlasConversionException();
        }
        return (short) value.charValue();
    }

    /**
     * @param value
     * @return
     * @throws AtlasConversionException
     */
    @Override
    @AtlasConversionInfo(sourceType = FieldType.CHAR, targetType = FieldType.STRING)
    public String convertToString(Character value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }
}
