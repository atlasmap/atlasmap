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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class DateConverterTest {

    private DateConverter dateConverter = new DateConverter();

    @Test
    public void convertToZonedDateTime() throws Exception {
        ZonedDateTime zonedDateTime = dateConverter.convertToZonedDateTime(new Date(), null);
        assertNotNull(zonedDateTime);
        assertThat(zonedDateTime, instanceOf(ZonedDateTime.class));
        assertTrue(zonedDateTime.getZone().getId().equals(ZoneId.systemDefault().getId()));
    }

    @Test
    public void convertToZonedDateTimeWithZoneId() throws Exception {
        ZonedDateTime zonedDateTime = dateConverter.convertToZonedDateTime(new Date(), "America/New_York");
        assertNotNull(zonedDateTime);
        assertThat(zonedDateTime, instanceOf(ZonedDateTime.class));
        assertTrue(zonedDateTime.getZone().getId().equals("America/New_York"));
    }

    @Test
    public void convertFromZonedDateTime() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();
        Date date = dateConverter.convertFromZonedDateTime(now);
        assertNotNull(date);
        assertEquals(date.getTime(), now.toInstant().toEpochMilli());
    }

    @Test
    public void convertToLocalDateTime() throws Exception {
        LocalDateTime localDateTime = dateConverter.convertToLocalDateTime(new Date(), null);
        assertNotNull(localDateTime);
        assertThat(localDateTime, instanceOf(LocalDateTime.class));
    }

    @Test
    public void convertToLocalDateTimeWithZoneId() throws Exception {
        LocalDateTime localDateTime = dateConverter.convertToLocalDateTime(new Date(), "America/New_York");
        assertNotNull(localDateTime);
        assertThat(localDateTime, instanceOf(LocalDateTime.class));
        assertTrue(localDateTime.atZone(ZoneId.of("America/New_York")).getZone().getId().equals("America/New_York"));
    }

    @Test
    public void convertFromLocalDateTime() throws Exception {
        Date date = dateConverter.convertFromLocalDateTime(LocalDateTime.now(), null);
        assertNotNull(date);
        assertEquals(date.getTime(), date.toInstant().toEpochMilli());
    }

    @Test
    public void convertToLocalTime() throws Exception {
        LocalTime localTime = dateConverter.convertToLocalTime(new Date(), null);
        assertNotNull(localTime);
        assertThat(localTime, instanceOf(LocalTime.class));
    }

    @Test
    public void convertToLocalTimeWithZoneId() throws Exception {
        LocalTime localTime = dateConverter.convertToLocalTime(new Date(), "America/New_York");
        assertNotNull(localTime);
        assertThat(localTime, instanceOf(LocalTime.class));
    }

    @Test
    public void convertFromLocalTime() throws Exception {
        Date date = dateConverter.convertFromLocalTime(LocalTime.now(), null);
        assertNotNull(date);
    }

    @Test
    public void convertToLocalDate() throws Exception {
        LocalDate localDate = dateConverter.convertToLocalDate(new Date(), null);
        assertNotNull(localDate);
        assertThat(localDate, instanceOf(LocalDate.class));
    }

    @Test
    public void convertToLocalDateWithZoneId() throws Exception {
        LocalDate localDate = dateConverter.convertToLocalDate(new Date(), "America/New_York");
        assertNotNull(localDate);
        assertThat(localDate, instanceOf(LocalDate.class));
    }

    @Test
    public void convertFromLocalDate() throws Exception {
        Date date = dateConverter.convertFromLocalDate(LocalDate.now(), null, null);
        assertNotNull(date);
    }

    @Test
    public void convertToTimestamp() throws Exception {
        Timestamp timestamp = dateConverter.convertToTimestamp(new Date());
        assertNotNull(timestamp);
        assertThat(timestamp, instanceOf(Timestamp.class));
    }

    @Test
    public void convertFromTimestamp() throws Exception {
        Date date = dateConverter.convertFromTimestamp(new Timestamp(System.nanoTime()));
        assertNotNull(date);
    }

    @Test
    public void convertToTime() throws Exception {
        Time time = dateConverter.convertToTime(new Date(), null);
        assertNotNull(time);
        assertThat(time, instanceOf(Time.class));
    }

    @Test
    public void convertToTimeWithZoneId() throws Exception {
        Time time = dateConverter.convertToTime(new Date(), "America/New_York");
        assertNotNull(time);
        assertThat(time, instanceOf(Time.class));
    }

    @Test
    public void convertFromTime() throws Exception {
        Date date = dateConverter.convertFromTime(Time.valueOf(LocalTime.now()), null);
        assertNotNull(date);
    }

    @Test
    public void convertFromTimeWithZoneId() throws Exception {
        Date date = dateConverter.convertFromTime(Time.valueOf(LocalTime.now()), "America/New_York");
        assertNotNull(date);
    }

    @Test
    public void convertToSqlDate() throws Exception {
        java.sql.Date date = dateConverter.convertToSqlDate(new Date(), null);
        assertNotNull(date);
        assertThat(date, instanceOf(java.sql.Date.class));
    }

    @Test
    public void convertFromSqlDate() throws Exception {
        Date date = dateConverter.convertFromSqlDate(java.sql.Date.valueOf(LocalDate.now()), null);
        assertNotNull(date);
        assertThat(date, instanceOf(Date.class));
    }

    @Test
    public void convertFromSqlDateWithZoneId() throws Exception {
        Date date = dateConverter.convertFromSqlDate(java.sql.Date.valueOf(LocalDate.now()), "America/New_York");
        assertNotNull(date);
        assertThat(date, instanceOf(Date.class));
    }

    @Test
    public void convertToGregorianCalendar() throws Exception {
        GregorianCalendar gregorianCalendar = dateConverter.convertToGregorianCalendar(new Date(), null);
        assertNotNull(gregorianCalendar);
        assertThat(gregorianCalendar, instanceOf(GregorianCalendar.class));
    }

    @Test
    public void convertToGregorianCalendarWithZoneId() throws Exception {
        GregorianCalendar gregorianCalendar = dateConverter.convertToGregorianCalendar(new Date(), "America/New_York");
        assertNotNull(gregorianCalendar);
        assertThat(gregorianCalendar, instanceOf(GregorianCalendar.class));
        assertTrue(gregorianCalendar.getTimeZone().getID().equals("America/New_York"));
    }

    @Test
    public void convertFromGregorianCalendar() throws Exception {
        Date date = dateConverter.convertFromGregorianCalendar((GregorianCalendar) GregorianCalendar.getInstance());
        assertNotNull(date);
    }

    @Test
    public void convertToCalendar() throws Exception {
        Calendar calendar = dateConverter.convertToCalendar(new Date());
        assertNotNull(calendar);
        assertThat(calendar, instanceOf(GregorianCalendar.class));
    }

    @Test
    public void convertFromCalendar() throws Exception {
        Date date = dateConverter.convertFromCalendar(Calendar.getInstance());
        assertNotNull(date);
    }

    @Test
    public void convertToString() throws Exception {
        String dateString = dateConverter.convertToString(new Date());
        assertNotNull(dateString);
        assertThat(dateString, instanceOf(String.class));
    }

    @Test
    public void convertFromString() throws Exception {
        // assumes a valid ISO 8601 date time string
        Date date = dateConverter.convertFromString(Instant.now().toString(), null, null);
        assertNotNull(date);
        date = dateConverter.convertFromString("2014-02-20T20:04:05.867Z", null, null);
        assertNotNull(date);
    }

    @Test
    public void convertToLong() throws Exception {
        Date now = new Date();
        Long dateAsLong = dateConverter.convertToLong(now);
        assertNotNull(dateAsLong);
        assertThat(dateAsLong, instanceOf(Long.class));
        assertTrue(now.getTime() == dateAsLong);
    }

    @Test
    public void convertFromLong() throws Exception {
        Date date = dateConverter.convertFromLong(Long.MAX_VALUE);
        assertNotNull(date);
        date = dateConverter.convertFromLong(Long.MIN_VALUE);
        assertNotNull(date);
        date = dateConverter.convertFromLong(Long.parseLong("0"));
        assertTrue(date.toInstant().toString().equals("1970-01-01T00:00:00Z"));
    }

}
