package io.atlasmap.mock.v2;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DateTimeFieldActionsTest {

    private DateTimeFieldActions actions;
    

    
    @Test
    public void testDayOfWeekActionInteger() {
        assertEquals("Sunday", DateTimeFieldActions.DayOfWeekInteger(null, 1));
        assertEquals("Monday", DateTimeFieldActions.DayOfWeekInteger(null, 2));
        assertEquals("Tuesday", DateTimeFieldActions.DayOfWeekInteger(null, 3));
        assertEquals("Wednesday", DateTimeFieldActions.DayOfWeekInteger(null, 4));
        assertEquals("Thursday", DateTimeFieldActions.DayOfWeekInteger(null, 5));
        assertEquals("Friday", DateTimeFieldActions.DayOfWeekInteger(null, 6));
        assertEquals("Saturday", DateTimeFieldActions.DayOfWeekInteger(null, 7));
        assertEquals("Funday", DateTimeFieldActions.DayOfWeekInteger(null, Integer.MIN_VALUE));
        assertEquals("Funday", DateTimeFieldActions.DayOfWeekInteger(null, Integer.MAX_VALUE));
    }

    @Test
    public void testDayOfWeekActionString() {
        assertEquals("Sunday", DateTimeFieldActions.DayOfWeekString(null, "sun"));
        assertEquals("Monday", DateTimeFieldActions.DayOfWeekString(null, "mon"));
        assertEquals("Tuesday", DateTimeFieldActions.DayOfWeekString(null, "tue"));
        assertEquals("Wednesday", DateTimeFieldActions.DayOfWeekString(null, "wed"));
        assertEquals("Thursday", DateTimeFieldActions.DayOfWeekString(null, "thur"));
        assertEquals("Friday", DateTimeFieldActions.DayOfWeekString(null, "fri"));
        assertEquals("Saturday", DateTimeFieldActions.DayOfWeekString(null, "sat"));
        assertEquals("Funday", DateTimeFieldActions.DayOfWeekString(null, "foobar"));    
    }

}
