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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class SqlTimeConverter implements AtlasConverter<Time> {

    @AtlasConversionInfo(sourceType = FieldType.TIME, targetType = FieldType.DATE_TIME_TZ)
    public Calendar toCalendar(Time time, String sourceFormat, String targetFormat) throws AtlasConversionException {
        return time != null ? GregorianCalendar.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(time.getTime()), ZoneId.systemDefault())) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.TIME, targetType = FieldType.DATE_TIME)
    public Date toDate(Time time, String sourceFormat, String targetFormat) throws AtlasConversionException {
        return time != null ? new Date(time.getTime()) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.TIME, targetType = FieldType.DATE_TIME_TZ)
    public GregorianCalendar toGregorianCalendar(Time time, String sourceFormat, String targetFormat) throws AtlasConversionException {
        return time != null ? GregorianCalendar.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(time.getTime()), ZoneId.systemDefault())) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.TIME, targetType = FieldType.TIME)
    public LocalTime toLocalTime(Time time, String sourceFormat, String targetFormat) throws AtlasConversionException {
        return time != null ? time.toLocalTime() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.TIME, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(Time time, String sourceFormat, String targetFormat) throws AtlasConversionException {
        return time != null ? time.toLocalTime().atDate(LocalDate.now()) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.TIME, targetType = FieldType.DATE_TIME)
    public Timestamp toSqlTimestamp(Time time, String sourceFormat, String targetFormat) throws AtlasConversionException {
        return time != null ? new Timestamp(time.getTime()) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.TIME, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(Time time, String sourceFormat, String targetFormat) throws AtlasConversionException {
        return time != null ? ZonedDateTime.ofInstant(Instant.ofEpochMilli(time.getTime()), ZoneId.systemDefault()) : null;
    }

}
