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

import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DoubleConverterTest {

    private AtlasPrimitiveConverter<Double> converter = new DoubleConverter();

    @Test
    public void convertToBoolean() throws Exception {
        Double df = 0.0;
        Double dt = 1.0;

        Boolean b = converter.convertToBoolean(dt);
        assertNotNull(b);
        assertTrue(b);

        b = converter.convertToBoolean(df);
        assertNotNull(b);
        assertFalse(b);

    }

    @Test
    public void convertToBooleanNull() throws Exception {
        Double df = null;
        Boolean b = converter.convertToBoolean(df);
        assertNull(b);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToBooleanException() throws Exception {
        Double dt = -1.0;
        converter.convertToBoolean(dt);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToByte() throws Exception {
        Double df = 0.0;
        converter.convertToByte(df);
    }

    @Test
    public void convertToByteNull() throws Exception {
        assertNull(converter.convertToByte(null));
    }

    @Test
    public void convertToCharacter() throws Exception {
        Double df = 0.0;
        Character c = converter.convertToCharacter(df);
        assertNotNull(c);
        assertEquals(0, c.charValue());
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        Double df = null;
        Character c = converter.convertToCharacter(df);
        assertNull(c);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacterMAX() throws Exception {
        Double df = 65556.00;
        converter.convertToCharacter(df);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacterMIN() throws Exception {
        Double df = -1.00;
        converter.convertToCharacter(df);
    }

    @Test
    public void convertToDouble() throws Exception {
        Double df = 0.0;
        Double d = converter.convertToDouble(df);
        assertNotNull(d);
        assertNotSame(df, d);
        assertEquals(0.0, d, 0.0);
    }

    @Test
    public void convertToDoubleNull() throws Exception {
        Double df = null;
        Double d = converter.convertToDouble(df);
        assertNull(d);
    }

    @Test
    public void convertToFloat() throws Exception {
        Double df = 0.0d;
        Float f = converter.convertToFloat(df);
        assertNotNull(f);
        assertEquals(0.0, f, 0.0);

        df = 0.15;
        f = converter.convertToFloat(df);
        assertNotNull(f);
        assertEquals(0.15, f, 1.0);
    }

    @Test
    public void convertToFloatNull() throws Exception {
        Double df = null;
        Float f = converter.convertToFloat(df);
        assertNull(f);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToFloatExceptionMAX() throws Exception {
        Double df = Double.MAX_VALUE;
        converter.convertToFloat(df);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToFloatExceptionMIN() throws Exception {
        Double df = Double.MIN_VALUE;
        converter.convertToFloat(df);
    }

    @Test
    public void convertToInteger() throws Exception {
        Double df = 0.15;
        Integer i = converter.convertToInteger(df);
        assertNotNull(i);
        assertEquals(0, i, 0.0);
    }

    @Test
    public void convertToIntegerNull() throws Exception {
        Double df = null;
        Integer i = converter.convertToInteger(df);
        assertNull(i);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToIntegerExceptionMAX() throws Exception {
        Double df = Double.MAX_VALUE;
        converter.convertToInteger(df);
    }

    @Test
    public void convertToLong() throws Exception {
        Double df = 0.0;
        Long l = converter.convertToLong(df);
        assertNotNull(l);
        assertEquals(0, l, 0.0);
    }

    @Test
    public void convertToLongNull() throws Exception {
        Double df = null;
        Long l = converter.convertToLong(df);
        assertNull(l);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToLongExceptionMAX() throws Exception {
        Double df = Double.MAX_VALUE;
        converter.convertToLong(df);
    }

    @Test
    public void convertToShort() throws Exception {
        Double df = 0.0;
        Short s = converter.convertToShort(df);
        assertNotNull(s);
        assertEquals(0, s, 0.0);
    }

    @Test
    public void convertToShortNull() throws Exception {
        Double df = null;
        Short s = converter.convertToShort(df);
        assertNull(s);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToShortExceptionMAX() throws Exception {
        Double df = Double.MAX_VALUE;
        converter.convertToShort(df);
    }

    @Test
    public void convertToString() throws Exception {
        Double df = 0.0;
        String s = converter.convertToString(df);
        assertNotNull(s);
        assertTrue("0.0".equals(s));
    }

    @Test
    public void convertToStringNull() throws Exception {
        Double df = null;
        String s = converter.convertToString(df);
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
