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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Test;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class CharacterConverterTest {

    private CharacterConverter converter = new CharacterConverter();

    @Test
    public void convertToBoolean() throws Exception {
        Character c = Character.forDigit(1, 10);
        Boolean t = converter.toBoolean(c, null, null);
        assertNotNull(t);
        assertTrue(t);
        c = "T".charAt(0);
        Boolean t2 = converter.toBoolean(c, null, null);
        assertNotNull(t2);
        assertTrue(t2);
        c = "t".charAt(0);
        Boolean t3 = converter.toBoolean(c, null, null);
        assertNotNull(t3);
        assertTrue(t3);

        c = Character.forDigit(0, 10);
        Boolean f = converter.toBoolean(c, null, null);
        assertNotNull(f);
        assertFalse(f);
        c = "F".charAt(0);
        Boolean f2 = converter.toBoolean(c, null, null);
        assertNotNull(f2);
        assertFalse(f2);
        c = "f".charAt(0);
        Boolean f3 = converter.toBoolean(c, null, null);
        assertNotNull(f3);
        assertFalse(f3);
    }

    @Test
    public void convertToBooleanInvalid() throws Exception {
        Character c = null;
        converter.toBoolean(c, null, null);
        assertNull(c);
    }

    @Test
    public void convertToByte() throws Exception {
        byte value = 99;
        assertEquals(value, converter.toByte('c').byteValue());
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToByteOutOfRange() throws Exception {
        byte value = 99;
        assertEquals(value, converter.toByte('\uD840').byteValue());
    }

    @Test
    public void convertToByteNull() throws Exception {
        assertNull(converter.toByte(null));
    }

    @Test
    public void convertToCharacter() throws Exception {
        Character c = (char) 0;
        Character c2 = converter.toCharacter(c);
        assertNotNull(c2);
        assertNotSame(c, c2);
        assertEquals(c, c2);
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        Character c = null;
        Character c2 = converter.toCharacter(c);
        assertNull(c2);
    }

    @Test
    public void convertToDouble() throws Exception {
        Character c = Character.forDigit(0, 10);
        Double d = converter.toDouble(c);
        assertNotNull(d);
        assertEquals(48.0, d, 0.0);

        c = '\uFFFF';
        d = converter.toDouble(c);
        assertNotNull(d);
        assertEquals(65535.0, d, 0.0);
    }

    @Test
    public void convertToDoubleNull() throws Exception {
        Character c = null;
        Double d = converter.toDouble(c);
        assertNull(d);
    }

    @Test
    public void convertToFloat() throws Exception {
        Character c = Character.forDigit(0, 10);
        Float f = converter.toFloat(c);
        assertNotNull(f);
        assertEquals(48.00, f, 0.00);

        c = '\uFFFF';
        f = converter.toFloat(c);
        assertNotNull(f);
        assertEquals(65535.0, f, 0.00);
    }

    @Test
    public void convertToFloatNull() throws Exception {
        Character c = null;
        Float f = converter.toFloat(c);
        assertNull(f);
    }

    @Test
    public void convertToInteger() throws Exception {
        Character c = Character.forDigit(0, 10);
        Integer i = converter.toInteger(c);
        assertNotNull(i);
        assertEquals(48, i, 0.00);

        c = '\uFFFF';
        i = converter.toInteger(c);
        assertNotNull(i);
        assertEquals(65535, i, 0.00);

    }

    @Test
    public void convertToIntegerNull() throws Exception {
        Character c = null;
        Integer i = converter.toInteger(c);
        assertNull(i);
    }

    @Test
    public void convertToLong() throws Exception {

        Character c = Character.forDigit(0, 10);
        Long l = converter.toLong(c);
        assertNotNull(l);
        assertEquals(48, l, 0.00);

        c = '\uFFFF';
        l = converter.toLong(c);
        assertNotNull(l);
        assertEquals(65535, l, 0.00);

    }

    @Test
    public void convertToLongNull() throws Exception {
        Long l = converter.toLong(null);
        assertNull(l);
    }

    @Test
    public void convertToShort() throws Exception {
        Character c = Character.forDigit(0, 10);
        Short s = converter.toShort(c);
        assertNotNull(s);
        assertEquals(48.0, s, 0.00);
    }

    @Test
    public void convertToShortNull() throws Exception {
        Character c = null;
        Short s = converter.toShort(c);
        assertNull(s);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToShortException() throws Exception {
        Character c = Character.MAX_VALUE;
        converter.toShort(c);
    }

    @Test
    public void convertToString() throws Exception {
        Character c = Character.forDigit(0, 10);
        String s = converter.toString(c, null, null);
        assertNotNull(s);
        assertEquals("0", s);
    }

    @Test
    public void convertToStringNull() throws Exception {
        Character c = null;
        String s = converter.toString(c, null, null);
        assertNull(s);
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class<?> aClass = CharacterConverter.class;
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
                        assertNotNull(atlasConversionConcern.getMessage(atlasConversionInfo));
                        assertNotNull(atlasConversionConcern.value());
                    }
                }
            }
        }
    }

}
