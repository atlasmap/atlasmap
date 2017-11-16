package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.spi.AtlasCombineStrategy;

public class DefaultAtlasCombineStrategyTest {

    private AtlasCombineStrategy combine = null;

    @Before
    public void setUp() throws Exception {
        combine = new DefaultAtlasCombineStrategy();
    }

    @After
    public void tearDown() throws Exception {
        combine = null;
    }

    @Test
    public void testGetName() {
        assertNotNull(combine);
        assertEquals("DefaultAtlasCombineStrategy", combine.getName());
    }

    protected Map<Integer, String> generateCombineMap(int count) {
        String combines = "abcdefghijklmnopqrstuvwxyz";
        Map<Integer, String> cMap = new HashMap<Integer, String>();
        for (int i = 0; i < count; i++) {
            cMap.put(i, combines.substring(i, i + 1));
        }
        return cMap;
    }

    @Test
    public void testGetSetDelimiter() {
        assertNotNull(combine);
        assertNotNull(combine.getDelimiter());
        assertEquals(DefaultAtlasCombineStrategy.DEFAULT_COMBINE_DELIMITER, combine.getDelimiter());

        combine.setDelimiter(":");
        assertEquals(":", combine.getDelimiter());
        String value = combine.combineValues(generateCombineMap(4));
        assertNotNull(value);
        assertEquals("a:b:c:d", value);
    }

    @Test
    public void testGetSetLimit() {
        assertNotNull(combine);
        assertNotNull(combine.getDelimiter());
        assertEquals(DefaultAtlasCombineStrategy.DEFAULT_COMBINE_LIMIT, combine.getLimit());

        combine.setLimit(2);
        String value = combine.combineValues(generateCombineMap(4));
        assertNotNull(value);
        assertEquals("a b", value);
    }

    @Test
    public void testGetSetAutoTrim() {
        assertNotNull(combine);
        assertFalse(((DefaultAtlasCombineStrategy) combine).isDisableAutoTrim());
        ((DefaultAtlasCombineStrategy) combine).setDisableAutoTrim(true);
        assertTrue(((DefaultAtlasCombineStrategy) combine).isDisableAutoTrim());
        Map<Integer, String> cMap = generateCombineMap(2);
        cMap.put(0, cMap.get(0) + " ");
        String value = combine.combineValues(cMap);
        assertNotNull(value);
        assertEquals("a  b", value);
    }

    @Test
    public void testDisableAutoTrimNullDelimiter() {
        assertNotNull(combine);
        assertFalse(((DefaultAtlasCombineStrategy) combine).isDisableAutoTrim());
        ((DefaultAtlasCombineStrategy) combine).setDisableAutoTrim(true);
        assertTrue(((DefaultAtlasCombineStrategy) combine).isDisableAutoTrim());
        Map<Integer, String> cMap = generateCombineMap(3);
        cMap.put(0, cMap.get(0) + " ");
        cMap.put(2, "  " + cMap.get(2));
        String value = combine.combineValues(cMap, null);
        assertNotNull(value);
        assertEquals("a  b   c", value);
    }

    @Test
    public void testCombineSingleValue() {
        assertNotNull(combine);
        String value = combine.combineValues(generateCombineMap(1));
        assertNotNull(value);
        assertEquals("a", value);
    }

    @Test
    public void testCombineValues() {
        assertNotNull(combine);
        String value = combine.combineValues(generateCombineMap(6));
        assertNotNull(value);
        assertEquals("a b c d e f", value);
    }

    @Test
    public void testCombineValuesDelimiter() {
        assertNotNull(combine);
        String value = combine.combineValues(generateCombineMap(6), "1");
        assertNotNull(value);
        assertEquals("a1b1c1d1e1f", value);
    }

    @Test
    public void testCombineValuesDelimiterLimit() {
        assertNotNull(combine);
        String value = combine.combineValues(generateCombineMap(6), "1", 3);
        assertNotNull(value);
        assertEquals("a1b1c", value);
    }

    @Test
    public void testCombineValuesNullDelimiter() {
        assertNotNull(combine);
        combine.setDelimiter(null);
        assertNull(combine.getDelimiter());

        String value = combine.combineValues(generateCombineMap(6));
        assertNotNull(value);
        assertEquals("a b c d e f", value);
    }

    @Test
    public void testCombineValuesNullLimit() {
        assertNotNull(combine);
        combine.setLimit(null);
        assertNull(combine.getLimit());

        String value = combine.combineValues(generateCombineMap(6));
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
        String value = combine.combineValues(new HashMap<Integer, String>());
        assertNull(value);
    }

    @Test
    public void testSortByKey() {
        Map<Integer, String> cMap = DefaultAtlasCombineStrategy.sortByKey(generateCombineMap(4));
        assertEquals("a", cMap.get(0));
        assertEquals("b", cMap.get(1));
        assertEquals("c", cMap.get(2));
        assertEquals("d", cMap.get(3));
    }

    @Test
    public void testSortByKeyOutOfOrder() {

        Map<Integer, String> outOfOrder = new HashMap<Integer, String>();
        outOfOrder.put(2, "c");
        outOfOrder.put(0, "a");
        outOfOrder.put(3, "d");
        outOfOrder.put(1, "b");

        Map<Integer, String> cMap = DefaultAtlasCombineStrategy.sortByKey(outOfOrder);
        int count = 0;
        for (String str : cMap.values()) {
            if (count == 0) {
                assertEquals("a", str);
            }
            if (count == 1) {
                assertEquals("b", str);
            }
            if (count == 2) {
                assertEquals("c", str);
            }
            if (count == 3) {
                assertEquals("d", str);
            }
            count++;
        }
    }

    @Test
    public void testSortByKeyOutOfOrderGaps() {

        Map<Integer, String> outOfOrder = new HashMap<Integer, String>();
        outOfOrder.put(7, "c");
        outOfOrder.put(3, "a");
        outOfOrder.put(99, "d");
        outOfOrder.put(5, "b");

        Map<Integer, String> cMap = DefaultAtlasCombineStrategy.sortByKey(outOfOrder);
        int count = 0;
        for (String str : cMap.values()) {
            if (count == 0) {
                assertEquals("a", str);
            }
            if (count == 1) {
                assertEquals("b", str);
            }
            if (count == 2) {
                assertEquals("c", str);
            }
            if (count == 3) {
                assertEquals("d", str);
            }
            count++;
        }
    }

    @Test
    public void testSortByKeyWithNullKey() {
        Map<Integer, String> generatedCombineMap = generateCombineMap(4);
        generatedCombineMap.put(null, "valueWithNullKey");
        Map<Integer, String> cMap = DefaultAtlasCombineStrategy.sortByKey(generatedCombineMap);
        assertEquals("a", cMap.get(0));
        assertEquals("b", cMap.get(1));
        assertEquals("c", cMap.get(2));
        assertEquals("d", cMap.get(3));
        assertEquals("valueWithNullKey", cMap.get(null));
    }

    @Test
    public void testSortByKeyWithNullValue() {
        Map<Integer, String> generatedCombineMap = generateCombineMap(4);
        generatedCombineMap.put(Integer.valueOf(4), null);
        generatedCombineMap.put(Integer.valueOf(5), "f");
        Map<Integer, String> cMap = DefaultAtlasCombineStrategy.sortByKey(generatedCombineMap);
        assertEquals("a", cMap.get(0));
        assertEquals("b", cMap.get(1));
        assertEquals("c", cMap.get(2));
        assertEquals("d", cMap.get(3));
        assertEquals(null, cMap.get(4));
        assertEquals("f", cMap.get(5));
    }
}
