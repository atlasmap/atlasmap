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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

import io.atlasmap.v2.AddDays;
import io.atlasmap.v2.AddSeconds;
import io.atlasmap.v2.CurrentDate;
import io.atlasmap.v2.CurrentDateTime;
import io.atlasmap.v2.CurrentTime;
import io.atlasmap.v2.DayOfMonth;
import io.atlasmap.v2.DayOfWeek;
import io.atlasmap.v2.DayOfYear;

public class DateFieldActionsTest {

    @Test
    public void testAddDays() {
        AddDays action = new AddDays();
        action.setDays(2);
        assertNull(action.addDays(null));
        ZonedDateTime origDate = ZonedDateTime.now();
        ZonedDateTime laterDate = origDate.plusDays(2);
        assertEquals(laterDate, action.addDays(origDate));
    }

    @Test
    public void testAddSeconds() {
        AddSeconds action = new AddSeconds();
        action.setSeconds(2);
        assertNull(action.addSeconds(null));
        ZonedDateTime origDate = ZonedDateTime.now();
        ZonedDateTime laterDate = origDate.plusSeconds(2);
        assertEquals(laterDate, action.addSeconds(origDate));
    }

    @Test
    public void testCurrentDate() {
        CurrentDate action = new CurrentDate();
        assertNotNull(action.currentDate(null));
    }

    @Test
    public void testCurrentDateTime() {
        CurrentDateTime action = new CurrentDateTime();
        assertNotNull(action.currentDateTime(null));
    }

    @Test
    public void testCurrentTime() {
        CurrentTime action = new CurrentTime();
        assertNotNull(action.currentTime(null));
    }

    @Test
    public void testDayOfMonth() {
        DayOfMonth action = new DayOfMonth();
        assertNull(action.dayOfMonth(null));
        ZonedDateTime origDate = LocalDate.of(2018,  10,  3).atStartOfDay(ZoneId.systemDefault());
        assertEquals(Integer.valueOf(3), action.dayOfMonth(origDate));
    }

    @Test
    public void testDayOfWeek() {
        DayOfWeek action = new DayOfWeek();
        assertNull(action.dayOfWeek(null));
        ZonedDateTime origDate = LocalDate.of(2017, 12, 14).atStartOfDay(ZoneId.systemDefault());
        assertEquals(Integer.valueOf(4), action.dayOfWeek(origDate));
    }

    @Test
    public void testDayOfYear() {
        DayOfYear action = new DayOfYear();
        assertNull(action.dayOfYear(null));
        ZonedDateTime origDate = LocalDate.of(2017, 12, 31).atStartOfDay(ZoneId.systemDefault());
        assertEquals(Integer.valueOf(365), action.dayOfYear(origDate));
    }
}
