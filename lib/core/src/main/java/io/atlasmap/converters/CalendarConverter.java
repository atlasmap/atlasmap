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

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

import io.atlasmap.spi.AtlasConverter;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class CalendarConverter implements AtlasConverter<Calendar> {

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE_TIME)
    public Date toDate(Calendar calendar) {
        return calendar != null ? calendar.getTime() : null;
    }

    @AtlasConversionInfo(sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE_TIME_TZ)
    public ZonedDateTime toZonedDateTime(Calendar calendar) {
        return calendar == null ? null : DateTimeHelper.toZonedDateTime(calendar);
    }
}
