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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class IntegerConverterTest {

    private AtlasPrimitiveConverter<Integer> converter = new IntegerConverter();

    @Test
    public void convertToBoolean() throws Exception {
        int xTrue = 1;
        int xFalse = 0;

        Boolean out = converter.convertToBoolean(xTrue);
        assertNotNull(out);
        assertTrue(out);
        out = converter.convertToBoolean(xFalse);
        assertNotNull(out);
        assertFalse(out);
    }

    @Test
    public void convertToBooleanNull() throws Exception {
        Boolean out = converter.convertToBoolean(null);
        assertNull(out);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToBooleanConvertException() throws Exception {
        converter.convertToBoolean(10);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToByte() throws Exception {
        converter.convertToByte(Integer.MAX_VALUE);
    }

    @Test
    public void convertToByteNull() throws Exception {
        Byte byt = converter.convertToByte(null);
        assertNull(byt);
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        Character character = converter.convertToCharacter(null);
        assertNull(character);
    }

    @Test
    public void convertToCharacter() throws Exception {
        Integer integer = new Integer(4);
        Character character = converter.convertToCharacter(integer);
        assertNotNull(character);
        int revert = Character.digit(character, 10);
        assertEquals(integer, new Integer(revert));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacterGreaterThanMAX() throws Exception {
        Integer integer = 1500000;
        converter.convertToCharacter(integer);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacterLessThanMIN() throws Exception {
        Integer integer = -1500000;
        converter.convertToCharacter(integer);
    }

    @Test
    public void convertToDouble() throws Exception {
        Integer integer = 0;
        Double d = converter.convertToDouble(integer);
        assertNotNull(d);
        assertEquals(d, 0.0, 0.0);

        integer = 1;
        d = converter.convertToDouble(integer);
        assertNotNull(d);
        assertEquals(1.0, d, 0.0);
    }

    @Test
    public void convertToDoubleNull() throws Exception {
        assertNull(converter.convertToDouble(null));
    }

    @Test
    public void convertToFloat() throws Exception {
        Integer integer = 0;
        Float f = converter.convertToFloat(integer);
        assertNotNull(f);
        assertEquals(f, 0.0, 0.0);

        integer = 1;
        f = converter.convertToFloat(integer);
        assertNotNull(f);
        assertEquals(1.0, f, 0.0);
    }

    @Test
    public void convertToFloatNull() throws Exception {
        assertNull(converter.convertToFloat(null));
    }

    @Test
    public void convertToShort() throws Exception {
        int i = Short.MAX_VALUE;
        int negI = Short.MIN_VALUE;

        Short out = converter.convertToShort(i);
        assertNotNull(out);
        assertEquals(i, out.intValue());
        out = converter.convertToShort(negI);
        assertNotNull(out);
        assertEquals(negI, out.intValue());
    }

    @Test
    public void convertToShortNull() throws Exception {
        Short out = converter.convertToShort(null);
        assertNull(out);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToShortConvertExceptionGreaterThanMax() throws Exception {
        converter.convertToShort(Integer.MAX_VALUE);
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToShortConvertExceptionLessThanMin() throws Exception {
        converter.convertToShort(Integer.MIN_VALUE);
    }

    @Test
    public void convertToLong() throws Exception {
        int i = Integer.MAX_VALUE;
        int negI = Integer.MIN_VALUE;

        Long out = converter.convertToLong(i);
        assertNotNull(out);
        assertEquals(i, out.intValue());
        out = converter.convertToLong(negI);
        assertNotNull(out);
        assertEquals(negI, out.intValue());
    }

    @Test
    public void convertToLongNull() throws Exception {
        Long out = converter.convertToLong(null);
        assertNull(out);
    }

    @Test
    public void convertToString() throws Exception {
        int i = Integer.MAX_VALUE;
        int negI = Integer.MIN_VALUE;

        String out = converter.convertToString(i);
        assertNotNull(out);
        assertEquals(Integer.toString(i), out);
        out = converter.convertToString(negI);
        assertNotNull(out);
        assertEquals(Integer.toString(negI), out);
    }

    @Test
    public void convertToStringNull() throws Exception {
        String out = converter.convertToString(null);
        assertNull(out);
    }

    @Test
    public void convertToInteger() throws Exception {
        Integer foo = Integer.MAX_VALUE;
        Integer out = converter.convertToInteger(foo);
        foo++;
        assertNotNull(out);
        // test that a copy was made
        assertNotSame(out, foo);
        assertNotEquals(out, foo);
    }

    @Test
    public void convertToIntegerNull() throws Exception {
        Integer out = converter.convertToInteger(null);
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
                        assertNotNull(atlasConversionConcern.getMessage());
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
