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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Test;

public class DateConverterTest {

    private DateConverter dateConverter = new DateConverter();

    @Test
    public void convertToZonedDateTime() {
        ZonedDateTime zonedDateTime = DateTimeHelper.toZonedDateTime(new Date(), null);
        assertNotNull(zonedDateTime);
        assertTrue(zonedDateTime instanceof ZonedDateTime);
        assertTrue(zonedDateTime.getZone().getId().equals(ZoneId.systemDefault().getId()));
    }

    @Test
    public void convertToZonedDateTimeWithZoneId() {
        ZonedDateTime zonedDateTime = DateTimeHelper.toZonedDateTime(new Date(), "America/New_York");
        assertNotNull(zonedDateTime);
        assertTrue(zonedDateTime instanceof ZonedDateTime);
        assertTrue(zonedDateTime.getZone().getId().equals("America/New_York"));
    }

    @Test
    public void convertToLocalDateTime() {
        LocalDateTime localDateTime = DateTimeHelper.convertDateToLocalDateTime(new Date(), null);
        assertNotNull(localDateTime);
        assertTrue(localDateTime instanceof LocalDateTime);
    }

    @Test
    public void convertToLocalDateTimeWithZoneId() {
        LocalDateTime localDateTime = DateTimeHelper.convertDateToLocalDateTime(new Date(), "America/New_York");
        assertNotNull(localDateTime);
        assertTrue(localDateTime instanceof LocalDateTime);
        assertTrue(localDateTime.atZone(ZoneId.of("America/New_York")).getZone().getId().equals("America/New_York"));
    }

    @Test
    public void convertFromLocalDateTime() {
        Date date = DateTimeHelper.convertLocalDateTimeToDate(LocalDateTime.now(), null);
        assertNotNull(date);
        assertEquals(date.getTime(), date.toInstant().toEpochMilli());
    }

    @Test
    public void convertToLocalTime() {
        LocalTime localTime = DateTimeHelper.convertDateToLocalTime(new Date(), null);
        assertNotNull(localTime);
        assertTrue(localTime instanceof LocalTime);
    }

    @Test
    public void convertToLocalTimeWithZoneId() {
        LocalTime localTime = DateTimeHelper.convertDateToLocalTime(new Date(), "America/New_York");
        assertNotNull(localTime);
        assertTrue(localTime instanceof LocalTime);
    }

    @Test
    public void convertFromLocalTime() {
        Date date = DateTimeHelper.convertLocalTimeToDate(LocalTime.now(), null);
        assertNotNull(date);
    }

    @Test
    public void convertToLocalDate() {
        LocalDate localDate = DateTimeHelper.convertDateToLocalDate(new Date(), null);
        assertNotNull(localDate);
        assertTrue(localDate instanceof LocalDate);
    }

    @Test
    public void convertToLocalDateWithZoneId() {
        LocalDate localDate = DateTimeHelper.convertDateToLocalDate(new Date(), "America/New_York");
        assertNotNull(localDate);
        assertTrue(localDate instanceof LocalDate);
    }

    @Test
    public void convertToTimestamp() {
        Timestamp timestamp = dateConverter.toSqlTimestamp(new Date());
        assertNotNull(timestamp);
        assertTrue(timestamp instanceof Timestamp);
    }

    @Test
    public void convertToTime() {
        Time time = DateTimeHelper.convertDateToTime(new Date(), null);
        assertNotNull(time);
        assertTrue(time instanceof Time);
    }

    @Test
    public void convertToTimeWithZoneId() {
        Time time = DateTimeHelper.convertDateToTime(new Date(), "America/New_York");
        assertNotNull(time);
        assertTrue(time instanceof Time);
    }

    @Test
    public void convertFromTime() {
        Date date = DateTimeHelper.convertSqlTimeToDate(Time.valueOf(LocalTime.now()), null);
        assertNotNull(date);
    }

    @Test
    public void convertFromTimeWithZoneId() {
        Date date = DateTimeHelper.convertSqlTimeToDate(Time.valueOf(LocalTime.now()), "America/New_York");
        assertNotNull(date);
    }

    @Test
    public void convertToSqlDate() {
        java.sql.Date date = DateTimeHelper.convertDateToSqlDate(new Date(), null);
        assertNotNull(date);
        assertTrue(date instanceof java.sql.Date);
    }

    @Test
    public void convertFromSqlDate() {
        Date date = DateTimeHelper.convertSqlDateToDate(java.sql.Date.valueOf(LocalDate.now()), null);
        assertNotNull(date);
        assertTrue(date instanceof Date);
    }

    @Test
    public void convertFromSqlDateWithZoneId() {
        Date date = DateTimeHelper.convertSqlDateToDate(java.sql.Date.valueOf(LocalDate.now()), "America/New_York");
        assertNotNull(date);
        assertTrue(date instanceof Date);
    }

    @Test
    public void convertToGregorianCalendar() {
        GregorianCalendar gregorianCalendar = DateTimeHelper.convertDateToGregorianCalendar(new Date(), null);
        assertNotNull(gregorianCalendar);
        assertTrue(gregorianCalendar instanceof GregorianCalendar);
    }

    @Test
    public void convertToGregorianCalendarWithZoneId() {
        GregorianCalendar gregorianCalendar = DateTimeHelper.convertDateToGregorianCalendar(new Date(), "America/New_York");
        assertNotNull(gregorianCalendar);
        assertTrue(gregorianCalendar instanceof GregorianCalendar);
        assertTrue(gregorianCalendar.getTimeZone().getID().equals("America/New_York"));
    }

    @Test
    public void convertToCalendar() {
        Calendar calendar = dateConverter.toCalendar(new Date());
        assertNotNull(calendar);
        assertTrue(calendar instanceof GregorianCalendar);
    }

    @Test
    public void convertToString() {
        String dateString = dateConverter.toString(new Date());
        assertNotNull(dateString);
        assertTrue(dateString instanceof String);
    }

    @Test
    public void convertToLong() {
        Date now = new Date();
        Long dateAsLong = dateConverter.toLong(now);
        assertNotNull(dateAsLong);
        assertTrue(dateAsLong instanceof Long);
        assertTrue(now.getTime() == dateAsLong);
    }
}
