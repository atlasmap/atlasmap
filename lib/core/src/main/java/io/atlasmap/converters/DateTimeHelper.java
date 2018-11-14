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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeHelper {

    public static GregorianCalendar convertDateToGregorianCalendar(Date date, String timeZone) {
        return GregorianCalendar.from(DateTimeHelper.toZonedDateTime(date, timeZone));
    }

    public static Date convertSqlDateToDate(java.sql.Date date, String timeZone) {
        return Date.from(date.toLocalDate().atStartOfDay(zoneId(timeZone)).toInstant());
    }

    public static java.sql.Date convertDateToSqlDate(Date date, String timeZone) {
        return java.sql.Date.valueOf(DateTimeHelper.convertDateToLocalDate(date, timeZone));
    }

    public static Date convertSqlTimeToDate(Time time, String timeZone) {
        return DateTimeHelper.convertLocalTimeToDate(time.toLocalTime(), timeZone); // ?
    }

    public static LocalDate convertDateToLocalDate(Date date, String timeZone) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId(timeZone)).toLocalDate();
    }

    public static Date convertLocalTimeToDate(LocalTime localTime, String timeZone) {
        return Date.from(localTime.atDate(LocalDate.now()).atZone(zoneId(timeZone)).toInstant());
    }

    public static LocalTime convertDateToLocalTime(Date date, String timeZone) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId(timeZone)).toLocalTime();
    }

    public static Date convertLocalDateTimeToDate(LocalDateTime localDateTime, String timeZone) {
        return Date.from(localDateTime.atZone(zoneId(timeZone)).toInstant());
    }

    public static LocalDateTime convertDateToLocalDateTime(Date date, String timeZone) {
        return LocalDateTime.ofInstant(date.toInstant(), zoneId(timeZone));
    }

    public static Time convertDateToTime(Date date, String timeZone) {
        return Time.valueOf(LocalDateTime.ofInstant(date.toInstant(), zoneId(timeZone)).toLocalTime());
    }

    public static ZonedDateTime toZonedDateTime(Calendar calendar) {
        return ZonedDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    public static ZonedDateTime toZonedDateTime(Date date, String timeZone) {
        return ZonedDateTime.ofInstant(date.toInstant(), zoneId(timeZone));
    }

    public static ZonedDateTime toZonedDateTime(LocalDate date, String timeZone) {
        return date.atStartOfDay(zoneId(timeZone));
    }

    public static ZonedDateTime toZonedDateTime(LocalTime time, String timeZone) {
        return toZonedDateTime(time.atDate(LocalDate.now()), timeZone);
    }

    public static ZonedDateTime toZonedDateTime(LocalDateTime date, String timeZone) {
        return date.atZone(zoneId(timeZone));
    }

    private static ZoneId zoneId(String timeZone) {
        return timeZone != null ? ZoneId.of(timeZone) : ZoneId.systemDefault();
    }
}
