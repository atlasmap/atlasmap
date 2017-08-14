/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.converters;

import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.converters.CharacterConverter;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class CharacterConverterTest {

    private AtlasPrimitiveConverter<Character> converter = new CharacterConverter();

    @Test
    public void convertToBoolean() throws Exception {
        Character c = Character.forDigit(1, 10);
        Boolean t = converter.convertToBoolean(c);
        assertNotNull(t);
        assertTrue(t);
        c = "T".charAt(0);
        Boolean t2 = converter.convertToBoolean(c);
        assertNotNull(t2);
        assertTrue(t2);
        c = "t".charAt(0);
        Boolean t3 = converter.convertToBoolean(c);
        assertNotNull(t3);
        assertTrue(t3);

        c = Character.forDigit(0, 10);
        Boolean f = converter.convertToBoolean(c);
        assertNotNull(f);
        assertFalse(f);
        c = "F".charAt(0);
        Boolean f2 = converter.convertToBoolean(c);
        assertNotNull(f2);
        assertFalse(f2);
        c = "f".charAt(0);
        Boolean f3 = converter.convertToBoolean(c);
        assertNotNull(f3);
        assertFalse(f3);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToBoolean_Null() throws Exception {
        Character c = "q".charAt(0);
        converter.convertToBoolean(c);
    }

    @Test
    public void convertToBoolean_Invalid() throws Exception {
        Character c = null;
        converter.convertToBoolean(c);
        assertNull(c);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToByte() throws Exception {
        Character c = "T".charAt(0);
        converter.convertToByte(c);
    }

    @Test
    public void convertToByte_Null() throws Exception {
        assertNull(converter.convertToByte(null));
    }

    @Test
    public void convertToCharacter() throws Exception {
        Character c = (char) 0;
        Character c2 = converter.convertToCharacter(c);
        assertNotNull(c2);
        assertNotSame(c, c2);
        assertEquals(c, c2);
    }

    @Test
    public void convertToCharacter_Null() throws Exception {
        Character c = null;
        Character c2 = converter.convertToCharacter(c);
        assertNull(c2);
    }

    @Test
    public void convertToDouble() throws Exception {
        Character c = Character.forDigit(0, 10);
        Double d = converter.convertToDouble(c);
        assertNotNull(d);
        assertEquals(48.0, d, 0.0);

        c = '\uFFFF';
        d = converter.convertToDouble(c);
        assertNotNull(d);
        assertEquals(65535.0, d, 0.0);
    }

    @Test
    public void convertToDouble_Null() throws Exception {
        Character c = null;
        Double d = converter.convertToDouble(c);
        assertNull(d);
    }

    @Test
    public void convertToFloat() throws Exception {
        Character c = Character.forDigit(0, 10);
        Float f = converter.convertToFloat(c);
        assertNotNull(f);
        assertEquals(48.00, f, 0.00);

        c = '\uFFFF';
        f = converter.convertToFloat(c);
        assertNotNull(f);
        assertEquals(65535.0, f, 0.00);
    }

    @Test
    public void convertToFloat_Null() throws Exception {
        Character c = null;
        Float f = converter.convertToFloat(c);
        assertNull(f);
    }

    @Test
    public void convertToInteger() throws Exception {
        Character c = Character.forDigit(0, 10);
        Integer i = converter.convertToInteger(c);
        assertNotNull(i);
        assertEquals(48, i, 0.00);

        c = '\uFFFF';
        i = converter.convertToInteger(c);
        assertNotNull(i);
        assertEquals(65535, i, 0.00);

    }

    @Test
    public void convertToInteger_Null() throws Exception {
        Character c = null;
        Integer i = converter.convertToInteger(c);
        assertNull(i);
    }

    @Test
    public void convertToLong() throws Exception {

        Character c = Character.forDigit(0, 10);
        Long l = converter.convertToLong(c);
        assertNotNull(l);
        assertEquals(48, l, 0.00);

        c = '\uFFFF';
        l = converter.convertToLong(c);
        assertNotNull(l);
        assertEquals(65535, l, 0.00);

    }

    @Test
    public void convertToLong_Null() throws Exception {
        Character c = null;
        Long l = converter.convertToLong(null);
        assertNull(l);
    }

    @Test
    public void convertToShort() throws Exception {
        Character c = Character.forDigit(0, 10);
        Short s = converter.convertToShort(c);
        assertNotNull(s);
        assertEquals(48.0, s, 0.00);
    }

    @Test
    public void convertToShort_Null() throws Exception {
        Character c = null;
        Short s = converter.convertToShort(c);
        assertNull(s);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToShort_Exception() throws Exception {
        Character c = Character.MAX_VALUE;
        Short s = converter.convertToShort(c);
    }

    @Test
    public void convertToString() throws Exception {
        Character c = Character.forDigit(0, 10);
        String s = converter.convertToString(c);
        assertNotNull(s);
        assertEquals("0", s);
    }

    @Test
    public void convertToString_Null() throws Exception {
        Character c = null;
        String s = converter.convertToString(c);
        assertNull(s);
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class aClass = CharacterConverter.class;
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            if (method.isSynthetic()) {
                // We are running in Eclipse or jacoco
                continue;
            }
            if (method.getName().startsWith("convert")) {
                Annotation[] annotations = method.getDeclaredAnnotations();
                assertNotNull(annotations);
                assertTrue(annotations.length > 0);
                for (Annotation annotation : annotations) {
                    assertTrue(AtlasConversionInfo.class.isAssignableFrom(annotation.annotationType()));
                    AtlasConversionInfo atlasConversionInfo = (AtlasConversionInfo) annotation;
                    assertNotNull(atlasConversionInfo.sourceType());
                    assertTrue(atlasConversionInfo.sourceType().compareTo(FieldType.CHAR) == 0);
                    assertNotNull(atlasConversionInfo.targetType());
                    for (AtlasConversionConcern atlasConversionConcern : atlasConversionInfo.concerns()) {
                        assertNotNull(atlasConversionConcern.getMessage());
                        assertNotNull(atlasConversionConcern.value());
                    }
                }
            }
        }
    }

}