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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class CharSequenceConverterTest {
    private CharSequenceConverter converter = new CharSequenceConverter();

    @Test
    public void convertToBoolean() {
        String f = "0";
        String t = "1";
        String capitalT = "T";
        String capitalF = "F";
        String smallT = "t";
        String smallF = "f";

        String strTrue = "true";
        String strFalse = "false";

        Boolean b = converter.toBoolean(t, null, null);
        assertNotNull(b);
        assertTrue(b);

        b = converter.toBoolean(f, null, null);
        assertNotNull(b);
        assertFalse(b);

        b = converter.toBoolean(capitalT, null, null);
        assertNotNull(b);
        assertTrue(b);

        b = converter.toBoolean(capitalF, null, null);
        assertNotNull(b);
        assertFalse(b);

        b = converter.toBoolean(smallT, null, null);
        assertNotNull(b);
        assertTrue(b);

        b = converter.toBoolean(smallF, null, null);
        assertNotNull(b);
        assertFalse(b);

        b = converter.toBoolean(strTrue, null, null);
        assertNotNull(b);
        assertTrue(b);

        b = converter.toBoolean(strFalse, null, null);
        assertNotNull(b);
        assertFalse(b);

    }

    @Test
    public void convertToBooleanNull() {
        Boolean b = converter.toBoolean(null, null, null);
        assertNull(b);
    }

    @Test
    public void convertToBooleanFallback() {
        String s = "";
        Boolean b = converter.toBoolean(s, null, null);
        assertFalse(b);

        b = converter.toBoolean("junk", null, null);
        assertFalse(b);
    }

    @Test
    public void convertToByte() throws Exception {
        String s = "0";
        Byte value = (byte) 0;
        assertEquals(value, converter.toByte(s));

        s = "+127";
        value = (byte) 127;
        assertEquals(value, converter.toByte(s));

        s = "-128";
        value = (byte) -128;
        assertEquals(value, converter.toByte(s));

    }

    @Test
    public void convertToByteNull() throws Exception {
        assertNull(converter.toByte(null));
    }

    @Test
    public void convertToByteMAX() throws Exception {
        String s = String.valueOf(Byte.MAX_VALUE);
        Byte l = converter.toByte(s);
        assertNotNull(l);
        assertEquals(Byte.MAX_VALUE, l, 0.0);
    }

    @Test
    public void convertToByteMIN() throws Exception {
        String s = String.valueOf(Byte.MIN_VALUE);
        Byte l = converter.toByte(s);
        assertNotNull(l);
        assertEquals(Byte.MIN_VALUE, l, 0.0);
    }

    @Test
    public void convertToByteGreaterThanMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "128";
            converter.toByte(s);
        });
    }

    @Test
    public void convertToByteLessThanMIN() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "-129";
            converter.toByte(s);
        });
    }

    @Test
    public void convertToCharacter() throws Exception {
        String s = "0";
        Character c = converter.toCharacter(s);
        assertNotNull(c);
        assertEquals(48, c.charValue());
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        Character c = converter.toCharacter(null);
        assertNull(c);
    }

    @Test
    public void convertToCharacterEmptyString() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "";
            converter.toCharacter(s);
        });
    }

    @Test
    public void convertToCharacterStringSpace() throws Exception {
        String s = " ";
        Character c = converter.toCharacter(s);
        assertNotNull(c);
        assertEquals(32, c.charValue());
    }

    @Test
    public void convertToDate() {
        // assumes a valid ISO 8601 date time string
        Date date = converter.toDate(Instant.now().toString(), null, null);
        assertNotNull(date);
        date = converter.toDate("2014-02-20T20:04:05.867Z", null, null);
        assertNotNull(date);
    }

    @Test
    public void convertToDouble() throws Exception {
        String s = "0.0";
        Double d = converter.toDouble(s);
        assertNotNull(d);
        assertEquals(0.0, d, 0.0);
    }

    @Test
    public void convertToDoubleNull() throws Exception {
        Double d = converter.toDouble(null);
        assertNull(d);
    }

    @Test
    public void convertToDoubleMAX() throws Exception {
        String s = String.valueOf(Double.MAX_VALUE);
        Double d = converter.toDouble(s);
        assertNotNull(d);
        assertEquals(Double.MAX_VALUE, d, 0.0);
    }

    @Test
    public void convertToDoubleGreaterThanMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "1.7976931348623157E400";
            converter.toDouble(s);
        });
    }

    @Test
    public void convertToDoubleLessThanMIN() throws Exception {
        String s = "-4.9E-325";
        Double d = converter.toDouble(s);
        assertNotNull(d);
        assertEquals(0.0, d, 0.0);
    }

    @Test
    public void convertToDoubleUnparseable() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "1.2efff";
            converter.toDouble(s);
        });
    }

    @Test
    public void convertToFloat() throws Exception {
        String s = "0";
        Float f = converter.toFloat(s);
        assertNotNull(f);
        assertEquals(0.0, f, 0.0);
        s = "1";
        f = converter.toFloat(s);
        assertNotNull(f);
        assertEquals(1.0, f, 0.0);
    }

    @Test
    public void convertToFloatNull() throws Exception {
        assertNull(converter.toFloat(null));
    }

    @Test
    public void convertToFloatMAX() throws Exception {
        String s = "3.4028235E38";
        Float f = converter.toFloat(s);
        assertNotNull(f);
        assertEquals(Float.MAX_VALUE, f, 0.0);
    }

    @Test
    public void convertToFloatMIN() throws Exception {
        String s = "1.401298464324817E-45";
        Float f = converter.toFloat(s);
        assertNotNull(f);
        assertEquals(Float.MIN_VALUE, f, 0.0);
    }

    @Test
    public void convertToFloatUnparsable() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "QWERTY";
            converter.toFloat(s);
        });
    }

    @Test
    public void convertToFloatGreaterThanMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "3.4028235E39";
            converter.toFloat(s);
        });
    }

    @Test
    public void convertToFloatLessThanMIN() throws Exception {
        String s = "1.4E-49";
        Float f = converter.toFloat(s);
        assertNotNull(f);
        assertEquals(0.0, f, 0.0);
    }

    @Test
    public void convertToInteger() throws Exception {
        String s = "0";
        Integer i = converter.toInteger(s);
        assertNotNull(i);
        assertEquals(0, i, 0.0);
        s = "3.5";
        i = converter.toInteger(s);
        assertNotNull(i);
        assertEquals(3, i, 0.0);
    }

    @Test
    public void convertToIntegerNull() throws Exception {
        Integer i = converter.toInteger(null);
        assertNull(i);
    }

    @Test
    public void convertToIntegerLessThanMIN() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "-21474836495554545";
            converter.toInteger(s);
        });
    }

    @Test
    public void convertToIntegerGreaterThanMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "214748364755545422145221";
            converter.toInteger(s);
        });
    }

    @Test
    public void convertToIntegerUnparseable() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "2147483648qwerty";
            converter.toInteger(s);
        });
    }

    @Test
    public void convertToLong() throws Exception {
        String s = "1";
        Long l = converter.toLong(s);
        assertNotNull(l);
        assertEquals(1, l, 0.0);
        s = "3.5";
        l = converter.toLong(s);
        assertNotNull(l);
        assertEquals(3, l, 0.0);
    }

    @Test
    public void convertToLongNull() throws Exception {
        Long l = converter.toLong(null);
        assertNull(l);
    }

    @Test
    public void convertToLongMAX() throws Exception {
        String s = String.valueOf(Long.MAX_VALUE);
        Long l = converter.toLong(s);
        assertNotNull(l);
        assertEquals(Long.MAX_VALUE, l, 0.0);
    }

    @Test
    public void convertToLongMIN() throws Exception {
        String s = String.valueOf(Long.MIN_VALUE);
        Long l = converter.toLong(s);
        assertNotNull(l);
        assertEquals(Long.MIN_VALUE, l, 0.0);
    }

    @Test
    public void convertToLongGreaterThanMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "9223372036854775808";
            converter.toLong(s);
        });
    }

    @Test
    public void convertToLongLessThanMIN() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "-9223372036854775809";
            converter.toLong(s);
        });
    }

    @Test
    public void convertToLongUnparsable() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "QWERTY";
            converter.toLong(s);
        });
    }

    @Test
    public void convertToShort() throws Exception {
        String aString = "0";
        Short s = converter.toShort(aString);
        assertNotNull(s);
        assertEquals(0, s, 0.0);
    }

    @Test
    public void convertToShortNull() throws Exception {
        Short s = converter.toShort(null);
        assertNull(s);
    }

    @Test
    public void convertToShortMAX() throws Exception {
        String s = String.valueOf(Short.MAX_VALUE);
        Short l = converter.toShort(s);
        assertNotNull(l);
        assertEquals(Short.MAX_VALUE, l, 0.0);
    }

    @Test
    public void convertToShortMIN() throws Exception {
        String s = String.valueOf(Short.MIN_VALUE);
        Short l = converter.toShort(s);
        assertNotNull(l);
        assertEquals(Short.MIN_VALUE, l, 0.0);
    }

    @Test
    public void convertToShortGreaterThanMAX() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "9223372036854775808";
            converter.toShort(s);
        });
    }

    @Test
    public void convertToShortLessThanMIN() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "-9223372036854775809";
            converter.toShort(s);
        });
    }

    @Test
    public void convertToShortUnparsable() throws Exception {
        assertThrows(AtlasConversionException.class, () -> {
            String s = "QWERTY";
            converter.toShort(s);
        });
    }

    @Test
    public void convertToCharSequence() {
        CharSequence zero = "0";
        CharSequence converted = converter.toCharSequence(zero, null, null);
        assertNotNull(converted);
        assertNotSame(converted, zero);
        assertTrue("0".equals(converted));
    }

    @Test
    public void convertToCharSequenceNull() {
        CharSequence s = converter.toCharSequence(null, null, null);
        assertNull(s);
    }

    @Test
    public void convertToCharBuffer() {
        CharBuffer cb = converter.toCharBuffer("test", null, null);
        assertNotNull(cb);
    }

    @Test
    public void convertToString() {
        String s = converter.toString("test", null, null);
        assertNotNull(s);
    }

    @Test
    public void convertToStringBuffer() {
        StringBuffer sb = converter.toStringBuffer("test", null, null);
        assertNotNull(sb);
    }

    @Test
    public void convertToStringBuilder() {
        StringBuilder sb = converter.toStringBuilder("test", null, null);
        assertNotNull(sb);
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class<?> aClass = CharSequenceConverter.class;
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
                    assertTrue(atlasConversionInfo.sourceType().compareTo(FieldType.STRING) == 0);
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
    public void testConvertToNumber() throws AtlasConversionException {
        CharSequenceConverter converter = new CharSequenceConverter();
        assertNull(converter.toNumber(null));
        assertNull(converter.toNumber(" "));
        assertNotNull(converter.toNumber("1"));
        assertNotNull(converter.toNumber("1.99"));
    }

    @Test
    public void testConvertToNumberAtlasConversionException() throws AtlasConversionException {
        assertThrows(AtlasConversionException.class, () -> {
            CharSequenceConverter converter = new CharSequenceConverter();
            assertNull(converter.toNumber("abc"));
        });
    }
}
