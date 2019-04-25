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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class FloatConverter implements AtlasConverter<Float> {

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.DECIMAL)
    public BigDecimal toBigDecimal(Float value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.BIG_INTEGER,
            concerns = AtlasConversionConcern.FRACTIONAL_PART)
    public BigInteger toBigInteger(Float value) {
        return value != null ? BigDecimal.valueOf(value).toBigInteger() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.BOOLEAN, concerns = {
            AtlasConversionConcern.CONVENTION })
    public Boolean toBoolean(Float value) {
        if (value == null) {
            return null;
        }
        if (value == 0.0) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.BYTE, concerns = {
            AtlasConversionConcern.RANGE })
    public Byte toByte(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value % 1 == 0 && value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return value.byteValue();
        }
        throw new AtlasConversionException(new AtlasUnsupportedException(String.format(
                "Float %s is greater than Byte.MAX_VALUE or less than Byte.MIN_VALUE or is not a whole number",
                value)));
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.CHAR, concerns = {
            AtlasConversionConcern.RANGE, AtlasConversionConcern.CONVENTION })
    public Character toCharacter(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        if (value < Character.MIN_VALUE || value > Character.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("Float %s is greater than Character.MAX_VALUE or is less than Character.MIN_VALUE", value));
        }

        return Character.valueOf((char) value.intValue());
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.DATE,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public Date toDate(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value < Long.MIN_VALUE || value > Long.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("Float %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
        return new Date(value.longValue());
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.DOUBLE)
    public Double toDouble(Float value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.FLOAT)
    public Float toFloat(Float value) {
        if (value == null) {
            return null;
        }
        // we want a copy of value
        return value.floatValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.INTEGER, concerns = AtlasConversionConcern.RANGE)
    public Integer toInteger(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("Float %s is greater than Integer.MAX_VALUE or is less than Integer.MIN_VALUE", value));
        }
        return value.intValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.DATE,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public LocalDate toLocalDate(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value < Long.MIN_VALUE || value > Long.MAX_VALUE) {
            throw new AtlasConversionException(String.format(
                    "Float %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
        return Instant.ofEpochMilli(value.longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.TIME,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public LocalTime toLocalTime(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value < Long.MIN_VALUE || value > Long.MAX_VALUE) {
            throw new AtlasConversionException(String.format(
                    "Float %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
        return Instant.ofEpochMilli(value.longValue()).atZone(ZoneId.systemDefault()).toLocalTime();
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.DATE_TIME,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public LocalDateTime toLocalDateTime(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value < Long.MIN_VALUE || value > Long.MAX_VALUE) {
            throw new AtlasConversionException(String.format(
                    "Float %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
        return Instant.ofEpochMilli(value.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.LONG,
            concerns = AtlasConversionConcern.RANGE)
    public Long toLong(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
            throw new AtlasConversionException(
                    String.format("Float %s is greater than Long.MAX_VALUE or is less than Long.MIN_VALUE", value));
        }
        return value.longValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.NUMBER)
    public Number toNumber(Float value) {
        return value;
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.SHORT, concerns = AtlasConversionConcern.RANGE)
    public Short toShort(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Short.MAX_VALUE || value < Short.MIN_VALUE) {
            throw new AtlasConversionException(
                    String.format("Float %s is greater than Short.MAX_VALUE  or is less than Short.MIN_VALUE", value));
        }
        return value.shortValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.STRING)
    public String toString(Float value) {
        return value != null ? String.valueOf(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.FLOAT, targetType = FieldType.DATE_TIME_TZ,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public ZonedDateTime toZonedDateTime(Float value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Long.MAX_VALUE || value < Long.MIN_VALUE) {
            throw new AtlasConversionException(String.format(
                    "Float %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
        return Instant.ofEpochMilli(value.longValue()).atZone(ZoneId.systemDefault());
    }

}
