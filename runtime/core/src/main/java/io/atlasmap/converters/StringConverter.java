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
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Pattern;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class StringConverter implements AtlasConverter<String> {

    private static final Pattern TRUE_PATTERN = Pattern.compile("true|t|yes|y", Pattern.CASE_INSENSITIVE);

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DECIMAL,
            concerns = AtlasConversionConcern.FORMAT)
    public BigDecimal toBigDecimal(String value) throws AtlasConversionException {
        try {
            return value != null ? new BigDecimal(value) : null;
        } catch (NumberFormatException e) {
            throw new AtlasConversionException(String
                    .format("String %s cannont be converted to a BigDecimal as it is not in a valid format", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BIG_INTEGER,
            concerns = AtlasConversionConcern.FORMAT)
    public BigInteger toBigInteger(String value) throws AtlasConversionException {
        try {
            return value != null ? new BigInteger(value) : null;
        } catch (NumberFormatException e) {
            throw new AtlasConversionException(String
                    .format("String %s cannont be converted to a BigInteger as it is not in a valid format", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.CONVENTION)
    public Boolean toBoolean(String value, String sourceFormat, String targetFormat) {
        if (value == null) {
            return null;
        }

        // string expression of true?
        Pattern pattern;
        if (sourceFormat != null && !sourceFormat.isEmpty()) {
            pattern = Pattern.compile(sourceFormat, Pattern.CASE_INSENSITIVE);
        } else {
            pattern = TRUE_PATTERN;
        }
        if (pattern.matcher(value).matches()) {
            return Boolean.TRUE;
        }

        // then try C like numeric translation
        try {
            Number n = NumberFormat.getInstance().parse(value);
            if (n.intValue() == 0) {
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } catch (ParseException e) {
            e.getMessage(); // ignore
        }

        // false by default
        return Boolean.FALSE;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BYTE, concerns = AtlasConversionConcern.RANGE)
    public Byte toByte(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException nfex) {
            throw new AtlasConversionException(String
                    .format("String %s cannont be converted to a Byte as it is not in a numerical format", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.RANGE)
    public Character toCharacter(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // empty or greater than 1 char String throws Exception
        if (value.isEmpty() || value.length() > 1) {
            throw new AtlasConversionException(
                    String.format("String '%s' is either empty or greater than one character long", value));
        } else if (value.charAt(0) < Character.MIN_VALUE || value.charAt(0) > Character.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("String %s is greater than Character.MAX_VALUE  or less than Character.MIN_VALUE", value));
        }
        return value.charAt(0);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE_TIME)
    public Date toDate(String date, String sourceFormat, String targetFormat) {

        DateTimeFormatter formater = sourceFormat != null ? DateTimeFormatter.ofPattern(sourceFormat)
                : DateTimeFormatter.ISO_ZONED_DATE_TIME;
        return Date.from(ZonedDateTime.parse(date, formater).toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DOUBLE, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Double toDouble(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        try {
            Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }

        if (Double.valueOf(value) == 0.0d || Double.valueOf(value) == -0.0d) {
            return Double.valueOf(value);
        }
        if (Double.valueOf(value) < Double.MIN_VALUE || Double.valueOf(value) > Double.MAX_VALUE) {
            throw new AtlasConversionException(
                    String.format("String %s is greater than Double.MAX_VALUE  or less than Double.MIN_VALUE", value));
        }

        return Double.valueOf(value);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.FLOAT, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Float toFloat(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // check we can make a float of the String
        try {
            Float.parseFloat(value);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }

        BigDecimal bd = new BigDecimal(value);

        // handle 0.0f && -0.0 (floats suck)
        if (bd.floatValue() == 0.0f || bd.floatValue() == -0.0) {
            return Float.valueOf(value);
        }

        if (bd.floatValue() < Float.MIN_VALUE || bd.floatValue() > Float.MAX_VALUE) {
            throw new AtlasConversionException(
                    String.format("String %s is greater than Float.MAX_VALUE  or less than Float.MIN_VALUE", value));
        }

        return Float.valueOf(value);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.INTEGER, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Integer toInteger(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        BigDecimal bd = null;
        Integer i = null;
        try {
            i = Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            try {
                bd = new BigDecimal(value);
                i = bd.intValue();
            } catch (NumberFormatException nfe2) {
                throw new AtlasConversionException(nfe);
            }
        }

        if (bd != null && bd.compareTo(BigDecimal.valueOf(i)) != 0) {
            throw new AtlasConversionException(String
                    .format("String %s is greater than Integer.MAX_VALUE  or less than Integer.MIN_VALUE", value));
        }

        return i;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE)
    public LocalDate toLocalDate(String value) {
        return value != null ? LocalDate.parse(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.TIME)
    public LocalTime toLocalTime(String value) {
        return value != null ? LocalTime.parse(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(String value) {
        return value != null ? LocalDateTime.parse(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.LONG, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Long toLong(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        BigDecimal bd = null;
        Long l = null;
        try {
            l = Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            try {
                bd = new BigDecimal(value);
                l = bd.longValue();
            } catch (NumberFormatException nfe2) {
                throw new AtlasConversionException(nfe);
            }
        }

        if (bd != null && bd.compareTo(BigDecimal.valueOf(l)) != 0) {
            throw new AtlasConversionException(
                    String.format("String %s is greater than Long.MAX_VALUE  or less than Long.MIN_VALUE", value));
        }

        return l;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.SHORT, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Short toShort(String value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // check we can make a short of the String
        Short shortty;
        try {
            shortty = Short.parseShort(value);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }
        return Short.valueOf(shortty);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public String toString(String value, String sourceFormat, String targetFormat) {
        if (value == null) {
            return null;
        }
        // we want a copy of value
        return new String(value);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.NUMBER, concerns = {
            AtlasConversionConcern.FORMAT })
    public Number toNumber(String value) throws AtlasConversionException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if (value.matches("\\d+")) {
            return new BigInteger(value);
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new AtlasConversionException(e);
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(String value) {
        return value != null ? ZonedDateTime.parse(value) : null;
    }

}
