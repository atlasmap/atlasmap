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

import org.junit.Assert;
import org.junit.Test;

import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.v2.FieldType;

public class ByteConverterTest {

    private static final byte DEFAULT_VALUE = 100;

    private ByteConverter byteConverter = new ByteConverter();

    @Test()
    public void convertToBoolean() {
        assertTrue(byteConverter.toBoolean(new Byte((byte)1)));
        assertFalse(byteConverter.toBoolean(new Byte((byte)0)));
        assertTrue(byteConverter.toBoolean(Byte.MAX_VALUE));
    }

    @Test
    public void convertToBooleanNull() {
        assertNull(byteConverter.toBoolean(null));
    }

    @Test
    public void convertToByte() {
        byteConverter.toByte(Byte.MAX_VALUE);
    }

    @Test
    public void convertToByteNull() {
        assertNull(byteConverter.toByte(null));
    }

    @Test
    public void convertToCharacter() {
        byte value = 0;
        assertEquals('\u0000', byteConverter.toCharacter(new Byte(value)).charValue());
        value = 99;
        assertEquals('c', byteConverter.toCharacter(new Byte(value)).charValue());
    }

    @Test
    public void convertToCharacterNull() {
        assertNull(byteConverter.toCharacter(null));
    }

    @Test
    public void convertToDouble() {
        assertEquals(100, byteConverter.toDouble(DEFAULT_VALUE).doubleValue(), 0);
    }

    @Test
    public void convertToDoubleNull() {
        assertNull(byteConverter.toDouble(null));
    }

    @Test
    public void convertToFloat() {
        assertEquals(100, byteConverter.toFloat(DEFAULT_VALUE).floatValue(), 0);
    }

    @Test
    public void convertToFloatNull() {
        assertNull(byteConverter.toFloat(null));
    }

    @Test
    public void convertToInteger() {
        assertEquals(100, byteConverter.toInteger(DEFAULT_VALUE).intValue());
    }

    @Test
    public void convertToIntegerNull() {
        assertNull(byteConverter.toInteger(null));
    }

    @Test
    public void convertToLong() {
        assertEquals(100, byteConverter.toLong(DEFAULT_VALUE).longValue());
    }

    @Test
    public void convertToLongNull() {
        assertNull(byteConverter.toLong(null));
    }

    @Test
    public void convertToShort() {
        assertEquals(100, byteConverter.toShort(DEFAULT_VALUE).shortValue());
    }

    @Test
    public void convertToIShortNull() {
        assertNull(byteConverter.toShort(null));
    }

    @Test
    public void convertToString() throws Exception {
        Assert.assertEquals(byteConverter.toString(Byte.parseByte("1")), "1");
    }

    @Test
    public void convertToStringNull() {
        assertNull(byteConverter.toString(null));
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class<?> aClass = ByteConverter.class;
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
                    assertTrue(atlasConversionInfo.sourceType().compareTo(FieldType.BYTE) == 0);
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
