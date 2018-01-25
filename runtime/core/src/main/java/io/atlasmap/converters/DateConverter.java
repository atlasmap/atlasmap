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

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class DateConverter implements AtlasConverter<Date> {

    protected Time convertToTime(Date date, String timeZone) throws AtlasConversionException {
        ZoneId zoneId = timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
        return Time.valueOf(LocalDateTime.ofInstant(date.toInstant(), zoneId).toLocalTime());
    }

    protected LocalDateTime convertToLocalDateTime(Date date, String timeZone) throws AtlasConversionException {
        ZoneId zoneId = timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
        return LocalDateTime.ofInstant(date.toInstant(), zoneId);
    }

    protected ZonedDateTime convertToZonedDateTime(Date date, String timeZone) throws AtlasConversionException {
        ZoneId zoneId = timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
        return ZonedDateTime.ofInstant(date.toInstant(), zoneId);
    }

    protected Date convertFromLocalDateTime(LocalDateTime localDateTime, String timeZone)
            throws AtlasConversionException {
        ZoneId zoneId = timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

    protected LocalTime convertToLocalTime(Date date, String timeZone) throws AtlasConversionException {
        ZoneId zoneId = timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
        return LocalDateTime.ofInstant(date.toInstant(), zoneId).toLocalTime();
    }

    protected Date convertFromLocalTime(LocalTime localTime, String timeZone) throws AtlasConversionException {
        ZoneId zoneId = timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
        return Date.from(localTime.atDate(LocalDate.now()).atZone(zoneId).toInstant());
    }

    protected LocalDate convertToLocalDate(Date date, String timeZone) throws AtlasConversionException {
        ZoneId zoneId = timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
        return LocalDateTime.ofInstant(date.toInstant(), zoneId).toLocalDate();
    }

    protected Date convertFromTime(Time time, String timeZone) throws AtlasConversionException {
        return convertFromLocalTime(time.toLocalTime(), timeZone); // ?
    }

    protected java.sql.Date convertToSqlDate(Date date, String timeZone) throws AtlasConversionException {
        return java.sql.Date.valueOf(convertToLocalDate(date, timeZone));
    }

    protected Date convertFromSqlDate(java.sql.Date date, String timeZone) throws AtlasConversionException {
        ZoneId zoneId = timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
        return Date.from(date.toLocalDate().atStartOfDay(zoneId).toInstant());
    }

    protected GregorianCalendar convertToGregorianCalendar(Date date, String timeZone) throws AtlasConversionException {
        return GregorianCalendar.from(convertToZonedDateTime(date, timeZone));
    }

    // java.util.Date Converters

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public Time convertDateToTime(Date date, String sourceFormat, String targetFormat) throws AtlasConversionException {
        return convertToTime(date, targetFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE_TIME)
    public LocalDateTime convertDateToDateTime(Date date, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        return convertToLocalDateTime(date, targetFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime convertDateToZonedDateTime(Date date, String sourceFromat, String targetFormat)
            throws AtlasConversionException {
        return convertToZonedDateTime(date, targetFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.TIME)
    public LocalTime convertToLocalTime(Date date, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        return convertToLocalTime(date, targetFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE)
    public LocalDate convertToLocalDate(Date date, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        return convertToLocalDate(date, targetFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public Timestamp convertToTimestamp(Date date) throws AtlasConversionException {
        return Timestamp.from(date.toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public GregorianCalendar convertToGregorianCalendar(Date date, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        return convertToGregorianCalendar(date, sourceFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public java.sql.Date convertToSqlDate(Date date, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        return convertToSqlDate(date, sourceFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public Calendar convertToCalendar(Date date) throws AtlasConversionException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        return calendar;
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.STRING)
    public String convertToString(Date date) throws AtlasConversionException {
        // by default Instant.toString returns an ISO-8601 representation of the instant
        return date.toInstant().toString();

    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.LONG)
    public Long convertToLong(Date date) throws AtlasConversionException {
        return date.toInstant().toEpochMilli();
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public Date convertFromTimestamp(Timestamp timestamp) throws AtlasConversionException {
        return Date.from(timestamp.toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public Date convertFromTime(Time time, String sourceFormat, String targetFormat) throws AtlasConversionException {
        return convertFromLocalTime(time.toLocalTime(), sourceFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public Date convertFromSqlDate(java.sql.Date date, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        return convertFromSqlDate(date, sourceFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public Date convertFromGregorianCalendar(GregorianCalendar gregorianCalendar) throws AtlasConversionException {
        return convertFromZonedDateTime(gregorianCalendar.toZonedDateTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.COMPLEX)
    public Date convertFromCalendar(Calendar calendar) throws AtlasConversionException {
        return calendar.getTime();
    }

    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.COMPLEX)
    public Date convertFromString(String date, String sourceFormat, String targetFormat)
            throws AtlasConversionException {

        DateTimeFormatter formater = sourceFormat != null ? DateTimeFormatter.ofPattern(sourceFormat)
                : DateTimeFormatter.ISO_ZONED_DATE_TIME;
        return Date.from(ZonedDateTime.parse(date, formater).toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.COMPLEX)
    public Date convertFromLong(Long date) throws AtlasConversionException {
        if (date >= Instant.MIN.getEpochSecond()) {
            return Date.from(Instant.ofEpochMilli(date));
        } else {
            return new Date(date);
        }
    }

    @AtlasConversionInfo(sourceType = FieldType.TIME, targetType = FieldType.COMPLEX)
    public Date convertFromLocalTime(LocalTime localTime, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        return convertFromLocalTime(localTime, sourceFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX)
    public Date convertFromLocalDate(LocalDate localDate, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        ZoneId zoneId = sourceFormat != null ? ZoneId.of(sourceFormat) : ZoneId.systemDefault();
        return Date.from(localDate.atStartOfDay().atZone(zoneId).toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME, targetType = FieldType.COMPLEX)
    public Date convertFromLocalDateTime(LocalDateTime localDateTime, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        return convertFromLocalDateTime(localDateTime, sourceFormat);
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.COMPLEX)
    public Date convertFromZonedDateTime(ZonedDateTime dateTime) throws AtlasConversionException {
        return Date.from(dateTime.toInstant());
    }

}
