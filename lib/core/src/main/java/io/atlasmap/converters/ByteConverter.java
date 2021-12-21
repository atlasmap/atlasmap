/*
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.v2.FieldType;

/**
 * The type converters for {@link Byte}.
 */
public class ByteConverter implements AtlasConverter<Byte> {

    /**
     * Converts to {@link BigDecimal}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.DECIMAL)
    public BigDecimal toBigDecimal(Byte value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    /**
     * Converts to {@link BigInteger}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.BIG_INTEGER)
    public BigInteger toBigInteger(Byte value) {
        return value != null ? BigInteger.valueOf(value) : null;
    }

    /**
     * Converts to {@link Boolean}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.BOOLEAN,
            concerns = {AtlasConversionConcern.CONVENTION})
    public Boolean toBoolean(Byte value) {
        if (value == null) {
            return null;
        }
        return value.byteValue() != 0;
    }

    /**
     * Converts to {@link Byte}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.BYTE)
    public Byte toByte(Byte value) {
        return value != null ? new Byte(value) : null;
    }

    /**
     * Converts to {@link Character}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.CHAR)
    public Character toCharacter(Byte value) {
        return value != null ? (char) value.byteValue() : null;
    }

    /**
     * Converts to {@link Date}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.DATE_TIME)
    public Date toDate(Byte value) {
        if (value == null) {
            return null;
        }
        if (value >= Instant.MIN.getEpochSecond()) {
            return Date.from(Instant.ofEpochMilli(value));
        }
        return new Date(value);
    }

    /**
     * Converts to {@link Double}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.DOUBLE)
    public Double toDouble(Byte value) {
        return value != null ? (double) value : null;
    }

    /**
     * Converts to {@link Float}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.FLOAT)
    public Float toFloat(Byte value) {
        return value != null ? (float) value : null;
    }

    /**
     * Converts to {@link Integer}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.INTEGER)
    public Integer toInteger(Byte value) {
        return value != null ? (int) value : null;
    }

    /**
     * Converts to {@link LocalDate}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.DATE)
    public LocalDate toLocalDate(Byte value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    /**
     * Converts to {@link LocalTime}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.TIME)
    public LocalTime toLocalTime(Byte value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalTime() : null;
    }

    /**
     * Converts to {@link LocalDateTime}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(Byte value) {
        return value != null ? Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    /**
     * Converts to {@link Long}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.LONG)
    public Long toLong(Byte value) {
        return value != null ? (long) value : null;
    }

    /**
     * Converts to {@link Number}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.NUMBER)
    public Number toNumber(Byte value) {
        return toShort(value);
    }

    /**
     * Converts to {@link Short}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.SHORT)
    public Short toShort(Byte value) {
        return value != null ? (short) value : null;
    }

    /**
     * Converts to {@link String}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.STRING,
            concerns = {AtlasConversionConcern.CONVENTION})
    public String toString(Byte value) {
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * Converts to {@link CharBuffer}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.STRING)
    public CharBuffer toCharBuffer(Byte value) {
        return value != null ? CharBuffer.wrap(toString(value)) : null;
    }

    /**
     * Converts to {@link CharSequence}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.STRING)
    public CharSequence toCharSequence(Byte value) {
        return value != null ? toString(value) : null;
    }

    /**
     * Converts to {@link StringBuffer}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.STRING)
    public StringBuffer toStringBuffer(Byte value) {
        return value != null ? new StringBuffer(toString(value)) : null;
    }

    /**
     * Converts to {@link StringBuilder}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.STRING)
    public StringBuilder toStringBuilder(Byte value) {
        return value != null ? new StringBuilder(toString(value)) : null;
    }

    /**
     * Converts to {@link ZonedDateTime}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.BYTE, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(Byte value) {
        return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault());
    }

}
