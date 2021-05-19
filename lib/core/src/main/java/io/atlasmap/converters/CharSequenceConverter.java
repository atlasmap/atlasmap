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
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.v2.FieldType;

public class CharSequenceConverter implements AtlasConverter<CharSequence> {

    private static final Pattern TRUE_PATTERN = Pattern.compile("true|t|yes|y", Pattern.CASE_INSENSITIVE);

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DECIMAL,
            concerns = AtlasConversionConcern.FORMAT)
    public BigDecimal toBigDecimal(CharSequence value) throws AtlasConversionException {
        try {
            return value != null ? new BigDecimal(value.toString()) : null;
        } catch (NumberFormatException e) {
            throw new AtlasConversionException(String
                    .format("String %s cannont be converted to a BigDecimal as it is not in a valid format", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BIG_INTEGER,
            concerns = AtlasConversionConcern.FORMAT)
    public BigInteger toBigInteger(CharSequence value) throws AtlasConversionException {
        try {
            return value != null ? new BigInteger(value.toString()) : null;
        } catch (NumberFormatException e) {
            throw new AtlasConversionException(String
                    .format("String %s cannont be converted to a BigInteger as it is not in a valid format", value));
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.CONVENTION)
    public Boolean toBoolean(CharSequence value, String sourceFormat, String targetFormat) {
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
            Number n = NumberFormat.getInstance().parse(value.toString());
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

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BYTE, concerns = {
            AtlasConversionConcern.RANGE, AtlasConversionConcern.FORMAT, AtlasConversionConcern.FRACTIONAL_PART})
    public Byte toByte(CharSequence value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        try {
            return Byte.parseByte(value.toString());
        } catch (NumberFormatException nfex) {
            try {
                BigDecimal bd = new BigDecimal(value.toString());
                if (bd.compareTo(new BigDecimal(Byte.MIN_VALUE)) < 0
                        || bd.compareTo(new BigDecimal(Byte.MAX_VALUE)) > 0) {
                    throw new AtlasConversionException(String
                            .format("String %s is greater than Byte.MAX_VALUE  or less than Byte.MIN_VALUE", value));
                }
                return bd.byteValue();
            } catch (NumberFormatException nfe2) {
                throw new AtlasConversionException(String
                        .format("String %s cannont be converted to a Byte as it is not in a numerical format", value));
            }
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.RANGE)
    public Character toCharacter(CharSequence value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }
        // empty or greater than 1 char String throws Exception
        if (value.toString().isEmpty() || value.length() > 1) {
            throw new AtlasConversionException(
                    String.format("String '%s' is either empty or greater than one character long", value));
        } else if (value.charAt(0) < Character.MIN_VALUE || value.charAt(0) > Character.MAX_VALUE) {
            throw new AtlasConversionException(String
                    .format("String %s is greater than Character.MAX_VALUE  or less than Character.MIN_VALUE", value));
        }
        return value.charAt(0);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE_TIME)
    public Date toDate(CharSequence date, String sourceFormat, String targetFormat) {

        DateTimeFormatter formater = sourceFormat != null ? DateTimeFormatter.ofPattern(sourceFormat)
                : DateTimeFormatter.ISO_ZONED_DATE_TIME;
        return Date.from(ZonedDateTime.parse(date, formater).toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DOUBLE, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Double toDouble(CharSequence value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        String str = value.toString();
        double parsedDouble = 0.0d;
        try {
            parsedDouble = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }

        double absParsedDouble = Math.abs(parsedDouble);
        if (absParsedDouble == 0.0d) {
            return parsedDouble;
        }
        if (absParsedDouble < Double.MIN_VALUE || absParsedDouble > Double.MAX_VALUE) {
            throw new AtlasConversionException(
                    String.format(
                            "String %s is greater than Double.MAX_VALUE  or less than Double.MIN_VALUE",
                            str));
        }

        return parsedDouble;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.FLOAT, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Float toFloat(CharSequence value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        String str = value.toString();
        float parsedFloat = 0.0f;
        try {
            parsedFloat = Float.parseFloat(str);
        } catch (NumberFormatException nfe) {
            throw new AtlasConversionException(nfe);
        }

        float absParsedFloat = Math.abs(parsedFloat);
        if (absParsedFloat == 0.0f) {
            return parsedFloat;
        }

        if (absParsedFloat < Float.MIN_VALUE || absParsedFloat > Float.MAX_VALUE) {
            throw new AtlasConversionException(
                    String.format(
                            "String %s is greater than Float.MAX_VALUE  or less than Float.MIN_VALUE",
                            str));
        }

        return Float.valueOf(str);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.INTEGER, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART })
    public Integer toInteger(CharSequence value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        String str = value.toString();
        Integer i = null;
        try {
            i = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            try {
                BigDecimal bd = new BigDecimal(str);
                if (bd.compareTo(new BigDecimal(Integer.MIN_VALUE)) < 0
                        || bd.compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0) {
                    throw new AtlasConversionException(String
                            .format("String %s is greater than Integer.MAX_VALUE  or less than Integer.MIN_VALUE", str));
                }
                i = bd.intValue();
            } catch (NumberFormatException nfe2) {
                throw new AtlasConversionException(nfe);
            }
        }

        return i;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE)
    public LocalDate toLocalDate(CharSequence value) {
        return value != null ? LocalDate.parse(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.TIME)
    public LocalTime toLocalTime(CharSequence value) {
        return value != null ? LocalTime.parse(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(CharSequence value) {
        return value != null ? LocalDateTime.parse(value) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.LONG, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART })
    public Long toLong(CharSequence value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        String str = value.toString();
        Long l = null;
        try {
            l = Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            try {
                BigDecimal bd = new BigDecimal(str);
                if (bd.compareTo(new BigDecimal(Long.MIN_VALUE)) < 0
                        || bd.compareTo(new BigDecimal(Long.MAX_VALUE)) > 0) {
                    throw new AtlasConversionException(String
                            .format("String %s is greater than Long.MAX_VALUE  or less than Long.MIN_VALUE", value));
                }
                l = bd.longValue();
            } catch (NumberFormatException nfe2) {
                throw new AtlasConversionException(nfe);
            }
        }

        return l;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.SHORT, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART })
    public Short toShort(CharSequence value) throws AtlasConversionException {
        if (value == null) {
            return null;
        }

        String str = value.toString();
        Short shortty = null;
        try {
            shortty = Short.parseShort(str);
        } catch (NumberFormatException nfe) {
            try {
                BigDecimal bd = new BigDecimal(str);
                if (bd.compareTo(new BigDecimal(Short.MIN_VALUE)) < 0
                        || bd.compareTo(new BigDecimal(Short.MAX_VALUE)) > 0) {
                    throw new AtlasConversionException(String
                            .format("String %s is greater than Short.MAX_VALUE  or less than Short.MIN_VALUE", str));
                }
                shortty = bd.shortValue();
            } catch (NumberFormatException nfe2) {
                throw new AtlasConversionException(nfe2);
            }
        }
        return shortty;
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public CharBuffer toCharBuffer(CharSequence value, String sourceFormat, String targetFormat) {
        if (value == null) {
            return null;
        }
        return CharBuffer.wrap(value);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public CharSequence toCharSequence(CharSequence value, String sourceFormat, String targetFormat) {
        if (value == null) {
            return null;
        }
        return new String(value.toString());
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public String toString(CharSequence value, String sourceFormat, String targetFormat) {
        if (value == null) {
            return null;
        }
        return new String(value.toString());
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public StringBuffer toStringBuffer(CharSequence value, String sourceFormat, String targetFormat) {
        if (value == null) {
            return null;
        }
        return new StringBuffer(value);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public StringBuilder toStringBuilder(CharSequence value, String sourceFormat, String targetFormat) {
        if (value == null) {
            return null;
        }
        return new StringBuilder(value);
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.NUMBER, concerns = {
            AtlasConversionConcern.FORMAT })
    public Number toNumber(CharSequence value) throws AtlasConversionException {
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }
        String str = value.toString();
        if (str.matches("\\d+")) {
            return new BigInteger(str);
        }
        try {
            return new BigDecimal(str);
        } catch (NumberFormatException e) {
            throw new AtlasConversionException(e);
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(CharSequence value) {
        return value != null ? ZonedDateTime.parse(value) : null;
    }

}
