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

import io.atlasmap.spi.AtlasConversionConcern;
import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.converters.ByteConverter;
import io.atlasmap.spi.AtlasConversionInfo;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.FieldType;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ByteConverterTest {

    private AtlasPrimitiveConverter<Byte> byteConverter = new ByteConverter();

    @Test(expected = AtlasConversionException.class)
    public void convertToBoolean() throws Exception {
        byteConverter.convertToBoolean(Byte.MAX_VALUE);
    }

    @Test
    public void convertToBoolean_Null() throws Exception {
        assertNull(byteConverter.convertToBoolean(null));
    }


    @Test(expected = AtlasConversionException.class)
    public void convertToByte() throws Exception {
        byteConverter.convertToByte(Byte.MAX_VALUE);
    }

    @Test
    public void convertToByte_Null() throws Exception {
        assertNull(byteConverter.convertToByte(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToCharacter() throws Exception {
        byteConverter.convertToCharacter(Byte.MAX_VALUE);
    }

    @Test
    public void convertToCharacter_Null() throws Exception {
        assertNull(byteConverter.convertToCharacter(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToDouble() throws Exception {
        byteConverter.convertToDouble(Byte.MAX_VALUE);
    }

    @Test
    public void convertToDouble_Null() throws Exception {
        assertNull(byteConverter.convertToDouble(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToFloat() throws Exception {
        byteConverter.convertToFloat(Byte.MAX_VALUE);
    }

    @Test
    public void convertToFloat_Null() throws Exception {
        assertNull(byteConverter.convertToFloat(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToInteger() throws Exception {
        byteConverter.convertToInteger(Byte.MAX_VALUE);
    }

    @Test
    public void convertToInteger_Null() throws Exception {
        assertNull(byteConverter.convertToInteger(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToLong() throws Exception {
        byteConverter.convertToLong(Byte.MAX_VALUE);
    }

    @Test
    public void convertToLong_Null() throws Exception {
        assertNull(byteConverter.convertToLong(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToShort() throws Exception {
        byteConverter.convertToShort(Byte.MAX_VALUE);
    }

    @Test
    public void convertToIShort_Null() throws Exception {
        assertNull(byteConverter.convertToShort(null));
    }

    @Test(expected = AtlasConversionException.class)
    public void convertToString() throws Exception {
        byteConverter.convertToString(Byte.MAX_VALUE);
    }

    @Test
    public void convertToString_Null() throws Exception {
        assertNull(byteConverter.convertToString(null));
    }

    @Test
    public void checkAnnotations() throws Exception {
        Class aClass = ByteConverter.class;
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
                    assertTrue(atlasConversionInfo.sourceType().compareTo(FieldType.BYTE) == 0);
                    assertNotNull(atlasConversionInfo.targetType());
                    for (AtlasConversionConcern atlasConversionConcern : atlasConversionInfo.concerns()) {
                        assertNotNull(atlasConversionConcern.getMessage());
                        assertNotNull(atlasConversionConcern.value());
                    }
                }
            }
        }
    }

}