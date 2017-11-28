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
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

public class LongConverterTest {
    private AtlasPrimitiveConverter<Long> converter = new LongConverter();

    @Test
    public void convertToBoolean() throws Exception {
        Long aLong = 0L;
        Long l = 1L;

        Boolean b = converter.convertToBoolean(l);
        assertNotNull(b);
        assertTrue(b);

        b = converter.convertToBoolean(aLong);
        assertNotNull(b);
        assertFalse(b);

    }

    @Test
    public void convertToBooleanNull() throws Exception {
        Long l = null;
        Boolean b = converter.convertToBoolean(l);
        assertNull(b);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToBooleanException() throws Exception {
        Long dt = -1L;
        converter.convertToBoolean(dt);
    }

    @Test
    public void convertToByte() throws Exception {
        Long l = 0L;
        Byte value = (byte) 0;
        assertEquals(value, converter.convertToByte(l));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToByteOutOfRange() throws Exception {
        converter.convertToByte(Long.MAX_VALUE);
    }

    @Test
    public void convertToByteNull() throws Exception {
        assertNull(converter.convertToByte(null));
    }

    @Test
    public void convertToCharacter() throws Exception {
        Long l = 0L;
        Character c = converter.convertToCharacter(l);
        assertNotNull(c);
        assertEquals(0, c.charValue());
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        Long l = null;
        Character c = converter.convertToCharacter(l);
        assertNull(c);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacterMAX() throws Exception {
        Long l = Long.MAX_VALUE;
        converter.convertToCharacter(l);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacterMIN() throws Exception {
        Long l = -1L;
        converter.convertToCharacter(l);
    }

    @Test
    public void convertToDouble() throws Exception {
        Long l = 0L;
        Double d = converter.convertToDouble(l);
        assertNotNull(d);
        assertEquals(0.0, d, 0.0);
    }

    @Test
    public void convertToDoubleNull() throws Exception {
        Long l = null;
        Double d = converter.convertToDouble(l);
        assertNull(d);
    }

    @Test
    public void convertToDoubleMAX() throws Exception {
        Long l = Long.MAX_VALUE;
        Double d = converter.convertToDouble(l);
        assertNotNull(d);
        assertEquals(Long.MAX_VALUE, l, 0.0);
    }

    @Test
    public void convertToFloat() throws Exception {
        Long l = 0L;
        Float f = converter.convertToFloat(l);
        assertNotNull(f);
        assertEquals(0.0, f, 0.0);

        l = 1L;
        f = converter.convertToFloat(l);
        assertNotNull(f);
        assertEquals(1.0f, f, 0.0);
    }

    @Test
    public void convertToFloatNull() throws Exception {
        assertNull(converter.convertToFloat(null));
    }

    @Test
    public void convertToFloatMAX() throws Exception {
        Long l = Long.MAX_VALUE;
        Float f = converter.convertToFloat(l);
        assertNotNull(f);
        assertEquals(Long.MAX_VALUE, l, 0.0);
    }

    @Test
    public void convertToInteger() throws Exception {
        Long l = 0L;
        Integer i = converter.convertToInteger(l);
        assertNotNull(i);
        assertEquals(0, i, 0.0);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToIntegerMAX() throws Exception {
        converter.convertToInteger(Long.MAX_VALUE);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToIntegerMIN() throws Exception {
        converter.convertToInteger(Long.MIN_VALUE);
    }

    @Test
    public void convertToIntegerNull() throws Exception {
        Long l = null;
        Integer i = converter.convertToInteger(l);
        assertNull(i);
    }

    @Test
    public void convertToLong() throws Exception {
        Long l = 1L;
        Long d = converter.convertToLong(l);
        assertNotNull(d);
        assertNotSame(l, d);
        assertEquals(1L, d, 0.0);
    }

    @Test
    public void convertToLongNull() throws Exception {
        Long l = null;
        Long d = converter.convertToLong(l);
        assertNull(d);
    }

    @Test
    public void convertToShort() throws Exception {
        Long l = 0L;
        Short s = converter.convertToShort(l);
        assertNotNull(s);
        assertEquals(0, s, 0.0);
    }

    @Test
    public void convertToShortNull() throws Exception {
        Long l = null;
        Short s = converter.convertToShort(l);
        assertNull(s);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToShortExceptionMAX() throws Exception {
        Long l = Long.MAX_VALUE;
        converter.convertToShort(l);
    }

    @Test
    public void convertToString() throws Exception {
        Long l = 0L;
        String s = converter.convertToString(l);
        assertNotNull(s);
        assertTrue("0".equals(s));
    }

    @Test
    public void convertToStringNull() throws Exception {
        Long l = null;
        String s = converter.convertToString(l);
        assertNull(s);
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class<?> aClass = LongConverter.class;
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
                    assertTrue(atlasConversionInfo.sourceType().compareTo(FieldType.LONG) == 0);
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
