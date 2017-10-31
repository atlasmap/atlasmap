package io.atlasmap.mock.v2;

import static org.junit.Assert.*;
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
