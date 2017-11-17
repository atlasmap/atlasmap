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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

public class ByteConverterTest {

    private AtlasPrimitiveConverter<Byte> byteConverter = new ByteConverter();

    @Test(expected = AtlasConversionException.class)
    public void convertToBoolean() throws Exception {
        byteConverter.convertToBoolean(Byte.MAX_VALUE);
    }

    @Test
    public void convertToBooleanNull() throws Exception {
        assertNull(byteConverter.convertToBoolean(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToByte() throws Exception {
        byteConverter.convertToByte(Byte.MAX_VALUE);
    }

    @Test
    public void convertToByteNull() throws Exception {
        assertNull(byteConverter.convertToByte(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacter() throws Exception {
        byteConverter.convertToCharacter(Byte.MAX_VALUE);
    }

    @Test
    public void convertToCharacterNull() throws Exception {
        assertNull(byteConverter.convertToCharacter(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToDouble() throws Exception {
        byteConverter.convertToDouble(Byte.MAX_VALUE);
    }

    @Test
    public void convertToDoubleNull() throws Exception {
        assertNull(byteConverter.convertToDouble(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToFloat() throws Exception {
        byteConverter.convertToFloat(Byte.MAX_VALUE);
    }

    @Test
    public void convertToFloatNull() throws Exception {
        assertNull(byteConverter.convertToFloat(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToInteger() throws Exception {
        byteConverter.convertToInteger(Byte.MAX_VALUE);
    }

    @Test
    public void convertToIntegerNull() throws Exception {
        assertNull(byteConverter.convertToInteger(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToLong() throws Exception {
        byteConverter.convertToLong(Byte.MAX_VALUE);
    }

    @Test
    public void convertToLongNull() throws Exception {
        assertNull(byteConverter.convertToLong(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToShort() throws Exception {
        byteConverter.convertToShort(Byte.MAX_VALUE);
    }

    @Test
    public void convertToIShortNull() throws Exception {
        assertNull(byteConverter.convertToShort(null));
    }

    @Test
    public void convertToString() throws Exception {
        Assert.assertEquals(byteConverter.convertToString(Byte.parseByte("1")), "1");
    }

    @Test
    public void convertToStringNull() throws Exception {
        assertNull(byteConverter.convertToString(null));
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
