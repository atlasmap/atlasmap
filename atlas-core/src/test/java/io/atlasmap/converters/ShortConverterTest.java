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
import io.atlasmap.converters.ShortConverter;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ShortConverterTest {
    private AtlasPrimitiveConverter<Short> converter = new ShortConverter();

    @Test
    public void convertToBoolean() throws Exception {
        Short f = 0;
        Short t = 1;

        Boolean b = converter.convertToBoolean(t);
        assertNotNull(b);
        assertTrue(b);

        b = converter.convertToBoolean(f);
        assertNotNull(b);
        assertFalse(b);

    }

    @Test
    public void convertToBoolean_Null() throws Exception {
        Short l = null;
        Boolean b = converter.convertToBoolean(l);
        assertNull(b);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToBoolean_Exception() throws Exception {
        Short dt = -1;
        Boolean b = converter.convertToBoolean(dt);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToByte() throws Exception {
        Short l = 0;
        Byte b = converter.convertToByte(l);
    }

    @Test
    public void convertToByte_Null() throws Exception {
        assertNull(converter.convertToByte(null));
    }

    @Test
    public void convertToCharacter() throws Exception {      
        Short shorty = new Short((short)4);
        Character character = converter.convertToCharacter(shorty);
        assertNotNull(character);
        assertEquals(Character.valueOf((char)shorty.shortValue()), character);
    }

    @Test
    public void convertToCharacter_Null() throws Exception {
        Short s = null;
        Character c = converter.convertToCharacter(s);
        assertNull(c);
    }

    @Test
    public void convertToCharacter_MAX() throws Exception {
        Short s = Short.MAX_VALUE;
        Character c = converter.convertToCharacter(s);
        assertNotNull(c);
        assertEquals(Character.valueOf((char)s.shortValue()), c);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacter_MIN() throws Exception {
        Short s = Short.MIN_VALUE;
        Character c = converter.convertToCharacter(s);
    }


    @Test
    public void convertToDouble() throws Exception {
        Short s = 0;
        Double d = converter.convertToDouble(s);
        assertNotNull(d);
        assertEquals(0.0, d, 0.0);
    }

    @Test
    public void convertToDouble_Null() throws Exception {
        Short s = null;
        Double d = converter.convertToDouble(s);
        assertNull(d);
    }

    @Test
    public void convertToDouble_MAX() throws Exception {
        Short s = Short.MAX_VALUE;
        Double d = converter.convertToDouble(s);
        assertNotNull(d);
        assertEquals(Short.MAX_VALUE, s, 0.0);
    }


    @Test
    public void convertToFloat() throws Exception {
        Short s = 0;
        Float f = converter.convertToFloat(s);
        assertNotNull(f);
        assertEquals(0.0f, f, 0.0);
    }

    @Test
    public void convertToFloat_Null() throws Exception {
        assertNull(converter.convertToFloat(null));
    }

    @Test
    public void convertToFloat_MAX() throws Exception {
        Short s = Short.MAX_VALUE;
        Float f = converter.convertToFloat(s);
        assertNotNull(f);
        assertEquals(Short.MAX_VALUE, s, 0.0);
    }

    @Test
    public void convertToInteger() throws Exception {
        Short s = 0;
        Integer i = converter.convertToInteger(s);
        assertNotNull(i);
        assertEquals(0, i, 0.0);
    }

    @Test
    public void convertToInteger_Null() throws Exception {
        Short l = null;
        Integer i = converter.convertToInteger(l);
        assertNull(i);
    }

    @Test
    public void convertToLong() throws Exception {
        Short s = 1;
        Long l = converter.convertToLong(s);
        assertNotNull(l);
        assertEquals(1, l, 0.0);
    }

    @Test
    public void convertToLong_Null() throws Exception {
        Short s = null;
        Long l = converter.convertToLong(s);
        assertNull(l);
    }

    @Test
    public void convertToLong_MAX() throws Exception {
        Short s = Short.MAX_VALUE;
        Long l = converter.convertToLong(s);
        assertNotNull(l);
        assertEquals(32767.0, l, 0.0);
    }

    @Test
    public void convertToLong_MIN() throws Exception {
        Short s = Short.MIN_VALUE;
        Long l = converter.convertToLong(s);
        assertNotNull(l);
        assertEquals(-32768.0, l, 0.0);
    }

    @Test
    public void convertToShort() throws Exception {
        Short aShort = 0;
        Short s = converter.convertToShort(aShort);
        assertNotNull(s);
        assertNotSame(aShort, s);
        assertEquals(0, s, 0.0);
    }

    @Test
    public void convertToShort_Null() throws Exception {
        Short l = null;
        Short s = converter.convertToShort(l);
        assertNull(s);
    }


    @Test
    public void convertToString() throws Exception {
        Short l = 0;
        String s = converter.convertToString(l);
        assertNotNull(s);
        assertTrue("0".equals(s));
    }

    @Test
    public void convertToString_Null() throws Exception {
        Short l = null;
        String s = converter.convertToString(l);
        assertNull(s);
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class aClass = ShortConverter.class;
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            if(method.isSynthetic()) {
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
                        assertNotNull(atlasConversionConcern.getMessage());
                        assertNotNull(atlasConversionConcern.value());
                    }
                }
            }
        }
    }
    
    @Test
    public void testCharacterDigit() {
        int RADIX = 10;
        char ch = Character.valueOf((char)Short.MAX_VALUE);
       

        int i2 = Character.digit(ch, RADIX);
       
    }
}