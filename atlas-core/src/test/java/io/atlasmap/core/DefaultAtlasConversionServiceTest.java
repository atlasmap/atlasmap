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
package io.atlasmap.core;

import io.atlasmap.api.AtlasConverter;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.spi.AtlasPrimitiveConverter;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.FieldType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DefaultAtlasConversionServiceTest {

    private AtlasConversionService service = null;

    @Before
    public void setUp() throws Exception {
        service = DefaultAtlasConversionService.getRegistry();
    }

    @After
    public void tearDown() throws Exception {
        if (service != null) {
            service = null;
        }
    }

    @Test
    public void getservice() throws Exception {
        assertNotNull(service);
        DefaultAtlasConversionService service2 = DefaultAtlasConversionService.getRegistry();
        assertNotNull(service2);
        assertSame(service, service2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findMatchingConverterByFieldTypes() throws Exception {
        assertNotNull(service);
        Optional<AtlasConverter> atlasConverter = service.findMatchingConverter(FieldType.STRING, FieldType.BOOLEAN);
        assertTrue(atlasConverter.isPresent());
        assertNotNull(atlasConverter);
        assertTrue(AtlasPrimitiveConverter.class.isAssignableFrom(atlasConverter.get().getClass()));
        AtlasPrimitiveConverter<String> primitiveConverter = (AtlasPrimitiveConverter<String>) atlasConverter.get();
        assertNotNull(primitiveConverter);
        assertThat("io.atlasmap.core.MockPrimitiveConverter", is(atlasConverter.get().getClass().getCanonicalName()));
        Boolean t = primitiveConverter.convertToBoolean("T");
        assertNotNull(t);
        assertTrue(t);
        Boolean f = primitiveConverter.convertToBoolean("F");
        assertNotNull(f);
        assertFalse(f);
    }

    @Test
    public void findMatchingConverterByFieldTypesCustomConverter() throws Exception {
        assertNotNull(service);
        Optional<AtlasConverter> atlasConverter = service.findMatchingConverter(FieldType.STRING, FieldType.STRING);
        assertNotNull(atlasConverter);
        assertTrue(atlasConverter.isPresent());
        assertTrue(AtlasConverter.class.isAssignableFrom(atlasConverter.get().getClass()));
        assertThat("io.atlasmap.core.MockCustomConverter", is(atlasConverter.get().getClass().getCanonicalName()));
    }

    @Test
    public void findMatchingConverterByFieldTypesNoMatching() throws Exception {
        assertNotNull(service);
        Optional<AtlasConverter> atlasConverter = service.findMatchingConverter(FieldType.STRING, FieldType.COMPLEX);
        assertFalse(atlasConverter.isPresent());
    }

    @Test
    public void findMatchingConverterBySourceClass() throws Exception {
        assertNotNull(service);
        Optional<AtlasConverter> atlasConverter = service.findMatchingConverter("java.util.Date", "java.time.ZonedDateTime");
        assertNotNull(atlasConverter);
        assertTrue(atlasConverter.isPresent());
        assertTrue(AtlasConverter.class.isAssignableFrom(atlasConverter.get().getClass()));
        assertThat("io.atlasmap.core.MockCustomConverter", is(atlasConverter.get().getClass().getCanonicalName()));
    }

    @Test
    public void findMatchingConverterBySourceClassNoMatching() throws Exception {
        assertNotNull(service);
        Optional<AtlasConverter> atlasConverter = service.findMatchingConverter("java.util.Date", "java.time.CustomClass");
        assertFalse(atlasConverter.isPresent());
    }

    @Test
    public void findMatchingMethodByFieldTypes() throws Exception {
        assertNotNull(service);
        Optional<AtlasConverter> atlasConverter = service.findMatchingConverter(FieldType.STRING, FieldType.BOOLEAN);
        AtlasConverter converter = atlasConverter.orElse(null);
        assertNotNull(converter);
        Optional<Method> methods = service.findMatchingMethod(FieldType.STRING, FieldType.BOOLEAN, atlasConverter.orElseGet(null));
        Method method = methods.orElseGet(null);
        assertNotNull(method);
    }
    
    @Test
    public void testIsPrimitiveClass() {
        assertTrue(service.isPrimitive(boolean.class));
        assertTrue(service.isPrimitive(byte.class));
        assertTrue(service.isPrimitive(char.class));
        assertTrue(service.isPrimitive(double.class));
        assertTrue(service.isPrimitive(float.class));
        assertTrue(service.isPrimitive(int.class));
        assertTrue(service.isPrimitive(long.class));
        assertTrue(service.isPrimitive(short.class));
                
        // Negative testing
        assertFalse(service.isPrimitive(Boolean.class));
        assertFalse(service.isPrimitive(Byte.class));
        assertFalse(service.isPrimitive(Character.class));
        assertFalse(service.isPrimitive(Double.class));
        assertFalse(service.isPrimitive(Float.class));
        assertFalse(service.isPrimitive(Integer.class));
        assertFalse(service.isPrimitive(Long.class));
        assertFalse(service.isPrimitive(Short.class));
        assertFalse(service.isPrimitive(String.class));

        assertFalse(service.isPrimitive(AtlasMapping.class));
        assertFalse(service.isPrimitive(List.class));
        assertFalse(service.isPrimitive((Class)null));
    }
    
    @Test
    public void testIsPrimitiveFieldType() {
        assertTrue(service.isPrimitive(FieldType.BOOLEAN));
        assertTrue(service.isPrimitive(FieldType.BYTE));
        assertTrue(service.isPrimitive(FieldType.CHAR));
        assertTrue(service.isPrimitive(FieldType.DECIMAL));
        assertTrue(service.isPrimitive(FieldType.DOUBLE));
        assertTrue(service.isPrimitive(FieldType.FLOAT));
        assertTrue(service.isPrimitive(FieldType.INTEGER));
        assertTrue(service.isPrimitive(FieldType.LONG));
        assertTrue(service.isPrimitive(FieldType.SHORT));
        assertTrue(service.isPrimitive(FieldType.STRING));
                
        // Negative testing
        assertFalse(service.isPrimitive(FieldType.ALL));
        assertFalse(service.isPrimitive(FieldType.BYTE_ARRAY));
        assertFalse(service.isPrimitive(FieldType.COMPLEX));
        assertFalse(service.isPrimitive(FieldType.DATE));
        assertFalse(service.isPrimitive(FieldType.DATE_TIME));
        assertFalse(service.isPrimitive(FieldType.DATE_TIME_TZ));
        assertFalse(service.isPrimitive(FieldType.DATE_TZ));
        assertFalse(service.isPrimitive(FieldType.TIME));
        assertFalse(service.isPrimitive(FieldType.TIME_TZ));
        assertFalse(service.isPrimitive((FieldType)null));
    }
    
    @Test
    public void testIsBoxedPrimitive() {
        assertTrue(service.isBoxedPrimitive(Boolean.class));
        assertTrue(service.isBoxedPrimitive(Byte.class));
        assertTrue(service.isBoxedPrimitive(Character.class));
        assertTrue(service.isBoxedPrimitive(Double.class));
        assertTrue(service.isBoxedPrimitive(Float.class));
        assertTrue(service.isBoxedPrimitive(Integer.class));
        assertTrue(service.isBoxedPrimitive(Long.class));
        assertTrue(service.isBoxedPrimitive(Short.class));
        assertTrue(service.isBoxedPrimitive(String.class));
        
        // Negative testing
        assertFalse(service.isBoxedPrimitive(boolean.class));
        assertFalse(service.isBoxedPrimitive(byte.class));
        assertFalse(service.isBoxedPrimitive(char.class));
        assertFalse(service.isBoxedPrimitive(double.class));
        assertFalse(service.isBoxedPrimitive(float.class));
        assertFalse(service.isBoxedPrimitive(int.class));
        assertFalse(service.isBoxedPrimitive(long.class));
        assertFalse(service.isBoxedPrimitive(short.class));

        assertFalse(service.isBoxedPrimitive(AtlasMapping.class));
        assertFalse(service.isBoxedPrimitive(List.class));
        assertFalse(service.isBoxedPrimitive(null));
    }
    
    @Test
    public void testBoxOrUnboxPrimitive() {
        assertEquals(Boolean.class, service.boxOrUnboxPrimitive(boolean.class));
        assertEquals(boolean.class, service.boxOrUnboxPrimitive(Boolean.class));
        assertEquals(Byte.class, service.boxOrUnboxPrimitive(byte.class));
        assertEquals(byte.class, service.boxOrUnboxPrimitive(Byte.class));
        assertEquals(Double.class, service.boxOrUnboxPrimitive(double.class));
        assertEquals(double.class, service.boxOrUnboxPrimitive(Double.class));
        assertEquals(Float.class, service.boxOrUnboxPrimitive(float.class));
        assertEquals(float.class, service.boxOrUnboxPrimitive(Float.class));
        assertEquals(Integer.class, service.boxOrUnboxPrimitive(int.class));
        assertEquals(int.class, service.boxOrUnboxPrimitive(Integer.class));
        assertEquals(Long.class, service.boxOrUnboxPrimitive(long.class));
        assertEquals(long.class, service.boxOrUnboxPrimitive(Long.class));
        assertEquals(Short.class, service.boxOrUnboxPrimitive(short.class));
        assertEquals(short.class, service.boxOrUnboxPrimitive(Short.class));
        assertEquals(String.class, service.boxOrUnboxPrimitive(String.class));
        
        // Negative testing
        assertNotEquals(Boolean.class, service.boxOrUnboxPrimitive(Boolean.class));
        assertNotEquals(boolean.class, service.boxOrUnboxPrimitive(boolean.class));
        assertNotEquals(Byte.class, service.boxOrUnboxPrimitive(Byte.class));
        assertNotEquals(byte.class, service.boxOrUnboxPrimitive(byte.class));
        assertNotEquals(Double.class, service.boxOrUnboxPrimitive(Double.class));
        assertNotEquals(double.class, service.boxOrUnboxPrimitive(double.class));
        assertNotEquals(Float.class, service.boxOrUnboxPrimitive(Float.class));
        assertNotEquals(float.class, service.boxOrUnboxPrimitive(float.class));
        assertNotEquals(Integer.class, service.boxOrUnboxPrimitive(Integer.class));
        assertNotEquals(int.class, service.boxOrUnboxPrimitive(int.class));
        assertNotEquals(Long.class, service.boxOrUnboxPrimitive(Long.class));
        assertNotEquals(long.class, service.boxOrUnboxPrimitive(long.class));
        assertNotEquals(Short.class, service.boxOrUnboxPrimitive(Short.class));
        assertNotEquals(short.class, service.boxOrUnboxPrimitive(short.class));
        assertNotEquals(String.class, service.boxOrUnboxPrimitive(List.class));
        
        assertNull(service.boxOrUnboxPrimitive(null));
    }
    
    @Test
    public void testCopyPrimitive() {
        
        Object sourceValue = null;
        Object targetValue = service.copyPrimitive(sourceValue);
        assertNull(targetValue);
        
        sourceValue = (boolean)true;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Boolean.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Boolean(true), (Boolean)targetValue);
        sourceValue = (boolean)false;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Boolean.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Boolean(false), (Boolean)targetValue);
        
        sourceValue = (byte)1;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Byte.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Byte((byte)1), (Byte)targetValue);
        sourceValue = (byte)0;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Byte.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Byte((byte)0), (Byte)targetValue);

        sourceValue = (char)'a';
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Character.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Character((char)'a'), (Character)targetValue);
        sourceValue = (char)'z';
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Character.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Character((char)'z'), (Character)targetValue);
        
        sourceValue = Double.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Double.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Double(Double.MIN_VALUE), (Double)targetValue);
        sourceValue = Double.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Double.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Double(Double.MAX_VALUE), (Double)targetValue);
        
        sourceValue = Float.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Float.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Float(Float.MIN_VALUE), (Float)targetValue);
        sourceValue = Float.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Float.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Float(Float.MAX_VALUE), (Float)targetValue);
        
        sourceValue = Integer.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Integer.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Integer(Integer.MIN_VALUE), (Integer)targetValue);
        sourceValue = Integer.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Integer.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Integer(Integer.MAX_VALUE), (Integer)targetValue);
        
        sourceValue = Long.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Long.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Long(Long.MIN_VALUE), (Long)targetValue);
        sourceValue = Long.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Long.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Long(Long.MAX_VALUE), (Long)targetValue);
        
        sourceValue = Short.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Short.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Short(Short.MIN_VALUE), (Short)targetValue);
        sourceValue = Short.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Short.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Short(Short.MAX_VALUE), (Short)targetValue);
        
        // Non primitive handling
        sourceValue = new ArrayList<String>();
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertEquals(sourceValue, targetValue);
        
        sourceValue = new String("foo");
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertEquals("foo", targetValue);
    }
}
