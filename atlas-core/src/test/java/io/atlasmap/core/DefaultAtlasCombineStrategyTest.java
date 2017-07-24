package io.atlasmap.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultAtlasCombineStrategyTest {

    private DefaultAtlasCombineStrategy combine = null;
    
    @Before
    public void setUp() throws Exception {
        combine = new DefaultAtlasCombineStrategy();
    }

    @After
    public void tearDown() throws Exception {
        combine = null;
    }

    @Test
    public void testGetSetDelimiter() {
        assertNotNull(combine);
        assertNotNull(combine.getDelimiter());
        assertEquals(DefaultAtlasCombineStrategy.DEFAULT_COMBINE_DELIMITER, combine.getDelimiter());

        combine.setDelimiter(":");
        assertEquals(":", combine.getDelimiter());
        String value = combine.combineValues(Arrays.asList("a", "b", "c", "d"));
        assertNotNull(value);
        assertEquals("a:b:c:d", value);
    }
    
    @Test
    public void testGetSetLimit() {
        assertNotNull(combine);
        assertNotNull(combine.getDelimiter());
        assertEquals(DefaultAtlasCombineStrategy.DEFAULT_COMBINE_LIMIT, combine.getLimit());
                
        combine.setLimit(2);
        String value = combine.combineValues(Arrays.asList("a", "b", "c", "d"));
        assertNotNull(value);
        assertEquals("a b", value);
    }
    
    @Test
    public void testGetSetAutoTrim() {
        assertNotNull(combine);
        assertFalse(combine.isDisableAutoTrim());
        combine.setDisableAutoTrim(true);
        assertTrue(combine.isDisableAutoTrim());
        
        String value = combine.combineValues(Arrays.asList("a ", "b"));
        assertNotNull(value);
        assertEquals("a  b", value);
    }
    
    @Test
    public void testDisableAutoTrimNullDelimiter() {
        assertNotNull(combine);
        assertFalse(combine.isDisableAutoTrim());
        combine.setDisableAutoTrim(true);
        assertTrue(combine.isDisableAutoTrim());
        
        String value = combine.combineValues(Arrays.asList("a ", "b", "  c"), null);
        assertNotNull(value);
        assertEquals("a  b   c", value);
    }

    @Test
    public void testCombineSingleValue() {
        assertNotNull(combine);
        String value = combine.combineValues(Arrays.asList("a"));
        assertNotNull(value);
        assertEquals("a", value);
    }
    
    @Test
    public void testCombineValues() {
        assertNotNull(combine);
        String value = combine.combineValues(Arrays.asList("a", "b", "c", "d", "e", "f"));
        assertNotNull(value);
        assertEquals("a b c d e f", value);
    }
    
    @Test
    public void testCombineValuesDelimiter() {
        assertNotNull(combine);
        String value = combine.combineValues(Arrays.asList("a", "b", "c", "d", "e", "f"), "1");
        assertNotNull(value);
        assertEquals("a1b1c1d1e1f", value);
    }
    
    @Test
    public void testCombineValuesDelimiterLimit() {
        assertNotNull(combine);
        String value = combine.combineValues(Arrays.asList("a", "b", "c", "d", "e", "f"), "1", 3);
        assertNotNull(value);
        assertEquals("a1b1c", value);
    }
    
    @Test
    public void testCombineValuesNullDelimiter() {
        assertNotNull(combine);
        combine.setDelimiter(null);
        assertNull(combine.getDelimiter());
        
        String value = combine.combineValues(Arrays.asList("a", "b", "c", "d", "e", "f"));
        assertNotNull(value);
        assertEquals("a b c d e f", value);
    }
    
    @Test
    public void testCombineValuesNullLimit() {
        assertNotNull(combine);
        combine.setLimit(null);
        assertNull(combine.getLimit());
        
        String value = combine.combineValues(Arrays.asList("a", "b", "c", "d", "e", "f"));
        assertNotNull(value);
        assertEquals("a b c d e f", value);
    }
    
    @Test
    public void testSeparateValuesNullValue() {
        assertNotNull(combine);
        
        String value = combine.combineValues(null);
        assertNull(value);
    }

    @Test
    public void testCombineValuesEmptyValue() {
        assertNotNull(combine);
        String value = combine.combineValues(new ArrayList<String>());
        assertNull(value);
    }
}
