package io.atlasmap.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.spi.AtlasSeparateStrategy;

public class DefaultAtlasSeparateStrategyTest {

    private AtlasSeparateStrategy separate = null;

    @Before
    public void setUp() throws Exception {
        separate = new DefaultAtlasSeparateStrategy();
    }

    @After
    public void tearDown() throws Exception {
        separate = null;
    }

    @Test
    public void testGetName() {
        assertNotNull(separate);
        assertEquals("DefaultAtlasSeparateStrategy", separate.getName());
    }

    @Test
    public void testGetSetDelimiter() {
        assertNotNull(separate);
        assertNotNull(separate.getDelimiter());
        assertEquals(DefaultAtlasSeparateStrategy.DEFAULT_SEPARATE_DELIMITER, separate.getDelimiter());

        separate.setDelimiter(":");
        assertEquals(":", separate.getDelimiter());
        List<String> values = separate.separateValue("a:b:c:d");
        assertNotNull(values);
        assertEquals(new Integer(4), new Integer(values.size()));
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c", values.get(2));
        assertEquals("d", values.get(3));
    }

    @Test
    public void testGetSetLimit() {
        assertNotNull(separate);
        assertNotNull(separate.getDelimiter());
        assertEquals(DefaultAtlasSeparateStrategy.DEFAULT_SEPARATE_LIMIT, separate.getLimit());

        separate.setLimit(2);
        List<String> values = separate.separateValue("a b c d");
        assertNotNull(values);
        assertEquals(new Integer(2), new Integer(values.size()));
        assertEquals("a", values.get(0));
        assertEquals("b c d", values.get(1));
    }

    @Test
    public void testSeparateValue() {
        assertNotNull(separate);
        List<String> values = separate.separateValue("a b c d e f");
        assertNotNull(values);
        assertEquals(new Integer(6), new Integer(values.size()));
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c", values.get(2));
        assertEquals("d", values.get(3));
        assertEquals("e", values.get(4));
        assertEquals("f", values.get(5));
    }

    @Test
    public void testSeparateValueDelimiter() {
        assertNotNull(separate);
        List<String> values = separate.separateValue("a1b1c1d1e1f", "1");
        assertNotNull(values);
        assertEquals(new Integer(6), new Integer(values.size()));
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c", values.get(2));
        assertEquals("d", values.get(3));
        assertEquals("e", values.get(4));
        assertEquals("f", values.get(5));
    }

    @Test
    public void testSeparateValueDelimiterLimit() {
        assertNotNull(separate);
        List<String> values = separate.separateValue("a1b1c1d1e1f", "1", 3);
        assertNotNull(values);
        assertEquals(new Integer(3), new Integer(values.size()));
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c1d1e1f", values.get(2));
    }

    @Test
    public void testSeparateValueNullDelimiter() {
        assertNotNull(separate);
        separate.setDelimiter(null);
        assertNull(separate.getDelimiter());

        List<String> values = separate.separateValue("a b c d e f");
        assertNotNull(values);
        assertEquals(new Integer(6), new Integer(values.size()));
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c", values.get(2));
        assertEquals("d", values.get(3));
        assertEquals("e", values.get(4));
        assertEquals("f", values.get(5));
    }

    @Test
    public void testSeparateValueNullLimit() {
        assertNotNull(separate);
        separate.setLimit(null);
        assertNull(separate.getLimit());

        List<String> values = separate.separateValue("a b c d e f");
        assertNotNull(values);
        assertEquals(new Integer(6), new Integer(values.size()));
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c", values.get(2));
        assertEquals("d", values.get(3));
        assertEquals("e", values.get(4));
        assertEquals("f", values.get(5));
    }

    @Test
    public void testSeparateValueNullValue() {
        assertNotNull(separate);

        List<String> values = separate.separateValue(null);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    public void testSeparateValueEmptyValue() {
        assertNotNull(separate);

        List<String> values = separate.separateValue("");
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }
}
