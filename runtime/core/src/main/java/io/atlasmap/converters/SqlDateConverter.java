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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class SqlDateConverter implements AtlasConverter<java.sql.Date> {

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.DATE_TIME_TZ)
    public Calendar toCalendar(java.sql.Date date) throws AtlasConversionException {
        return date != null ? GregorianCalendar.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault())) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.DATE_TIME)
    public Date toDate(java.sql.Date date, String sourceFormat, String targetFormat)
            throws AtlasConversionException {
        return date != null ? DateTimeHelper.convertSqlDateToDate(date, sourceFormat) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.DATE_TIME_TZ)
    public GregorianCalendar toGregorianCalendar(java.sql.Date date) throws AtlasConversionException {
        return date != null ? GregorianCalendar.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault())) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.DATE)
    public LocalDate toLocalDate(java.sql.Date date) throws AtlasConversionException {
        return date != null ? date.toLocalDate() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.DATE_TIME)
    public LocalDateTime toLocalDateTime(java.sql.Date date) throws AtlasConversionException {
        return date != null ? date.toLocalDate().atStartOfDay() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.DATE_TIME)
    public java.sql.Timestamp toSqlTimestamp(java.sql.Date date) throws AtlasConversionException {
        return date != null ? new java.sql.Timestamp(date.getTime()) : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(java.sql.Date date) throws AtlasConversionException {
        return date != null ? ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault()) : null;
    }

}
