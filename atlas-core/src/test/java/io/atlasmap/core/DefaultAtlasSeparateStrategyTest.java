package io.atlasmap.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultAtlasSeparateStrategyTest {

    private DefaultAtlasSeparateStrategy sepStrategy = null;
    
    @Before
    public void setUp() throws Exception {
        sepStrategy = new DefaultAtlasSeparateStrategy();
    }

    @After
    public void tearDown() throws Exception {
        sepStrategy = null;
    }

    @Test
    public void testGetName() {
        assertNotNull(sepStrategy);
        assertEquals("DefaultAtlasSeparateStrategy", sepStrategy.getName());
    }

    @Test
    public void testGetSetDelimiter() {
        assertNotNull(sepStrategy);
        assertEquals(StringDelimiter.MULTISPACE.getValue(), sepStrategy.getDelimiter());
        sepStrategy.setDelimiter(StringDelimiter.COLON.getValue());
        assertEquals(StringDelimiter.COLON.getValue(), sepStrategy.getDelimiter());
    }

    @Test
    public void testSeparateValue() {
        assertNotNull(sepStrategy);
        List<String> values = sepStrategy.separateValue("foo bar blah", " ", 128);
        assertNotNull(values);
        assertEquals(new Integer(3), new Integer(values.size()));
        assertEquals("foo", values.get(0));
        assertEquals("bar", values.get(1));
        assertEquals("blah", values.get(2));
    }
    
    @Test
    public void testSeparateValueNull() {
        assertNotNull(sepStrategy);
        List<String> values = sepStrategy.separateValue(null, " ", 128);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }
    
    @Test
    public void testSeparateValueEmpty() {
        assertNotNull(sepStrategy);
        List<String> values = sepStrategy.separateValue("", " ", 128);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }
    
    @Test
    public void testSeparateValueNullDelimiter() {
        assertNotNull(sepStrategy);
        List<String> values = sepStrategy.separateValue("foo bar blah", null, 128);
        assertNotNull(values);
        assertEquals(new Integer(3), new Integer(values.size()));
        assertEquals("foo", values.get(0));
        assertEquals("bar", values.get(1));
        assertEquals("blah", values.get(2));
    }
    
    @Test
    public void testSeparateValueNullLimit() {
        assertNotNull(sepStrategy);
        List<String> values = sepStrategy.separateValue("foo bar blah", " ", null);
        assertNotNull(values);
        assertEquals(new Integer(3), new Integer(values.size()));
        assertEquals("foo", values.get(0));
        assertEquals("bar", values.get(1));
        assertEquals("blah", values.get(2));
    }

}
