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
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class BigDecimalConverter implements AtlasConverter<BigInteger> {

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.DECIMAL)
    public BigDecimal toBigDecimal(BigDecimal value) {
        return value;
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.BIG_INTEGER)
    public BigInteger toBigInteger(BigDecimal value) {
        return value != null ? value.toBigInteger() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.BOOLEAN)
    public Boolean toBoolean(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.BYTE,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public Byte toByte(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            return value.toBigInteger().byteValueExact();
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Byte.MAX_VALUE or less than Byte.MIN_VALUE", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.CHAR,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public Character toCharacter(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            Character.valueOf((char) value.intValueExact());
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Character.MAX_VALUE or less than Character.MIN_VALUE", value));
        }
        return Character.valueOf((char) value.intValue());
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.DATE_TIME,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public Date toDate(BigDecimal date) throws AtlasConversionException {
        if (date == null) {
            return null;
        }
        try {
            long dateLong = date.toBigInteger().longValueExact();
            if (dateLong >= Instant.MIN.getEpochSecond()) {
                return Date.from(Instant.ofEpochMilli(dateLong));
            }
            return new Date(dateLong);
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", date));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.DOUBLE,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public Double toDouble(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        Double answer = value.toBigInteger().doubleValue();
        if (answer == Double.NEGATIVE_INFINITY || answer == Double.POSITIVE_INFINITY) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Double.MAX_VALUE or less than Double.MIN_VALUE", value));
        }
        return answer;
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.FLOAT,
            concerns = AtlasConversionConcern.RANGE)
    public Float toFloat(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        Float answer = value.floatValue();
        if (answer == Float.NEGATIVE_INFINITY || answer == Float.POSITIVE_INFINITY) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Float.MAX_VALUE or less than Float.MIN_VALUE", value));
        }
        return answer;
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.INTEGER,
            concerns = AtlasConversionConcern.RANGE)
    public Integer toInteger(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            return value.intValueExact();
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Integer.MAX_VALUE or less than Integer.MIN_VALUE", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.DATE,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public LocalDate toLocalDate(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            long longValue = value.toBigInteger().longValueExact();
            return Instant.ofEpochMilli(longValue).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.TIME,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public LocalTime toLocalTime(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            long longValue = value.toBigInteger().longValueExact();
            return Instant.ofEpochMilli(longValue).atZone(ZoneId.systemDefault()).toLocalTime();
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.DATE_TIME,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public LocalDateTime toLocalDateTime(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            long longValue = value.toBigInteger().longValueExact();
            return Instant.ofEpochMilli(longValue).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.LONG,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public Long toLong(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            return value.toBigInteger().longValueExact();
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.NUMBER)
    public Number toNumber(BigDecimal value) {
        return value;
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.SHORT,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public Short toShort(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            return value.toBigInteger().shortValueExact();
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(
                    String.format("BigDecimal %s is greater than Short.MAX_VALUE or less than Short.MIN_VALUE", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.STRING)
    public String toString(BigDecimal value) {
        return value != null ? value.toString() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.DECIMAL, targetType = FieldType.DATE_TIME_TZ,
            concerns = {AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART})
    public ZonedDateTime toZonedDateTime(BigDecimal value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            long longValue = value.longValueExact();
            return Instant.ofEpochMilli(longValue).atZone(ZoneId.systemDefault());
        } catch (ArithmeticException e) {
            throw new AtlasConversionException(String.format(
                    "BigDecimal %s is greater than Long.MAX_VALUE or less than Long.MIN_VALUE", value));
        }
    }

}
