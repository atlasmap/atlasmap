/**
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

public class ShortConverterTest {
    private ShortConverter converter = new ShortConverter();

    @Test
    public void convertToBoolean() {
        Short f = 0;
        Short t = 1;

        Boolean b = converter.toBoolean(t);
        assertNotNull(b);
        assertTrue(b);

        b = converter.toBoolean(f);
        assertNotNull(b);
        assertFalse(b);

    }

    @Test
    public void convertToBooleanNull() {
        Short l = null;
        Boolean b = converter.toBoolean(l);
        assertNull(b);
    }

    @Test
    public void convertToBooleanNegative() {
        Short dt = -1;
        Boolean b = converter.toBoolean(dt);
        assertTrue(b);
    }

    @Test
    public void convertToByte() throws Exception {
        Short l = 0;
        Byte value = (byte) 0;
        assertEquals(value, converter.toByte(l));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToByteOutOfRange() throws Exception {
        converter.toByte(Short.MAX_VALUE);
    }

    @Test
    public void convertToByteNull() throws Exception {
        assertNull(converter.toByte(null));
    }

    @Test
    public void convertToCharacter() throws Exception {
        Short shorty = new Short((short) 4);
        Character character = converter.toCharacter(shorty);
        assertNotNull(character);
        assertEquals(Character.valueOf((char) shorty.shortValue()), character);
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        Short s = null;
        Character c = converter.toCharacter(s);
        assertNull(c);
    }

    @Test
    public void convertToCharacterMAX() throws Exception {
        Short s = Short.MAX_VALUE;
        Character c = converter.toCharacter(s);
        assertNotNull(c);
        assertEquals(Character.valueOf((char) s.shortValue()), c);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacterMIN() throws Exception {
        Short s = Short.MIN_VALUE;
        converter.toCharacter(s);
    }

    @Test
    public void convertToDouble() {
        Short s = 0;
        Double d = converter.toDouble(s);
        assertNotNull(d);
        assertEquals(0.0, d, 0.0);
    }

    @Test
    public void convertToDoubleNull() {
        Short s = null;
        Double d = converter.toDouble(s);
        assertNull(d);
    }

    @Test
    public void convertToDoubleMAX() {
        Short s = Short.MAX_VALUE;
        Double d = converter.toDouble(s);
        assertNotNull(d);
        assertEquals(Short.MAX_VALUE, s, 0.0);
    }

    @Test
    public void convertToFloat() {
        Short s = 0;
        Float f = converter.toFloat(s);
        assertNotNull(f);
        assertEquals(0.0f, f, 0.0);
    }

    @Test
    public void convertToFloatNull() {
        assertNull(converter.toFloat(null));
    }

    @Test
    public void convertToFloatMAX() {
        Short s = Short.MAX_VALUE;
        Float f = converter.toFloat(s);
        assertNotNull(f);
        assertEquals(Short.MAX_VALUE, s, 0.0);
    }

    @Test
    public void convertToInteger() {
        Short s = 0;
        Integer i = converter.toInteger(s);
        assertNotNull(i);
        assertEquals(0, i, 0.0);
    }

    @Test
    public void convertToIntegerNull() {
        Short l = null;
        Integer i = converter.toInteger(l);
        assertNull(i);
    }

    @Test
    public void convertToLong() {
        Short s = 1;
        Long l = converter.toLong(s);
        assertNotNull(l);
        assertEquals(1, l, 0.0);
    }

    @Test
    public void convertToLongNull() {
        Short s = null;
        Long l = converter.toLong(s);
        assertNull(l);
    }

    @Test
    public void convertToLongMAX() {
        Short s = Short.MAX_VALUE;
        Long l = converter.toLong(s);
        assertNotNull(l);
        assertEquals(32767.0, l, 0.0);
    }

    @Test
    public void convertToLongMIN() {
        Short s = Short.MIN_VALUE;
        Long l = converter.toLong(s);
        assertNotNull(l);
        assertEquals(-32768.0, l, 0.0);
    }

    @Test
    public void convertToShort() {
        Short aShort = 0;
        Short s = converter.toShort(aShort);
        assertNotNull(s);
        assertNotSame(aShort, s);
        assertEquals(0, s, 0.0);
    }

    @Test
    public void convertToShortNull() {
        Short l = null;
        Short s = converter.toShort(l);
        assertNull(s);
    }

    @Test
    public void convertToString() {
        Short l = 0;
        String s = converter.toString(l);
        assertNotNull(s);
        assertTrue("0".equals(s));
    }

    @Test
    public void convertToStringNull() {
        Short l = null;
        String s = converter.toString(l);
        assertNull(s);
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class<?> aClass = ShortConverter.class;
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
                    assertTrue(atlasConversionInfo.sourceType().compareTo(FieldType.SHORT) == 0);
                    assertNotNull(atlasConversionInfo.targetType());
                    for (AtlasConversionConcern atlasConversionConcern : atlasConversionInfo.concerns()) {
                        assertNotNull(atlasConversionConcern.getMessage(atlasConversionInfo));
                        assertNotNull(atlasConversionConcern.value());
                    }
                }
            }
        }
    }

    @Test
    public void testCharacterDigit() {
        int intTen = 10;
        char ch = Character.valueOf((char) Short.MAX_VALUE);

        Character.digit(ch, intTen);

    }
}
