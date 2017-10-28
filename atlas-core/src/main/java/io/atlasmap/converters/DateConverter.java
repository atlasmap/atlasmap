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

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

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

public class DateConverter implements AtlasConverter<Date> {
    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX, sourceClassName = "java.util.Date", targetClassName = "java.time.ZonedDateTime")
    public ZonedDateTime convertToZonedDateTime(Date date) throws AtlasConversionException {
        return convertToZonedDateTime(date, ZoneId.systemDefault());
    }

    public ZonedDateTime convertToZonedDateTime(Date date, ZoneId zoneId) throws AtlasConversionException {
        return ZonedDateTime.ofInstant(date.toInstant(), zoneId);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE, sourceClassName = "java.time.ZonedDateTime", targetClassName = "java.util.Date")
    public Date convertFromZonedDateTime(ZonedDateTime dateTime) throws AtlasConversionException {
        return Date.from(dateTime.toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX, sourceClassName = "java.util.Date", targetClassName = "java.time.LocalDateTime")
    public LocalDateTime convertToLocalDateTime(Date date) throws AtlasConversionException {
        return convertToLocalDateTime(date, ZoneId.systemDefault());
    }

    public LocalDateTime convertToLocalDateTime(Date date, ZoneId zoneId) throws AtlasConversionException {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId);
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE, targetClassName = "java.util.Date", sourceClassName = "java.time.LocalDateTime")
    public Date convertFromLocalDateTime(LocalDateTime localDateTime) throws AtlasConversionException {
        return convertFromLocalDateTime(localDateTime, ZoneId.systemDefault());
    }

    public Date convertFromLocalDateTime(LocalDateTime localDateTime, ZoneId zoneId) throws AtlasConversionException {
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX, sourceClassName = "java.util.Date", targetClassName = "java.time.LocalTime")
    public LocalTime convertToLocalTime(Date date) throws AtlasConversionException {
        return convertToLocalTime(date, ZoneId.systemDefault());
    }

    public LocalTime convertToLocalTime(Date date, ZoneId zoneId) throws AtlasConversionException {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId).toLocalTime();
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE, sourceClassName = "java.time.LocalTime", targetClassName = "java.util.Date")
    public Date convertFromLocalTime(LocalTime localTime) throws AtlasConversionException {
        return convertFromLocalTime(localTime, ZoneId.systemDefault());
    }

    public Date convertFromLocalTime(LocalTime localTime, ZoneId zoneId) throws AtlasConversionException {
        return Date.from(localTime.atDate(LocalDate.now()).atZone(zoneId).toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX, sourceClassName = "java.util.Date", targetClassName = "java.time.LocalDate")
    public LocalDate convertToLocalDate(Date date) throws AtlasConversionException {
        return convertToLocalDate(date, ZoneId.systemDefault());
    }

    public LocalDate convertToLocalDate(Date date, ZoneId zoneId) throws AtlasConversionException {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId).toLocalDate();
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE, sourceClassName = "java.time.LocalDate", targetClassName = "java.util.Date")
    public Date convertFromLocalDate(LocalDate localDate) throws AtlasConversionException {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX, sourceClassName = "java.util.Date", targetClassName = "java.sql.Timestamp")
    public Timestamp convertToTimestamp(Date date) throws AtlasConversionException {
        return Timestamp.from(date.toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE, sourceClassName = "java.sql.Timestamp", targetClassName = "java.util.Date")
    public Date convertFromTimestamp(Timestamp timestamp) throws AtlasConversionException {
        return Date.from(timestamp.toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX, sourceClassName = "java.util.Date", targetClassName = "java.sql.Time")
    public Time convertToTime(Date date) throws AtlasConversionException {
        return convertToTime(date, ZoneId.systemDefault());
    }

    public Time convertToTime(Date date, ZoneId zoneId) throws AtlasConversionException {
        return Time.valueOf(LocalDateTime.ofInstant(date.toInstant(), zoneId).toLocalTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE, sourceClassName = "java.sql.Time", targetClassName = "java.util.Date")
    public Date convertFromTime(Time time) throws AtlasConversionException {
        return convertFromLocalTime(time.toLocalTime(), ZoneId.systemDefault());
    }

    public Date convertFromTime(Time time, ZoneId zoneId) throws AtlasConversionException {
        return convertFromLocalTime(time.toLocalTime(), zoneId);
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX, sourceClassName = "java.util.Date", targetClassName = "java.sql.Date")
    public java.sql.Date convertToSqlDate(Date date) throws AtlasConversionException {
        return convertToSqlDate(date, ZoneId.systemDefault());
    }

    public java.sql.Date convertToSqlDate(Date date, ZoneId zoneId) throws AtlasConversionException {
        return java.sql.Date.valueOf(convertToLocalDate(date, zoneId));
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE, sourceClassName = "java.sql.Date", targetClassName = "java.util.Date")
    public Date convertFromSqlDate(java.sql.Date date) throws AtlasConversionException {
        return convertFromSqlDate(date, ZoneId.systemDefault());
    }

    public Date convertFromSqlDate(java.sql.Date date, ZoneId zoneId) throws AtlasConversionException {
        return Date.from(date.toLocalDate().atStartOfDay(zoneId).toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX, sourceClassName = "java.util.Date", targetClassName = "java.util.GregorianCalendar")
    public GregorianCalendar convertToGregorianCalendar(Date date) throws AtlasConversionException {
        return convertToGregorianCalendar(date, ZoneId.systemDefault());
    }

    public GregorianCalendar convertToGregorianCalendar(Date date, ZoneId zoneId) throws AtlasConversionException {
        return GregorianCalendar.from(convertToZonedDateTime(date, zoneId));
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE, sourceClassName = "java.util.GregorianCalendar", targetClassName = "java.util.Date")
    public Date convertFromGregorianCalendar(GregorianCalendar gregorianCalendar) throws AtlasConversionException {
        return convertFromZonedDateTime(gregorianCalendar.toZonedDateTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.COMPLEX, sourceClassName = "java.util.Date", targetClassName = "java.util.Calendar")
    public Calendar convertToCalendar(Date date) throws AtlasConversionException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        return calendar;
    }

    @AtlasConversionInfo(sourceType = FieldType.COMPLEX, targetType = FieldType.DATE, sourceClassName = "java.util.Calendar", targetClassName = "java.util.Date")
    public Date convertFromCalendar(Calendar calendar) throws AtlasConversionException {
        return calendar.getTime();
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.STRING, sourceClassName = "java.util.Date", targetClassName = "java.lang.String")
    public String convertToString(Date date) throws AtlasConversionException {
        // by default Instant.toString returns an ISO-8601 representation of the instant
        return date.toInstant().toString();

    }

    public String convertToString(Date date, DateTimeFormatter formatter) throws AtlasConversionException {
        return formatter.format(convertToZonedDateTime(date));
    }

    /**
     * Assumes a valid ISO 8601 date time string i.e. 2014-02-20T20:04:05.867Z
     *
     * @param iso8601DateString
     * @return
     * @throws AtlasConversionException
     */
    @AtlasConversionInfo(sourceType = FieldType.STRING, targetType = FieldType.DATE, sourceClassName = "java.lang.String", targetClassName = "java.util.Date")
    public Date convertFromString(String iso8601DateString) throws AtlasConversionException {
        return Date.from(ZonedDateTime.parse(iso8601DateString, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.LONG, sourceClassName = "java.util.Date", targetClassName = "java.long.Long")
    public Long convertToLong(Date date) throws AtlasConversionException {
        return date.toInstant().toEpochMilli();
    }

    @AtlasConversionInfo(sourceType = FieldType.LONG, targetType = FieldType.DATE, sourceClassName = "java.lang.Long", targetClassName = "java.util.Date")
    public Date convertFromLong(Long date) throws AtlasConversionException {
        return Date.from(Instant.ofEpochMilli(date));
    }

}
