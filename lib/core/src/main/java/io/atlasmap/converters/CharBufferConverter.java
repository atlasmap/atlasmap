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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.v2.FieldType;

/**
 * The type converter for {@link CharBuffer}.
 */
public class CharBufferConverter implements AtlasConverter<CharBuffer> {

    private CharSequenceConverter delegate = new CharSequenceConverter();

    /**
     * Converts to {@link BigDecimal}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DECIMAL,
            concerns = AtlasConversionConcern.FORMAT)
    public BigDecimal toBigDecimal(CharBuffer value) throws AtlasConversionException {
        return delegate.toBigDecimal(value);
    }

    /**
     * Converts to {@link BigInteger}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BIG_INTEGER,
            concerns = AtlasConversionConcern.FORMAT)
    public BigInteger toBigInteger(CharBuffer value) throws AtlasConversionException {
        return delegate.toBigInteger(value);
    }

    /**
     * Converts to {@link Boolean}.
     * @param value value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BOOLEAN, concerns = AtlasConversionConcern.CONVENTION)
    public Boolean toBoolean(CharBuffer value, String sourceFormat, String targetFormat) {
        return delegate.toBoolean(value, sourceFormat, targetFormat);
    }

    /**
     * Converts to {@link Byte}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException out of range or not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.BYTE, concerns = {
            AtlasConversionConcern.RANGE, AtlasConversionConcern.FORMAT, AtlasConversionConcern.FRACTIONAL_PART})
    public Byte toByte(CharBuffer value) throws AtlasConversionException {
        return delegate.toByte(value);
    }

    /**
     * Converts to {@link Character}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException out of range or not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.CHAR, concerns = AtlasConversionConcern.RANGE)
    public Character toCharacter(CharBuffer value) throws AtlasConversionException {
        return delegate.toCharacter(value);
    }

    /**
     * Converts to {@link Date}.
     * @param date value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE_TIME)
    public Date toDate(CharBuffer date, String sourceFormat, String targetFormat) {
        return delegate.toDate(date, sourceFormat, targetFormat);
    }

    /**
     * Converts to {@link Double}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException out of range or not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DOUBLE, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Double toDouble(CharBuffer value) throws AtlasConversionException {
        return delegate.toDouble(value);
    }

    /**
     * Converts to {@link Float}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException out of range or not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.FLOAT, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE })
    public Float toFloat(CharBuffer value) throws AtlasConversionException {
        return delegate.toFloat(value);
    }

    /**
     * Converts to {@link Integer}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException out of range or not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.INTEGER, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART })
    public Integer toInteger(CharBuffer value) throws AtlasConversionException {
        return delegate.toInteger(value);
    }

    /**
     * Converts to {@link LocalDate}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE)
    public LocalDate toLocalDate(CharBuffer value) {
        return delegate.toLocalDate(value);
    }

    /**
     * Converts to {@link LocalTime}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.TIME)
    public LocalTime toLocalTime(CharBuffer value) {
        return delegate.toLocalTime(value);
    }

    /**
     * Coverts to {@link LocalDateTime}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(CharBuffer value) {
        return delegate.toLocalDateTime(value);
    }

    /**
     * Converts to {@link Long}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException out of range or not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.LONG, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART })
    public Long toLong(CharBuffer value) throws AtlasConversionException {
        return delegate.toLong(value);
    }

    /**
     * Converts to {@link Short}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException out of range or not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.SHORT, concerns = {
            AtlasConversionConcern.FORMAT, AtlasConversionConcern.RANGE, AtlasConversionConcern.FRACTIONAL_PART })
    public Short toShort(CharBuffer value) throws AtlasConversionException {
        return delegate.toShort(value);
    }

    /**
     * Converts to {@link CharBuffer}.
     * @param value value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public CharBuffer toCharBuffer(CharBuffer value, String sourceFormat, String targetFormat) {
        return delegate.toCharBuffer(value, sourceFormat, targetFormat);
    }

    /**
     * Converts to {@link CharSequence}.
     * @param value value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public CharSequence toCharSequence(CharBuffer value, String sourceFormat, String targetFormat) {
        return delegate.toCharSequence(value, sourceFormat, targetFormat);
    }

    /**
     * Converts to {@link String}.
     * @param value value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public String toString(CharBuffer value, String sourceFormat, String targetFormat) {
        return delegate.toString(value, sourceFormat, targetFormat);
    }

    /**
     * Converts to {@link StringBuilder}.
     * @param value value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public StringBuffer toStringBuffer(CharBuffer value, String sourceFormat, String targetFormat) {
        return delegate.toStringBuffer(value, sourceFormat, targetFormat);
    }

    /**
     * Converts to {@link StringBuilder}.
     * @param value value
     * @param sourceFormat source format
     * @param targetFormat target format
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.STRING)
    public StringBuilder toStringBuilder(CharBuffer value, String sourceFormat, String targetFormat) {
        return delegate.toStringBuilder(value, sourceFormat, targetFormat);
    }

    /**
     * Converts to {@link Number}.
     * @param value value
     * @return converted
     * @throws AtlasConversionException not a number
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.NUMBER, concerns = {
            AtlasConversionConcern.FORMAT })
    public Number toNumber(CharBuffer value) throws AtlasConversionException {
        return delegate.toNumber(value);
    }

    /**
     * Converts to {@link ZonedDateTime}.
     * @param value value
     * @return converted
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(CharBuffer value) {
        return delegate.toZonedDateTime(value);
    }

}
