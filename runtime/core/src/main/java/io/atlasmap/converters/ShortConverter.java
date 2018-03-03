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
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.api.AtlasUnsupportedException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class ShortConverter implements AtlasConverter<Short> {

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.DECIMAL)
    public BigDecimal toBigDecimal(Short value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.BIG_INTEGER)
    public BigInteger toBigInteger(Short value) {
        return value != null ? BigInteger.valueOf(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.CONVENTION)
    public Boolean toBoolean(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value == 0) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.RANGE)
    public Byte toByte(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return value.byteValue();
        } else {
            throw new AtlasConversionException(new AtlasUnsupportedException(
                    String.format("Short %s is greater than Byte.MAX_VALUE or less than Byte.MIN_VALUE", value)));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.CHAR, concerns = {
            AtlasConversionConcern.RANGE, AtlasConversionConcern.CONVENTION })
    public Character toCharacter(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        if (value < Character.MIN_VALUE || value > Character.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("Short %s is greater than Character.MAX_VALUE  or less than Character.MIN_VALUE", value));
        }
        return Character.valueOf((char) value.intValue());
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.DATE_TIME)
    public Date toDate(Short value) throws AtlasConversionException {
        if (value >= Instant.MIN.getEpochSecond()) {
            return Date.from(Instant.ofEpochMilli(value));
        } else {
            return new Date(value);
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.DOUBLE)
    public Double toDouble(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.FLOAT)
    public Float toFloat(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value.floatValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.INTEGER)
    public Integer toInteger(Short value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.DATE)
    public LocalDate toLocalDate(Short value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.TIME)
    public LocalTime toLocalTime(Short value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalTime() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(Short value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.LONG)
    public Long toLong(Short value) throws AtlasConversionException {
        return value != null ? value.longValue() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.NUMBER)
    public Number toNumber(Short value) throws AtlasConversionException {
        return value;
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.SHORT)
    public Short toShort(Short value) throws AtlasConversionException {
        return value != null ? new Short(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.STRING)
    public String toString(Short value) throws AtlasConversionException {
        return value != null ? String.valueOf(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.SHORT, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(Short value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()) : null;
    }

}
