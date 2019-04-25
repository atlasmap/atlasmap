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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import io.atlasmap.spi.AtlasFieldAction;
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

    @AtlasFieldActionInfo(name = "AddDays", sourceType = FieldType.ANY_DATE, targetType = FieldType.ANY_DATE, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static ZonedDateTime addDays(Action action, ZonedDateTime input) {
        if (action == null || !(action instanceof AddDays)) {
            throw new IllegalArgumentException("Action must be an AddDays action");
        }

        if (input == null) {
            return null;
        }

        AddDays addDays = (AddDays) action;
        return input.plusDays(addDays.getDays() == null ? 0L : addDays.getDays());
    }

    @AtlasFieldActionInfo(name = "AddSeconds", sourceType = FieldType.ANY_DATE, targetType = FieldType.ANY_DATE, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static ZonedDateTime addSeconds(Action action, ZonedDateTime input) {
        if (action == null || !(action instanceof AddSeconds)) {
            throw new IllegalArgumentException("Action must be an AddSeconds action");
        }

        if (input == null) {
            return null;
        }

        AddSeconds addSeconds = (AddSeconds) action;
        return input.plusSeconds(addSeconds.getSeconds() == null ? 0L : addSeconds.getSeconds());
    }

    @AtlasFieldActionInfo(name = "CurrentDate", sourceType = FieldType.NONE, targetType = FieldType.ANY_DATE, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static ZonedDateTime currentDate(Action action, Object input) {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault());
    }

    @AtlasFieldActionInfo(name = "CurrentDateTime", sourceType = FieldType.NONE, targetType = FieldType.ANY_DATE, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static ZonedDateTime currentDateTime(Action action, Object input) {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault());
    }

    @AtlasFieldActionInfo(name = "CurrentTime", sourceType = FieldType.NONE, targetType = FieldType.DATE_TIME, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static ZonedDateTime currentTime(Action action, Object input) {
        return LocalTime.now().atDate(LocalDate.now()).atZone(ZoneId.systemDefault());
    }

    @AtlasFieldActionInfo(name = "DayOfMonth", sourceType = FieldType.ANY_DATE, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Integer dayOfMonth(Action action, ZonedDateTime input) {
        return input == null ? null : input.getDayOfMonth();
    }

    @AtlasFieldActionInfo(name = "DayOfWeek", sourceType = FieldType.ANY_DATE, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Integer dayOfWeek(Action action, ZonedDateTime input) {
        return input == null ? null : input.getDayOfWeek().getValue();
    }

    @AtlasFieldActionInfo(name = "DayOfYear", sourceType = FieldType.ANY_DATE, targetType = FieldType.INTEGER, sourceCollectionType = CollectionType.NONE, targetCollectionType = CollectionType.NONE)
    public static Integer dayOfYear(Action action, ZonedDateTime input) {
        return input == null ? null : input.getDayOfYear();
    }
}
