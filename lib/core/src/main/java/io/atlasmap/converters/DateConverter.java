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
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.v2.FieldType;

/**
 * The type converter for {@link Date}.
 */
public class DateConverter implements AtlasConverter<Date> {

    /**
     * Converts to {@link BigDecimal}.
     * @param date value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.DECIMAL)
    public BigDecimal toBigDecimal(Date date) {
        return BigDecimal.valueOf(date.getTime());
    }

    /**
     * Converts to {@link BigInteger}.
     * @param date value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.BIG_INTEGER)
    public BigInteger toBigInteger(Date date) {
        return BigInteger.valueOf(date.getTime());
    }

    /**
     * Converts to {@link Byte}.
     * @param value value
     * @return convewrted
     * @throws AtlasConversionException out of range
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.BYTE,
            concerns = AtlasConversionConcern.RANGE)
    public Byte toByte(Date value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        Long longValue = value.getTime();
        if (longValue < Byte.MIN_VALUE || longValue > Byte.MAX_VALUE) {
            throw new AtlasConversionException(
                    String.format("LocalDateTime %s is greater than Byte.MAX_VALUE or less than Byte.MIN_VALUE", value));
        }
        return longValue.byteValue();
    }

    /**
     * Converts to {@link Calendar}.
     * @param date value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.DATE_TIME_TZ)
    public Calendar toCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        return calendar;
    }

    /**
     * Converts to {@link Double}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.DOUBLE)
    public Double toDouble(Date value) {
        return value != null ? Double.valueOf(value.getTime()) : null;
    }

    /**
     * Converts to {@link Float}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.FLOAT)
    public Float toFloat(Date value) {
        return value != null ? Float.valueOf(value.getTime()) : null;
    }

    /**
     * Converts to {@link Integer}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException out of range
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.INTEGER,
            concerns = AtlasConversionConcern.RANGE)
    public Integer toInteger(Date value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        Long longValue = value.getTime();
        if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
            throw new AtlasConversionException(String
                    .format("Date %s is greater than Integer.MAX_VALUE or less than Integer.MIN_VALUE", value));
        }
        return longValue.intValue();
    }

    /**
     * Converts to {@link GregorianCalendar}.
     * @param date value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.DATE_TIME_TZ)
    public GregorianCalendar toGregorianCalendar(Date date, String sourceFormat, String targetFormat) {
        return DateTimeHelper.convertDateToGregorianCalendar(date, sourceFormat);
    }

    /**
     * COnverts to {@link LocalDate}.
     * @param date value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.DATE)
    public LocalDate toLocalDate(Date date, String sourceFormat, String targetFormat) {
        return DateTimeHelper.convertDateToLocalDate(date, targetFormat);
    }

    /**
     * Converts to {@link LocalDateTime}.
     * @param date value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(Date date, String sourceFormat, String targetFormat) {
        return DateTimeHelper.convertDateToLocalDateTime(date, targetFormat);
    }

    /**
     * Converts to {@link LocalTime}.
     * @param date value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.TIME)
    public LocalTime toLocalTime(Date date, String sourceFormat, String targetFormat) {
        return DateTimeHelper.convertDateToLocalTime(date, targetFormat);
    }

    /**
     * Converts to {@link Long}.
     * @param date value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.LONG)
    public Long toLong(Date date) {
        return date.toInstant().toEpochMilli();
    }

    /**
     * Converts to {@link Number}.
     * @param date value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.NUMBER)
    public Number toNumber(Date date) {
        return date.toInstant().toEpochMilli();
    }

    /**
     * Converts to {@link Short}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException out of range
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.SHORT,
            concerns = AtlasConversionConcern.RANGE)
    public Short toShort(Date value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        Long longValue = value.getTime();
        if (longValue > Short.MAX_VALUE || longValue < Short.MIN_VALUE) {
            throw new AtlasConversionException(String
                    .format("Date %s is greater than Short.MAX_VALUE or less than Short.MIN_VALUE", value));
        }
        return longValue.shortValue();
    }

    /**
     * Converts to {@link java.sql.Date}.
     * @param date value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.DATE)
    public java.sql.Date toSqlDate(Date date, String sourceFormat, String targetFormat) {
        return DateTimeHelper.convertDateToSqlDate(date, sourceFormat);
    }

    /**
     * Converts to {@link Time}.
     * @param date value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.TIME)
    public Time toSqlTime(Date date, String sourceFormat, String targetFormat) {
        return DateTimeHelper.convertDateToTime(date, targetFormat);
    }

    /**
     * Converts to {@link String}.
     * @param date value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.STRING)
    public String toString(Date date) {
        // by default Instant.toString returns an ISO-8601 representation of the instant
        return date.toInstant().toString();

    }

    /**
     * Converts to {@link CharBuffer}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.STRING)
    public CharBuffer toCharBuffer(Date value) {
        return value != null ? CharBuffer.wrap(toString(value)) : null;
    }

    /**
     * Converts to {@link CharSequence}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.STRING)
    public CharSequence toCharSequence(Date value) {
        return value != null ? toString(value) : null;
    }

    /**
     * Converts to {@link StringBuilder}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.STRING)
    public StringBuffer toStringBuffer(Date value) {
        return value != null ? new StringBuffer(toString(value)) : null;
    }

    /**
     * Converts to {@link StringBuilder}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.STRING)
    public StringBuilder toStringBuilder(Date value) {
        return value != null ? new StringBuilder(toString(value)) : null;
    }

    /**
     * Converts to {@link Timestamp}.
     * @param date value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.DATE_TIME)
    public Timestamp toSqlTimestamp(Date date) {
        return Timestamp.from(date.toInstant());
    }

    /**
     * Converts to {@link ZonedDateTime}.
     * @param date value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(Date date) {
        return DateTimeHelper.toZonedDateTime(date, null);
    }
}
