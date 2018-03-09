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

public class DateFieldActionsTest {

    @Test
    public void testAddDays() {
        AddDays action = new AddDays();
        action.setDays(2);
        assertNull(DateFieldActions.addDays(action, null));
        ZonedDateTime origDate = ZonedDateTime.now();
        ZonedDateTime laterDate = origDate.plusDays(2);
        assertEquals(laterDate, DateFieldActions.addDays(action, origDate));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddDaysWithNullAction() {
        DateFieldActions.addDays(null, ZonedDateTime.now());
    }

    @Test
    public void testAddSeconds() {
        AddSeconds action = new AddSeconds();
        action.setSeconds(2);
        assertNull(DateFieldActions.addSeconds(action, null));
        ZonedDateTime origDate = ZonedDateTime.now();
        ZonedDateTime laterDate = origDate.plusSeconds(2);
        assertEquals(laterDate, DateFieldActions.addSeconds(action, origDate));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddSecondsWithNullAction() {
        DateFieldActions.addSeconds(null, ZonedDateTime.now());
    }

    @Test
    public void testCurrentDate() {
        assertNotNull(DateFieldActions.currentDate(null, null));
    }

    @Test
    public void testCurrentDateTime() {
        assertNotNull(DateFieldActions.currentDateTime(null, null));
    }

    @Test
    public void testCurrentTime() {
        assertNotNull(DateFieldActions.currentTime(null, null));
    }

    @Test
    public void testDayOfWeek() {
        assertNull(DateFieldActions.dayOfWeek(null, null));
        ZonedDateTime origDate = LocalDate.of(2017, 12, 14).atStartOfDay(ZoneId.systemDefault());
        assertEquals(Integer.valueOf(4), DateFieldActions.dayOfWeek(null, origDate));
    }

    @Test
    public void testDayOfYear() {
        assertNull(DateFieldActions.dayOfYear(null, null));
        ZonedDateTime origDate = LocalDate.of(2017, 12, 31).atStartOfDay(ZoneId.systemDefault());
        assertEquals(Integer.valueOf(365), DateFieldActions.dayOfYear(null, origDate));
    }
}
