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

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * The helper class to handle date/time related conversion. The timezone string parameter
 * is directly passed in to {@link ZoneId#of(String)}.
 */
public class DateTimeHelper {

    /**
     * Converts from {@link Date} to {@link GregorianCalendar}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static GregorianCalendar convertDateToGregorianCalendar(Date date, String timeZone) {
        return GregorianCalendar.from(DateTimeHelper.toZonedDateTime(date, timeZone));
    }

    /**
     * Converts from {@link java.sql.Date} to {@link Date}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static Date convertSqlDateToDate(java.sql.Date date, String timeZone) {
        return Date.from(date.toLocalDate().atStartOfDay(zoneId(timeZone)).toInstant());
    }

    /**
     * Converts from {@link Date} to {@link java.sql.Date}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static java.sql.Date convertDateToSqlDate(Date date, String timeZone) {
        return java.sql.Date.valueOf(DateTimeHelper.convertDateToLocalDate(date, timeZone));
    }

    /**
     * Coverts from {@link Time} to {@link Date}.
     * @param time value
     * @param timeZone timezone
     * @return converted
     */
    public static Date convertSqlTimeToDate(Time time, String timeZone) {
        return DateTimeHelper.convertLocalTimeToDate(time.toLocalTime(), timeZone); // ?
    }

    /**
     * Converts from {@link Date} to {@link LocalDate}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static LocalDate convertDateToLocalDate(Date date, String timeZone) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId(timeZone)).toLocalDate();
    }

    /**
     * Converts from {@link LocalTime} to {@link Date}.
     * @param localTime value
     * @param timeZone timezone
     * @return converted
     */
    public static Date convertLocalTimeToDate(LocalTime localTime, String timeZone) {
        return Date.from(localTime.atDate(LocalDate.now()).atZone(zoneId(timeZone)).toInstant());
    }

    /**
     * Concerts from {@link Date} to {@link LocalTime}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static LocalTime convertDateToLocalTime(Date date, String timeZone) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId(timeZone)).toLocalTime();
    }

    /**
     * Converts from {@link LocalDateTime} to {@link Date}.
     * @param localDateTime value
     * @param timeZone timezone
     * @return converted
     */
    public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime, String timeZone) {
        return Date.from(localDateTime.atZone(zoneId(timeZone)).toInstant());
    }

    /**
     * Converts from {@link Date} to {@link LocalDateTime}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static LocalDateTime convertDateToLocalDateTime(Date date, String timeZone) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId(timeZone));
    }

    /**
     * Converts from {@link Date} to {@link Time}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static Time convertDateToTime(Date date, String timeZone) {
        return Time.valueOf(LocalDateTime.ofInstant(date.toInstant(), zoneId(timeZone)).toLocalTime());
    }

    /**
     * Converts from {@link Calendar} to {@link ZonedDateTime}.
     * @param calendar value
     * @return converted
     */
    public static ZonedDateTime toZonedDateTime(Calendar calendar) {
        return ZonedDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    /**
     * Converts from {@link Date} to {@link ZonedDateTime}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static ZonedDateTime toZonedDateTime(Date date, String timeZone) {
        return ZonedDateTime.ofInstant(date.toInstant(), zoneId(timeZone));
    }

    /**
     * Converts from {@link LocalDate} to {@link ZonedDateTime}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static ZonedDateTime toZonedDateTime(LocalDate date, String timeZone) {
        return date.atStartOfDay(zoneId(timeZone));
    }

    /**
     * Converts from {@link LocalTime} to {@link ZonedDateTime}.
     * @param time value
     * @param timeZone timezone
     * @return converted
     */
    public static ZonedDateTime toZonedDateTime(LocalTime time, String timeZone) {
        return toZonedDateTime(time.atDate(LocalDate.now()), timeZone);
    }

    /**
     * Converts to {@link LocalDateTime} to {@link ZonedDateTime}.
     * @param date value
     * @param timeZone timezone
     * @return converted
     */
    public static ZonedDateTime toZonedDateTime(LocalDateTime date, String timeZone) {
        return date.atZone(zoneId(timeZone));
    }

    /**
     * Parses timezone expression and create {@link ZoneId} from it.
     * @param timeZone value
     * @return created
     */
    private static ZoneId zoneId(String timeZone) {
        return timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
    }
}
