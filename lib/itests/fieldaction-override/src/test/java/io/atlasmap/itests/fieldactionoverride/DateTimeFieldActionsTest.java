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
package io.atlasmap.itests.fieldactionoverride;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DateTimeFieldActionsTest {
    
    @Test
    public void testDayOfWeekActionInteger() {
        assertEquals("Sunday", DateTimeFieldActions.dayOfWeekInteger(null, 1));
        assertEquals("Monday", DateTimeFieldActions.dayOfWeekInteger(null, 2));
        assertEquals("Tuesday", DateTimeFieldActions.dayOfWeekInteger(null, 3));
        assertEquals("Wednesday", DateTimeFieldActions.dayOfWeekInteger(null, 4));
        assertEquals("Thursday", DateTimeFieldActions.dayOfWeekInteger(null, 5));
        assertEquals("Friday", DateTimeFieldActions.dayOfWeekInteger(null, 6));
        assertEquals("Saturday", DateTimeFieldActions.dayOfWeekInteger(null, 7));
        assertEquals("Funday", DateTimeFieldActions.dayOfWeekInteger(null, Integer.MIN_VALUE));
        assertEquals("Funday", DateTimeFieldActions.dayOfWeekInteger(null, Integer.MAX_VALUE));
    }

    @Test
    public void testDayOfWeekActionString() {
        assertEquals("Sunday", DateTimeFieldActions.dayOfWeekString(null, "sun"));
        assertEquals("Monday", DateTimeFieldActions.dayOfWeekString(null, "mon"));
        assertEquals("Tuesday", DateTimeFieldActions.dayOfWeekString(null, "tue"));
        assertEquals("Wednesday", DateTimeFieldActions.dayOfWeekString(null, "wed"));
        assertEquals("Thursday", DateTimeFieldActions.dayOfWeekString(null, "thur"));
        assertEquals("Friday", DateTimeFieldActions.dayOfWeekString(null, "fri"));
        assertEquals("Saturday", DateTimeFieldActions.dayOfWeekString(null, "sat"));
        assertEquals("Funday", DateTimeFieldActions.dayOfWeekString(null, "foobar"));    
    }

}
