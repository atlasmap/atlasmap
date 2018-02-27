/**
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
package io.atlasmap.actions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import io.atlasmap.api.AtlasFieldAction;
import io.atlasmap.spi.AtlasFieldActionInfo;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.AddDays;
import io.atlasmap.v2.AddSeconds;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldType;

@SuppressWarnings({"squid:S3776",     // Cognitive complexity of method
    "squid:S1118",     // Add private constructor
    "squid:S1226",     // Introduce new variable
    "squid:S3358" })   // Extract nested ternary
public class DateFieldActions implements AtlasFieldAction {

    @AtlasFieldActionInfo(name = "AddDays", sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE_TIME_TZ, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Date addDays(Action action, Date input) {
        if (action == null || !(action instanceof AddDays)) {
            throw new IllegalArgumentException("Action must be an AddDays action");
        }

        if (input == null) {
            return null;
        }

        LocalDateTime dateTime = LocalDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault());
        AddDays addDays = (AddDays) action;
        dateTime = dateTime.plusDays(addDays.getDays() == null ? 0 : addDays.getDays());
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @AtlasFieldActionInfo(name = "AddSeconds", sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.DATE_TIME_TZ, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Date addSeconds(Action action, Date input) {
        if (action == null || !(action instanceof AddSeconds)) {
            throw new IllegalArgumentException("Action must be an AddSeconds action");
        }

        if (input == null) {
            return null;
        }

        LocalDateTime dateTime = LocalDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault());
        AddSeconds addSeconds = (AddSeconds) action;
        dateTime = dateTime.plusSeconds(addSeconds.getSeconds() == null ? 0 : addSeconds.getSeconds());
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @AtlasFieldActionInfo(name = "CurrentDate", sourceType = FieldType.ANY, targetType = FieldType.DATE_TZ, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Date currentDate(Action action, Object input) {
        return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @AtlasFieldActionInfo(name = "CurrentDateTime", sourceType = FieldType.ANY, targetType = FieldType.DATE_TIME_TZ, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Date currentDateTime(Action action, Object input) {
        return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    }

    @AtlasFieldActionInfo(name = "CurrentTime", sourceType = FieldType.ANY, targetType = FieldType.TIME_TZ, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Date currentTime(Action action, Object input) {
        return currentDateTime(action, input);
    }

    @AtlasFieldActionInfo(name = "DayOfWeek", sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Integer dayOfWeek(Action action, Date input) {
        if (input == null) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault());
        return dateTime.getDayOfWeek().getValue();
    }

    @AtlasFieldActionInfo(name = "DayOfYear", sourceType = FieldType.DATE_TIME_TZ, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Integer dayOfYear(Action action, Date input) {
        if (input == null) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(input.toInstant(), ZoneId.systemDefault());
        return dateTime.getDayOfYear();
    }
}
