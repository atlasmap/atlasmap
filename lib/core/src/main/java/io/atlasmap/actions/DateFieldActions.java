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

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.v2.AddDays;
import io.atlasmap.v2.AddSeconds;
import io.atlasmap.v2.CurrentDate;
import io.atlasmap.v2.CurrentDateTime;
import io.atlasmap.v2.CurrentTime;
import io.atlasmap.v2.DayOfMonth;
import io.atlasmap.v2.DayOfWeek;
import io.atlasmap.v2.DayOfYear;
import io.atlasmap.v2.FieldType;

@SuppressWarnings({"squid:S3776",     // Cognitive complexity of method
    "squid:S1118",     // Add private constructor
    "squid:S1226",     // Introduce new variable
    "squid:S3358" })   // Extract nested ternary
public class DateFieldActions implements AtlasFieldAction {

    @AtlasActionProcessor(sourceType = FieldType.ANY_DATE)
    public static ZonedDateTime addDays(AddDays addDays, ZonedDateTime input) {
        if (addDays == null) {
            throw new IllegalArgumentException("AddDays action must be specified");
        }
        if (input == null) {
            return null;
        }

        return input.plusDays(addDays.getDays() == null ? 0L : addDays.getDays());
    }

    @AtlasActionProcessor(sourceType = FieldType.ANY_DATE)
    public static ZonedDateTime addSeconds(AddSeconds addSeconds, ZonedDateTime input) {
        if (addSeconds == null) {
            throw new IllegalArgumentException("AddSeconds action must be specified");
        }
        if (input == null) {
            return null;
        }

        return input.plusSeconds(addSeconds.getSeconds() == null ? 0L : addSeconds.getSeconds());
    }

    @AtlasActionProcessor
    public static ZonedDateTime currentDate(CurrentDate action) {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault());
    }

    @AtlasActionProcessor
    public static ZonedDateTime currentDateTime(CurrentDateTime action) {
        return LocalDate.now().atStartOfDay(ZoneId.systemDefault());
    }

    @AtlasActionProcessor
    public static ZonedDateTime currentTime(CurrentTime action) {
        return LocalTime.now().atDate(LocalDate.now()).atZone(ZoneId.systemDefault());
    }

    @AtlasActionProcessor(sourceType = FieldType.ANY_DATE)
    public static Integer dayOfMonth(DayOfMonth action, ZonedDateTime input) {
        return input == null ? null : input.getDayOfMonth();
    }

    @AtlasActionProcessor(sourceType = FieldType.ANY_DATE)
    public static Integer dayOfWeek(DayOfWeek action, ZonedDateTime input) {
        return input == null ? null : input.getDayOfWeek().getValue();
    }

    @AtlasActionProcessor(sourceType = FieldType.ANY_DATE)
    public static Integer dayOfYear(DayOfYear action, ZonedDateTime input) {
        return input == null ? null : input.getDayOfYear();
    }
}
