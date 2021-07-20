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

public class DoubleConverterTest {

    private DoubleConverter converter = new DoubleConverter();

    @Test
    public void convertToBoolean() {
        Double df = 0.0;
        Double dt = 1.0;

        Boolean b = converter.toBoolean(dt);
        assertNotNull(b);
        assertTrue(b);

        b = converter.toBoolean(df);
        assertNotNull(b);
        assertFalse(b);

    }

    @Test
    public void convertToBooleanNull() {
        Double df = null;
        Boolean b = converter.toBoolean(df);
        assertNull(b);
    }

    @Test
    public void convertToBooleanNegative() {
        Double dt = -1.0;
        Boolean b = converter.toBoolean(dt);
        assertTrue(b);
    }

    @Test
    public void convertToByte() throws Exception {
        Double df = 0.0;
        Byte value = (byte) 0;
        assertEquals(value, converter.toByte(df));
    }

    @Test
    public void convertToByteOutOfRange() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            converter.toByte(Double.MAX_VALUE);
        });
    }

    @Test
    public void convertToByteNull() throws Exception {
        assertNull(converter.toByte(null));
    }

    @Test
    public void convertToCharacter() throws Exception {
        Double df = 0.0;
        Character c = converter.toCharacter(df);
        assertNotNull(c);
        assertEquals(0, c.charValue());
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        Double df = null;
        Character c = converter.toCharacter(df);
        assertNull(c);
    }

    @Test
    public void convertToCharacterMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            Double df = 65556.00;
            converter.toCharacter(df);
        });
    }

    @Test
    public void convertToCharacterMIN() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            Double df = -1.00;
            converter.toCharacter(df);
        });
    }

    @Test
    public void convertToDouble() {
        Double df = 0.0;
        Double d = converter.toDouble(df);
        assertNotNull(d);
        assertNotSame(df, d);
        assertEquals(0.0, d, 0.0);
    }

    @Test
    public void convertToDoubleNull() {
        Double df = null;
        Double d = converter.toDouble(df);
        assertNull(d);
    }

    @Test
    public void convertToFloat() throws Exception {
        Double df = 0.0d;
        Float f = converter.toFloat(df);
        assertNotNull(f);
        assertEquals(0.0, f, 0.0);

        df = 0.15;
        f = converter.toFloat(df);
        assertNotNull(f);
        assertEquals(0.15, f, 1.0);
    }

    @Test
    public void convertToFloatNull() throws Exception {
        Double df = null;
        Float f = converter.toFloat(df);
        assertNull(f);
    }

    @Test
    public void convertToFloatExceptionMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            Double df = Double.MAX_VALUE;
            converter.toFloat(df);
        });
    }

    @Test
    public void convertToFloatExceptionMIN() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            Double df = Double.MIN_VALUE;
            converter.toFloat(df);
        });
    }

    @Test
    public void convertToInteger() throws Exception {
        Double df = 0.15;
        Integer i = converter.toInteger(df);
        assertNotNull(i);
        assertEquals(0, i, 0.0);
    }

    @Test
    public void convertToIntegerNull() throws Exception {
        Double df = null;
        Integer i = converter.toInteger(df);
        assertNull(i);
    }

    @Test
    public void convertToIntegerExceptionMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            Double df = Double.MAX_VALUE;
            converter.toInteger(df);
        });
    }

    @Test
    public void convertToLong() throws Exception {
        Double df = 0.0;
        Long l = converter.toLong(df);
        assertNotNull(l);
        assertEquals(0, l, 0.0);
    }

    @Test
    public void convertToLongNull() throws Exception {
        Double df = null;
        Long l = converter.toLong(df);
        assertNull(l);
    }

    @Test
    public void convertToLongExceptionMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            Double df = Double.MAX_VALUE;
            converter.toLong(df);
        });
    }

    @Test
    public void convertToShort() throws Exception {
        Double df = 0.0;
        Short s = converter.toShort(df);
        assertNotNull(s);
        assertEquals(0, s, 0.0);
    }

    @Test
    public void convertToShortNull() throws Exception {
        Double df = null;
        Short s = converter.toShort(df);
        assertNull(s);
    }

    @Test
    public void convertToShortExceptionMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            Double df = Double.MAX_VALUE;
            converter.toShort(df);
        });
    }

    @Test
    public void convertToString() {
        Double df = 0.0;
        String s = converter.toString(df);
        assertNotNull(s);
        assertTrue("0.0".equals(s));
    }

    @Test
    public void convertToStringNull() {
        Double df = null;
        String s = converter.toString(df);
        assertNull(s);
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class<?> aClass = DoubleConverter.class;
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
                    assertTrue(atlasConversionInfo.sourceType().compareTo(FieldType.DOUBLE) == 0);
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
