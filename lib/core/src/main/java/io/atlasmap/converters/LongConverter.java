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

public class LongConverter implements AtlasConverter<Long> {

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.DECIMAL)
    public BigDecimal toBigDecimal(Long value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.BIG_INTEGER)
    public BigInteger toBigInteger(Long value) {
        return value != null ? BigInteger.valueOf(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.BOOLEAN,
            concerns = AtlasConversionConcern.CONVENTION)
    public Boolean toBoolean(Long value) {
        if (value == null) {
            return null;
        }
        return value == 0L ? Boolean.FALSE : Boolean.TRUE;
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.BYTE,
            concerns = AtlasConversionConcern.RANGE)
    public Byte toByte(Long value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return value.byteValue();
        }
        throw new AtlasConversionException(new AtlasUnsupportedException(
                String.format("Long %s is greater than Byte.MAX_VALUE or less than Byte.MIN_VALUE", value)));
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.CHAR, concerns = {
            AtlasConversionConcern.RANGE, AtlasConversionConcern.CONVENTION })
    public Character toCharacter(Long value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        if (value < Character.MIN_VALUE || value > Character.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("Long %s is greater than Character.MAX_VALUE  or less than Character.MIN_VALUE", value));
        }

        return Character.valueOf((char) value.intValue());
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.DATE_TIME_TZ)
    public Date toDate(Long date) {
        if (date >= Instant.MIN.getEpochSecond()) {
            return Date.from(Instant.ofEpochMilli(date));
        }
        return new Date(date);
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.DOUBLE)
    public Double toDouble(Long value) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.FLOAT)
    public Float toFloat(Long value) {
        if (value == null) {
            return null;
        }
        return value.floatValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.INTEGER, concerns = AtlasConversionConcern.RANGE)
    public Integer toInteger(Long value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new AtlasConversionException(
                    String.format("Long %s is greater than Integer.MAX_VALUE  or less than Integer.MIN_VALUE", value));
        }
        return value.intValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.DATE)
    public LocalDate toLocalDate(Long value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.TIME)
    public LocalTime toLocalTime(Long value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalTime() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(Long value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.LONG)
    public Long toLong(Long value) {
        return value != null ? new Long(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.SHORT, concerns = AtlasConversionConcern.RANGE)
    public Short toShort(Long value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        if (value > Short.MAX_VALUE || value < Short.MIN_VALUE) {
            throw new AtlasConversionException(
                    String.format("Long %s is greater than Short.MAX_VALUE or less than Short.MIN_VALUE", value));
        }
        return value.shortValue();
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.NUMBER)
    public Number toNumber(Long value) {
        return value;
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.STRING)
    public String toString(Long value) {
        return value != null ? String.valueOf(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(Long value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()) : null;
    }

}
