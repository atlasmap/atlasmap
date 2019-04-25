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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class GregorianCalendarConverter implements AtlasConverter<GregorianCalendar> {

    private ZonedDateTimeConverter zdtConverter = new ZonedDateTimeConverter();

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE_TIME_TZ)
    public Calendar toCalendar(GregorianCalendar calendar) {
        return calendar;
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE_TIME)
    public Date toDate(GregorianCalendar calendar) {
        return zdtConverter.toDate(calendar.toZonedDateTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE)
    public LocalDate toLocalDate(GregorianCalendar calendar) {
        return zdtConverter.toLocalDate(calendar.toZonedDateTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(GregorianCalendar calendar) {
        return zdtConverter.toLocalDateTime(calendar.toZonedDateTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.TIME)
    public LocalTime toLocalTime(GregorianCalendar calendar) {
        return zdtConverter.toLocalTime(calendar.toZonedDateTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE)
    public java.sql.Date toSqlDate(GregorianCalendar calendar) {
        return zdtConverter.toSqlDate(calendar.toZonedDateTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.TIME)
    public java.sql.Time toSqlTime(GregorianCalendar calendar) {
        return zdtConverter.toSqlTime(calendar.toZonedDateTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE_TIME)
    public java.sql.Timestamp toSqlTimestamp(GregorianCalendar calendar) {
        return zdtConverter.toSqlTimestamp(calendar.toZonedDateTime());
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZoneDateTime(GregorianCalendar calendar) {
        return calendar.toZonedDateTime();
    }

}
