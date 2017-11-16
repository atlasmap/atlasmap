package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

public class StringDelimiterTest {

    @Test
    public void testGetNameValue() {
        assertEquals("Colon", StringDelimiter.COLON.getName());
        assertEquals(":", StringDelimiter.COLON.getValue());
        assertEquals("Comma", StringDelimiter.COMMA.getName());
        assertEquals(",", StringDelimiter.COMMA.getValue());
        assertEquals("MultiSpace", StringDelimiter.MULTISPACE.getName());
        assertEquals("\\s+", StringDelimiter.MULTISPACE.getValue());
        assertEquals("Space", StringDelimiter.SPACE.getName());
        assertEquals("\\s", StringDelimiter.SPACE.getValue());
    }

    @Test
    public void testGetAll() {
        List<StringDelimiter> all = StringDelimiter.getAll();
        assertNotNull(all);
        assertEquals(new Integer(4), new Integer(all.size()));
        assertEquals(StringDelimiter.SPACE, all.get(0));
        assertEquals(StringDelimiter.MULTISPACE, all.get(1));
        assertEquals(StringDelimiter.COMMA, all.get(2));
        assertEquals(StringDelimiter.COLON, all.get(3));
    }

    @Test
    public void testGetAllValues() {
        List<String> values = StringDelimiter.getAllValues();
        assertNotNull(values);
        assertEquals(new Integer(4), new Integer(values.size()));
        assertEquals(StringDelimiter.SPACE.getValue(), values.get(0));
        assertEquals(StringDelimiter.MULTISPACE.getValue(), values.get(1));
        assertEquals(StringDelimiter.COMMA.getValue(), values.get(2));
        assertEquals(StringDelimiter.COLON.getValue(), values.get(3));
    }

    @Test
    public void testGetAllNames() {
        List<String> names = StringDelimiter.getAllNames();
        assertNotNull(names);
        assertEquals(new Integer(4), new Integer(names.size()));
        assertEquals(StringDelimiter.SPACE.getName(), names.get(0));
        assertEquals(StringDelimiter.MULTISPACE.getName(), names.get(1));
        assertEquals(StringDelimiter.COMMA.getName(), names.get(2));
        assertEquals(StringDelimiter.COLON.getName(), names.get(3));
    }

}
