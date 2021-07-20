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
package io.atlasmap.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class IntegerConverterTest {

    private IntegerConverter converter = new IntegerConverter();

    @Test
    public void convertToBoolean() {
        int xTrue = 1;
        int xFalse = 0;

        Boolean out = converter.toBoolean(xTrue);
        assertNotNull(out);
        assertTrue(out);
        out = converter.toBoolean(xFalse);
        assertNotNull(out);
        assertFalse(out);
    }

    @Test
    public void convertToBooleanNull() {
        Boolean out = converter.toBoolean(null);
        assertNull(out);
    }

    @Test
    public void convertToBooleanHigh() {
        Boolean out = converter.toBoolean(10);
        assertTrue(out);
    }

    @Test
    public void convertToByte() throws Exception {
        Byte value = (byte) 100;
        assertEquals(value, converter.toByte(100));
    }

    @Test
    public void convertToByteOutOfRange() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            converter.toByte(Integer.MAX_VALUE);
        });
    }

    @Test
    public void convertToByteNull() throws Exception {
        Byte byt = converter.toByte(null);
        assertNull(byt);
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        Character character = converter.toCharacter(null);
        assertNull(character);
    }

    @Test
    public void convertToCharacter() throws Exception {
        Integer integer = Integer.valueOf(4);
        Character character = converter.toCharacter(integer);
        assertNotNull(character);
        int revert = character.charValue();
        assertEquals(integer, Integer.valueOf(revert));
    }

    @Test
    public void convertToCharacterGreaterThanMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            Integer integer = 1500000;
            converter.toCharacter(integer);
        });
    }

    @Test
    public void convertToCharacterLessThanMIN() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            Integer integer = -1500000;
            converter.toCharacter(integer);
        });
    }

    @Test
    public void convertToDouble() {
        Integer integer = 0;
        Double d = converter.toDouble(integer);
        assertNotNull(d);
        assertEquals(d, 0.0, 0.0);

        integer = 1;
        d = converter.toDouble(integer);
        assertNotNull(d);
        assertEquals(1.0, d, 0.0);
    }

    @Test
    public void convertToDoubleNull() {
        assertNull(converter.toDouble(null));
    }

    @Test
    public void convertToFloat() {
        Integer integer = 0;
        Float f = converter.toFloat(integer);
        assertNotNull(f);
        assertEquals(f, 0.0, 0.0);

        integer = 1;
        f = converter.toFloat(integer);
        assertNotNull(f);
        assertEquals(1.0, f, 0.0);
    }

    @Test
    public void convertToFloatNull() {
        assertNull(converter.toFloat(null));
    }

    @Test
    public void convertToShort() throws Exception {
        int i = Short.MAX_VALUE;
        int negI = Short.MIN_VALUE;

        Short out = converter.toShort(i);
        assertNotNull(out);
        assertEquals(i, out.intValue());
        out = converter.toShort(negI);
        assertNotNull(out);
        assertEquals(negI, out.intValue());
    }

    @Test
    public void convertToShortNull() throws Exception {
        Short out = converter.toShort(null);
        assertNull(out);
    }

    @Test
    public void convertToShortConvertExceptionGreaterThanMax() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            converter.toShort(Integer.MAX_VALUE);
        });
    }

    @Test
    public void convertToShortConvertExceptionLessThanMin() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            converter.toShort(Integer.MIN_VALUE);
        });
    }

    @Test
    public void convertToLong() {
        int i = Integer.MAX_VALUE;
        int negI = Integer.MIN_VALUE;

        Long out = converter.toLong(i);
        assertNotNull(out);
        assertEquals(i, out.intValue());
        out = converter.toLong(negI);
        assertNotNull(out);
        assertEquals(negI, out.intValue());
    }

    @Test
    public void convertToLongNull() {
        Long out = converter.toLong(null);
        assertNull(out);
    }

    @Test
    public void convertToString() {
        int i = Integer.MAX_VALUE;
        int negI = Integer.MIN_VALUE;

        String out = converter.toString(i);
        assertNotNull(out);
        assertEquals(Integer.toString(i), out);
        out = converter.toString(negI);
        assertNotNull(out);
        assertEquals(Integer.toString(negI), out);
    }

    @Test
    public void convertToStringNull() {
        String out = converter.toString(null);
        assertNull(out);
    }

    @Test
    public void convertToInteger() {
        Integer foo = Integer.MAX_VALUE;
        Integer out = converter.toInteger(foo);
        foo++;
        assertNotNull(out);
        // test that a copy was made
        assertNotSame(out, foo);
        assertNotEquals(out, foo);
    }

    @Test
    public void convertToIntegerNull() {
        Integer out = converter.toInteger(null);
        assertNull(out);
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class<?> aClass = IntegerConverter.class;
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
                    assertTrue(atlasConversionInfo.sourceType().compareTo(FieldType.INTEGER) == 0);
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
        int i = 4;
        char ch = Character.forDigit(i, intTen);
        assertTrue(ch == '4');
        int i2 = Character.digit(ch, intTen);
        assertTrue(i2 == 4);
    }

}
