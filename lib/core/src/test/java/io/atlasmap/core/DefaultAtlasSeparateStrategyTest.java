/*
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
package io.atlasmap.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.spi.AtlasSeparateStrategy;
import io.atlasmap.spi.StringDelimiter;

public class DefaultAtlasSeparateStrategyTest {

    private AtlasSeparateStrategy separate = null;

    @BeforeEach
    public void setUp() {
        separate = new DefaultAtlasSeparateStrategy();
    }

    @AfterEach
    public void tearDown() {
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

        separate.setDelimiter(StringDelimiter.COLON);
        assertEquals(StringDelimiter.COLON, separate.getDelimiter());
        List<String> values = separate.separateValue("a:b:c:d");
        assertNotNull(values);
        assertEquals(Integer.valueOf(4), Integer.valueOf(values.size()));
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
        assertEquals(Integer.valueOf(2), Integer.valueOf(values.size()));
        assertEquals("a", values.get(0));
        assertEquals("b c d", values.get(1));
    }

    @Test
    public void testSeparateValue() {
        assertNotNull(separate);
        List<String> values = separate.separateValue("a b c d e f");
        assertNotNull(values);
        assertEquals(Integer.valueOf(6), Integer.valueOf(values.size()));
        assertEquals("a", values.get(0));
        assertEquals("b", values.get(1));
        assertEquals("c", values.get(2));
        assertEquals("d", values.get(3));
        assertEquals("e", values.get(4));
        assertEquals("f", values.get(5));
    }

    @Test
    public void testSeparateValueNullDelimiter() {
        assertNotNull(separate);
        separate.setDelimiter(null);
        assertNull(separate.getDelimiter());

        List<String> values = separate.separateValue("a b c d e f");
        assertNotNull(values);
        assertEquals(Integer.valueOf(6), Integer.valueOf(values.size()));
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
        assertEquals(Integer.valueOf(6), Integer.valueOf(values.size()));
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
