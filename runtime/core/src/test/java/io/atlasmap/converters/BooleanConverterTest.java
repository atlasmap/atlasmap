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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Test;

import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class BooleanConverterTest {

    private BooleanConverter converter = new BooleanConverter();

    @Test
    public void convertToBoolean() throws Exception {
        Boolean t = Boolean.TRUE;
        Boolean f = Boolean.FALSE;
        Boolean t2 = converter.toBoolean(t, null, null);
        Boolean f2 = converter.toBoolean(f, null, null);
        assertNotNull(t2);
        assertEquals(t2, t);
        assertTrue(t2);
        assertNotNull(f2);
        assertEquals(f2, f);
        assertFalse(f2);
    }

    @Test
    public void convertToBooleanNull() throws Exception {
        Boolean t = null;
        Boolean t2 = converter.toBoolean(t, null, null);
        assertNull(t2);
    }

    @Test
    public void convertToByte() throws Exception {
        Byte trueValue = (byte) 1;
        assertEquals(trueValue, converter.toByte(Boolean.TRUE));
        Byte falseValue = (byte) 0;
        assertEquals(falseValue, converter.toByte(Boolean.FALSE));
    }

    @Test
    public void convertToByteNull() throws Exception {
        assertNull(converter.toByte(null));
    }

    @Test
    public void convertToCharacter() throws Exception {
        Boolean t = Boolean.TRUE;
        Boolean f = Boolean.FALSE;
        Character c = converter.toCharacter(t);
        assertNotNull(c);
        assertEquals(1, c.charValue());
        c = converter.toCharacter(f);
        assertNotNull(c);
        assertEquals(0, c.charValue());
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        Boolean t = null;
        Character c = converter.toCharacter(t);
        assertNull(c);
    }

    @Test
    public void convertToDouble() throws Exception {
        Boolean t = Boolean.TRUE;
        Boolean f = Boolean.FALSE;
        Double d = converter.toDouble(t);
        assertNotNull(d);
        assertEquals(1, d, 0.0d);
        d = converter.toDouble(f);
        assertNotNull(d);
        assertEquals(0, d, 0.0d);

    }

    @Test
    public void convertToDoubleNull() throws Exception {
        Boolean t = null;
        Double d = converter.toDouble(t);
        assertNull(d);
    }

    @Test
    public void convertToFloat() throws Exception {
        Boolean t = Boolean.TRUE;
        Boolean f = Boolean.FALSE;
        Float aFloat = converter.toFloat(t);
        assertNotNull(aFloat);
        assertEquals(1, aFloat, 0.0f);
        aFloat = converter.toFloat(f);
        assertNotNull(aFloat);
        assertEquals(0, aFloat, 0.0f);
    }

    @Test
    public void convertToFloatNull() throws Exception {
        Boolean t = null;
        Float f = converter.toFloat(t);
        assertNull(f);
    }

    @Test
    public void convertToInteger() throws Exception {
        Boolean t = Boolean.TRUE;
        Boolean f = Boolean.FALSE;
        Integer i = converter.toInteger(t);
        assertNotNull(i);
        assertEquals(1, i.intValue());
        i = converter.toInteger(f);
        assertNotNull(i);
        assertEquals(0, i.intValue());
    }

    @Test
    public void convertToIntegerNull() throws Exception {
        Boolean t = null;
        Integer i = converter.toInteger(t);
        assertNull(i);
    }

    @Test
    public void convertToLong() throws Exception {
        Boolean t = Boolean.TRUE;
        Boolean f = Boolean.FALSE;
        Long l = converter.toLong(t);
        assertNotNull(l);
        assertEquals(1, l.longValue());
        l = converter.toLong(f);
        assertNotNull(l);
        assertEquals(0, l.longValue());
    }

    @Test
    public void convertToLongNull() throws Exception {
        Boolean t = null;
        Long l = converter.toLong(t);
        assertNull(l);
    }

    @Test
    public void convertToShort() throws Exception {
        Boolean t = Boolean.TRUE;
        Boolean f = Boolean.FALSE;
        Short s = converter.toShort(t);
        assertNotNull(s);
        assertEquals(1, s.shortValue());
        s = converter.toShort(f);
        assertNotNull(s);
        assertEquals(0, s.shortValue());
    }

    @Test
    public void convertToShortNull() throws Exception {
        Boolean t = null;
        Short s = converter.toShort(t);
        assertNull(s);
    }

    @Test
    public void convertToString() throws Exception {
        Boolean t = Boolean.TRUE;
        Boolean f = Boolean.FALSE;
        String s = converter.toString(t, null, null);
        assertNotNull(s);
        assertTrue("true".equals(s));
        s = converter.toString(f, null, null);
        assertNotNull(s);
        assertTrue("false".equals(s));
    }

    @Test
    public void convertToStringNull() throws Exception {
        Boolean t = null;
        String s = converter.toString(t, null, null);
        assertNull(s);
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class<?> aClass = BooleanConverter.class;
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
                    assertTrue(atlasConversionInfo.sourceType().compareTo(FieldType.BOOLEAN) == 0);
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
