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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasConversionService;
import io.atlasmap.api.AtlasConverter;
import io.atlasmap.converters.StringConverter;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.FieldType;

public class DefaultAtlasConversionServiceTest {

    private AtlasConversionService service = null;

    @Before
    public void setUp() {
        service = DefaultAtlasConversionService.getInstance();
    }

    @After
    public void tearDown() {
        if (service != null) {
            service = null;
        }
    }

    @Test
    public void getservice() {
        assertNotNull(service);
        DefaultAtlasConversionService service2 = DefaultAtlasConversionService.getInstance();
        assertNotNull(service2);
        assertSame(service, service2);
    }

    @Test
    public void findMatchingConverterByFieldTypes() {
        assertNotNull(service);
        Optional<AtlasConverter<?>> atlasConverter = service.findMatchingConverter(FieldType.STRING, FieldType.BOOLEAN);
        assertTrue(atlasConverter.isPresent());
        assertNotNull(atlasConverter);
        assertEquals(StringConverter.class, atlasConverter.get().getClass());
        StringConverter stringConverter = (StringConverter) atlasConverter.get();
        assertNotNull(stringConverter);
        assertThat("io.atlasmap.converters.StringConverter", is(atlasConverter.get().getClass().getCanonicalName()));
        Boolean t = stringConverter.toBoolean("T", null, null);
        assertNotNull(t);
        assertTrue(t);
        Boolean f = stringConverter.toBoolean("F", null, null);
        assertNotNull(f);
        assertFalse(f);
        service.findMatchingConverter(null, FieldType.BOOLEAN);
        service.findMatchingConverter(FieldType.STRING, null);
        FieldType fieldType = null;
        service.findMatchingConverter(fieldType, fieldType);
    }

    @Test
    public void findMatchingConverterByFieldTypesCustomConverter() {
        assertNotNull(service);
        Optional<AtlasConverter<?>> atlasConverter = service.findMatchingConverter(FieldType.STRING, FieldType.STRING);
        assertNotNull(atlasConverter);
        assertTrue(atlasConverter.isPresent());
        assertTrue(AtlasConverter.class.isAssignableFrom(atlasConverter.get().getClass()));
        assertThat("io.atlasmap.converters.StringConverter", is(atlasConverter.get().getClass().getCanonicalName()));
    }

    @Test
    public void findMatchingConverterByFieldTypesNoMatching() {
        assertNotNull(service);
        Optional<AtlasConverter<?>> atlasConverter = service.findMatchingConverter(FieldType.STRING, FieldType.COMPLEX);
        assertFalse(atlasConverter.isPresent());
    }

    @Test
    public void findMatchingConverterBySourceClass() {
        assertNotNull(service);
        Optional<AtlasConverter<?>> atlasConverter = service.findMatchingConverter("java.util.Date",
                "java.time.ZonedDateTime");
        assertNotNull(atlasConverter);
        assertTrue(atlasConverter.isPresent());
        assertTrue(AtlasConverter.class.isAssignableFrom(atlasConverter.get().getClass()));
        assertThat("io.atlasmap.converters.DateConverter", is(atlasConverter.get().getClass().getCanonicalName()));
    }

    @Test
    public void findMatchingConverterBySourceClassNoMatching() {
        assertNotNull(service);
        Optional<AtlasConverter<?>> atlasConverter = service.findMatchingConverter("java.util.Date",
                "java.time.CustomClass");
        assertFalse(atlasConverter.isPresent());
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
        assertFalse(service.isPrimitive((Class<?>) null));
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
        assertFalse(service.isPrimitive(FieldType.ANY));
        assertFalse(service.isPrimitive(FieldType.BYTE_ARRAY));
        assertFalse(service.isPrimitive(FieldType.COMPLEX));
        assertFalse(service.isPrimitive(FieldType.DATE));
        assertFalse(service.isPrimitive(FieldType.DATE_TIME));
        assertFalse(service.isPrimitive(FieldType.DATE_TIME_TZ));
        assertFalse(service.isPrimitive(FieldType.DATE_TZ));
        assertFalse(service.isPrimitive(FieldType.TIME));
        assertFalse(service.isPrimitive(FieldType.TIME_TZ));
        assertFalse(service.isPrimitive((FieldType) null));
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
        assertFalse(service.isBoxedPrimitive((Class<?>) null));
    }

    @Test
    public void testBoxOrUnboxPrimitive() {
        assertEquals(Boolean.class, service.boxOrUnboxPrimitive(boolean.class));
        assertEquals(boolean.class, service.boxOrUnboxPrimitive(Boolean.class));
        assertEquals(Byte.class, service.boxOrUnboxPrimitive(byte.class));
        assertEquals(byte.class, service.boxOrUnboxPrimitive(Byte.class));
        assertEquals(Character.class, service.boxOrUnboxPrimitive(char.class));
        assertEquals(char.class, service.boxOrUnboxPrimitive(Character.class));
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
        assertNotEquals(Character.class, service.boxOrUnboxPrimitive(Character.class));
        assertNotEquals(char.class, service.boxOrUnboxPrimitive(char.class));
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

        assertNull(service.boxOrUnboxPrimitive((Class<?>)null));
    }

    @Test
    public void testCopyPrimitive() {

        Object sourceValue = null;
        Object targetValue = service.copyPrimitive(sourceValue);
        assertNull(targetValue);

        sourceValue = (boolean) true;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Boolean.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(Boolean.TRUE, targetValue);
        sourceValue = (boolean) false;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Boolean.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(Boolean.FALSE, targetValue);

        sourceValue = (byte) 1;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Byte.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Byte((byte) 1), targetValue);
        sourceValue = (byte) 0;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Byte.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Byte((byte) 0), targetValue);

        sourceValue = (char) 'a';
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Character.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Character('a'), targetValue);
        sourceValue = (char) 'z';
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Character.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Character('z'), targetValue);

        sourceValue = Double.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Double.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Double(Double.MIN_VALUE), targetValue);
        sourceValue = Double.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Double.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Double(Double.MAX_VALUE), targetValue);

        sourceValue = Float.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Float.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Float(Float.MIN_VALUE), targetValue);
        sourceValue = Float.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Float.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Float(Float.MAX_VALUE), targetValue);

        sourceValue = Integer.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Integer.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Integer(Integer.MIN_VALUE), targetValue);
        sourceValue = Integer.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Integer.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Integer(Integer.MAX_VALUE), targetValue);

        sourceValue = Long.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Long.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Long(Long.MIN_VALUE), targetValue);
        sourceValue = Long.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Long.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Long(Long.MAX_VALUE), targetValue);

        sourceValue = Short.MIN_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Short.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Short(Short.MIN_VALUE), targetValue);
        sourceValue = Short.MAX_VALUE;
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertTrue(Short.class.getCanonicalName().equals(targetValue.getClass().getCanonicalName()));
        assertEquals(new Short(Short.MAX_VALUE), targetValue);

        // Non primitive handling
        sourceValue = new ArrayList<String>();
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertEquals(sourceValue, targetValue);

        sourceValue = "foo";
        targetValue = service.copyPrimitive(sourceValue);
        assertNotNull(targetValue);
        assertEquals("foo", targetValue);
    }

    @Test
    public void testListPrimitiveClassNames() {
        assertNotNull(DefaultAtlasConversionService.listPrimitiveClassNames());
    }

    @Test
    public void testIsPrimitive() {
        assertTrue(service.isPrimitive("short"));
        String s = null;
        assertFalse(service.isPrimitive(s));
        assertFalse(service.isPrimitive("String"));
    }

    @Test
    public void testFieldTypeFromClass() {
        String className = null;
        assertEquals(FieldType.NONE, service.fieldTypeFromClass(className));
        className = "";
        assertEquals(FieldType.NONE, service.fieldTypeFromClass(className));
        Class<String> klass = null;
        assertNull(service.fieldTypeFromClass(klass));

        assertNotNull(service.fieldTypeFromClass(Object.class));
        assertNotNull(service.fieldTypeFromClass(Boolean.class));
        assertNotNull(service.fieldTypeFromClass(Byte.class));
        assertNotNull(service.fieldTypeFromClass(Character.class));
        assertNotNull(service.fieldTypeFromClass(Double.class));
        assertNotNull(service.fieldTypeFromClass(Float.class));
        assertNotNull(service.fieldTypeFromClass(Integer.class));
        assertNotNull(service.fieldTypeFromClass(Long.class));
        assertNotNull(service.fieldTypeFromClass(Short.class));
        assertNotNull(service.fieldTypeFromClass(String.class));
        assertNotNull(service.fieldTypeFromClass(Year.class));
        assertNotNull(service.fieldTypeFromClass(Month.class));
        assertNotNull(service.fieldTypeFromClass(YearMonth.class));
        assertNotNull(service.fieldTypeFromClass(MonthDay.class));
        assertNotNull(service.fieldTypeFromClass(LocalDate.class));
        assertNotNull(service.fieldTypeFromClass(LocalTime.class));
        assertNotNull(service.fieldTypeFromClass(LocalDateTime.class));
        assertNotNull(service.fieldTypeFromClass(java.sql.Date.class));
        assertNotNull(service.fieldTypeFromClass(Date.class));
        assertNotNull(service.fieldTypeFromClass(ZonedDateTime.class));
        assertNotNull(service.fieldTypeFromClass("boolean"));
        assertNotNull(service.fieldTypeFromClass("byte"));
        assertNotNull(service.fieldTypeFromClass("char"));
        assertNotNull(service.fieldTypeFromClass("double"));
        assertNotNull(service.fieldTypeFromClass("float"));
        assertNotNull(service.fieldTypeFromClass("int"));
        assertNotNull(service.fieldTypeFromClass("long"));
        assertNotNull(service.fieldTypeFromClass("short"));
    }

    @Test
    public void testClassFromFieldType() {
        assertNull(service.classFromFieldType(null));
        assertEquals(java.lang.Object.class, service.classFromFieldType(FieldType.ANY));
        assertNotNull(service.classFromFieldType(FieldType.BOOLEAN));
        assertNotNull(service.classFromFieldType(FieldType.BYTE));
        assertNotNull(service.classFromFieldType(FieldType.CHAR));
        assertNotNull(service.classFromFieldType(FieldType.DOUBLE));
        assertNotNull(service.classFromFieldType(FieldType.FLOAT));
        assertNotNull(service.classFromFieldType(FieldType.INTEGER));
        assertNotNull(service.classFromFieldType(FieldType.LONG));
        assertNotNull(service.classFromFieldType(FieldType.SHORT));
        assertNotNull(service.classFromFieldType(FieldType.STRING));
        assertNotNull(service.classFromFieldType(FieldType.DATE));
        assertNotNull(service.classFromFieldType(FieldType.TIME));
        assertNotNull(service.classFromFieldType(FieldType.DATE_TIME));
        assertNotNull(service.classFromFieldType(FieldType.DATE_TZ));
        assertNotNull(service.classFromFieldType(FieldType.TIME_TZ));
        assertNotNull(service.classFromFieldType(FieldType.DATE_TIME_TZ));
        assertNull(service.classFromFieldType(FieldType.NONE));
    }

    @Test
    public void testConvertTypeFromBigDecimal() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.BOOLEAN));
        assertEquals((byte)1, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.BYTE));
        assertEquals((char)1, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1), service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.FLOAT));
        assertEquals(1, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.INTEGER));
        assertEquals(1L, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.LONG));
        assertEquals(BigDecimal.valueOf(1), service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.SHORT));
        assertEquals("1", service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType(BigDecimal.valueOf(1), FieldType.DECIMAL, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromBigInteger() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.BOOLEAN));
        assertEquals((byte)1, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.BYTE));
        assertEquals((char)1, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1), service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.FLOAT));
        assertEquals(1, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.INTEGER));
        assertEquals(1L, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.LONG));
        assertEquals(BigInteger.valueOf(1), service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.SHORT));
        assertEquals("1", service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType(BigInteger.valueOf(1), FieldType.BIG_INTEGER, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromBoolean() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType(true, FieldType.BOOLEAN, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType(true, FieldType.BOOLEAN, FieldType.BOOLEAN));
        assertEquals((byte)1, service.convertType(true, FieldType.BOOLEAN, FieldType.BYTE));
        assertEquals((char)1, service.convertType(true, FieldType.BOOLEAN, FieldType.CHAR));
        assertEquals(BigDecimal.valueOf(1), service.convertType(true, FieldType.BOOLEAN, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(true, FieldType.BOOLEAN, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(true, FieldType.BOOLEAN, FieldType.FLOAT));
        assertEquals(1, service.convertType(true, FieldType.BOOLEAN, FieldType.INTEGER));
        assertEquals(1L, service.convertType(true, FieldType.BOOLEAN, FieldType.LONG));
        assertEquals((short)1, service.convertType(true, FieldType.BOOLEAN, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(true, FieldType.BOOLEAN, FieldType.SHORT));
        assertEquals("true", service.convertType(true, FieldType.BOOLEAN, FieldType.STRING));
    }

    @Test
    public void testConvertTypeFromByte() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType(0x01, FieldType.BYTE, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType(0x01, FieldType.BYTE, FieldType.BOOLEAN));
        assertEquals(0x01, service.convertType(0x01, FieldType.BYTE, FieldType.BYTE));
        assertEquals((char)1, service.convertType(0x01, FieldType.BYTE, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType(0x01, FieldType.BYTE, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(0x01, FieldType.BYTE, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(0x01, FieldType.BYTE, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(0x01, FieldType.BYTE, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(0x01, FieldType.BYTE, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1), service.convertType(0x01, FieldType.BYTE, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(0x01, FieldType.BYTE, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(0x01, FieldType.BYTE, FieldType.FLOAT));
        assertEquals(1, service.convertType(0x01, FieldType.BYTE, FieldType.INTEGER));
        assertEquals(1L, service.convertType(0x01, FieldType.BYTE, FieldType.LONG));
        assertEquals(1, service.convertType(0x01, FieldType.BYTE, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(0x01, FieldType.BYTE, FieldType.SHORT));
        assertEquals("1", service.convertType(0x01, FieldType.BYTE, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType(0x01, FieldType.BYTE, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromCharacter() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(97), service.convertType('a', FieldType.CHAR, FieldType.BIG_INTEGER));
        assertEquals(false, service.convertType('a', FieldType.CHAR, FieldType.BOOLEAN));
        assertEquals((byte)97, service.convertType('a', FieldType.CHAR, FieldType.BYTE));
        assertEquals('a', service.convertType('a', FieldType.CHAR, FieldType.CHAR));
        assertEquals(BigDecimal.valueOf(97), service.convertType('a', FieldType.CHAR, FieldType.DECIMAL));
        assertEquals(97.0d, service.convertType('a', FieldType.CHAR, FieldType.DOUBLE));
        assertEquals(97.0f, service.convertType('a', FieldType.CHAR, FieldType.FLOAT));
        assertEquals(97, service.convertType('a', FieldType.CHAR, FieldType.INTEGER));
        assertEquals(97L, service.convertType('a', FieldType.CHAR, FieldType.LONG));
        assertEquals(97, service.convertType('a', FieldType.CHAR, FieldType.NUMBER));
        assertEquals((short)97, service.convertType('a', FieldType.CHAR, FieldType.SHORT));
        assertEquals("a", service.convertType('a', FieldType.CHAR, FieldType.STRING));
    }

    @Test
    public void testConvertTypeFromDate() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(new Date(1).getTime()), service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.BIG_INTEGER));
        assertEquals((byte)1, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.BYTE));
        assertEquals(LocalDate.class, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1), service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.FLOAT));
        assertEquals(1, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.INTEGER));
        assertEquals(1L, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.LONG));
        assertEquals(1L, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.SHORT));
        assertEquals(String.class, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.STRING).getClass());
        assertEquals(LocalTime.class, service.convertType(new Date(1), FieldType.DATE_TIME, FieldType.TIME).getClass());
        assertEquals(LocalDateTime.class, service.convertType(new Date(1), null, LocalDateTime.class, null).getClass());
    }

    @Test
    public void testConvertTypeFromDouble() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType(new Double(1), FieldType.DOUBLE, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.BOOLEAN));
        assertEquals((byte)0x01, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.BYTE));
        assertEquals((char)1, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1.0), service.convertType(new Double(1), FieldType.DOUBLE, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.FLOAT));
        assertEquals(1, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.INTEGER));
        assertEquals(1L, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.LONG));
        assertEquals(1.0, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.SHORT));
        assertEquals("1.0", service.convertType(new Double(1), FieldType.DOUBLE, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType(new Double(1), FieldType.DOUBLE, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromFloat() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType(new Float(1), FieldType.FLOAT, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType(new Float(1), FieldType.FLOAT, FieldType.BOOLEAN));
        assertEquals((byte)0x01, service.convertType(new Float(1), FieldType.FLOAT, FieldType.BYTE));
        assertEquals((char)1, service.convertType(new Float(1), FieldType.FLOAT, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType(new Float(1), FieldType.FLOAT, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(new Float(1), FieldType.FLOAT, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Float(1), FieldType.FLOAT, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Float(1), FieldType.FLOAT, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Float(1), FieldType.FLOAT, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1.0), service.convertType(new Float(1), FieldType.FLOAT, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(new Float(1), FieldType.FLOAT, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(new Float(1), FieldType.FLOAT, FieldType.FLOAT));
        assertEquals(1, service.convertType(new Float(1), FieldType.FLOAT, FieldType.INTEGER));
        assertEquals(1L, service.convertType(new Float(1), FieldType.FLOAT, FieldType.LONG));
        assertEquals(1.0f, service.convertType(new Float(1), FieldType.FLOAT, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(new Float(1), FieldType.FLOAT, FieldType.SHORT));
        assertEquals("1.0", service.convertType(new Float(1), FieldType.FLOAT, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType(new Float(1), FieldType.FLOAT, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromInteger() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType(new Integer(1), FieldType.INTEGER, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.BOOLEAN));
        assertEquals((byte)0x01, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.BYTE));
        assertEquals((char)1, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1), service.convertType(new Integer(1), FieldType.INTEGER, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.FLOAT));
        assertEquals(1, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.INTEGER));
        assertEquals(1L, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.LONG));
        assertEquals(1, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.SHORT));
        assertEquals("1", service.convertType(new Integer(1), FieldType.INTEGER, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType(new Integer(1), FieldType.INTEGER, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromLocalDate() throws AtlasConversionException {
        assertEquals(BigInteger.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.BIG_INTEGER).getClass());
        assertEquals(LocalDate.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.DECIMAL).getClass());
        assertEquals(Double.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.DOUBLE).getClass());
        assertEquals(Float.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.FLOAT).getClass());
        assertEquals(Long.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.LONG).getClass());
        assertEquals(Long.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.NUMBER).getClass());
        assertEquals(String.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.STRING).getClass());
        assertEquals(LocalTime.class, service.convertType(LocalDate.ofEpochDay(0), FieldType.DATE, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromLocalTime() throws AtlasConversionException {
        assertEquals(BigInteger.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.BIG_INTEGER).getClass());
        assertEquals(Date.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.DECIMAL).getClass());
        assertEquals(Double.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.DOUBLE).getClass());
        assertEquals(Float.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.FLOAT).getClass());
        assertEquals(Long.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.LONG).getClass());
        assertEquals(Long.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.NUMBER).getClass());
        assertEquals(String.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.STRING).getClass());
        assertEquals(LocalTime.class, service.convertType(LocalTime.ofSecondOfDay(0), FieldType.TIME, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromLong() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType(new Long(1), FieldType.LONG, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType(new Long(1), FieldType.LONG, FieldType.BOOLEAN));
        assertEquals((byte)0x01, service.convertType(new Long(1), FieldType.LONG, FieldType.BYTE));
        assertEquals((char)1, service.convertType(new Long(1), FieldType.LONG, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType(new Long(1), FieldType.LONG, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(new Long(1), FieldType.LONG, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Long(1), FieldType.LONG, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Long(1), FieldType.LONG, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(new Long(1), FieldType.LONG, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1), service.convertType(new Long(1), FieldType.LONG, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(new Long(1), FieldType.LONG, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(new Long(1), FieldType.LONG, FieldType.FLOAT));
        assertEquals(1, service.convertType(new Long(1), FieldType.LONG, FieldType.INTEGER));
        assertEquals(1L, service.convertType(new Long(1), FieldType.LONG, FieldType.LONG));
        assertEquals(1L, service.convertType(new Long(1), FieldType.LONG, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(new Long(1), FieldType.LONG, FieldType.SHORT));
        assertEquals("1", service.convertType(new Long(1), FieldType.LONG, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType(new Long(1), FieldType.LONG, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromNumber() throws AtlasConversionException {
        Number number = 1;
        assertEquals(BigInteger.valueOf(1), service.convertType(number, FieldType.NUMBER, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType(number, FieldType.NUMBER, FieldType.BOOLEAN));
        assertEquals((byte)0x01, service.convertType(number, FieldType.NUMBER, FieldType.BYTE));
        assertEquals((char)1, service.convertType(number, FieldType.NUMBER, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType(number, FieldType.NUMBER, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(number, FieldType.NUMBER, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(number, FieldType.NUMBER, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(number, FieldType.NUMBER, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(number, FieldType.NUMBER, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1), service.convertType(number, FieldType.NUMBER, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType(number, FieldType.NUMBER, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType(number, FieldType.NUMBER, FieldType.FLOAT));
        assertEquals(1, service.convertType(number, FieldType.NUMBER, FieldType.INTEGER));
        assertEquals(1L, service.convertType(number, FieldType.NUMBER, FieldType.LONG));
        assertEquals(1, service.convertType(number, FieldType.NUMBER, FieldType.NUMBER));
        assertEquals((short)1, service.convertType(number, FieldType.NUMBER, FieldType.SHORT));
        assertEquals("1", service.convertType(number, FieldType.NUMBER, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType(number, FieldType.NUMBER, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromShort() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType((short)1, FieldType.SHORT, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType((short)1, FieldType.SHORT, FieldType.BOOLEAN));
        assertEquals((byte)0x01, service.convertType((short)1, FieldType.SHORT, FieldType.BYTE));
        assertEquals((char)1, service.convertType((short)1, FieldType.SHORT, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType((short)1, FieldType.SHORT, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType((short)1, FieldType.SHORT, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType((short)1, FieldType.SHORT, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType((short)1, FieldType.SHORT, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType((short)1, FieldType.SHORT, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1), service.convertType((short)1, FieldType.SHORT, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType((short)1, FieldType.SHORT, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType((short)1, FieldType.SHORT, FieldType.FLOAT));
        assertEquals(1, service.convertType((short)1, FieldType.SHORT, FieldType.INTEGER));
        assertEquals(1L, service.convertType((short)1, FieldType.SHORT, FieldType.LONG));
        assertEquals((short)1, service.convertType((short)1, FieldType.SHORT, FieldType.NUMBER));
        assertEquals((short)1, service.convertType((short)1, FieldType.SHORT, FieldType.SHORT));
        assertEquals("1", service.convertType((short)1, FieldType.SHORT, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType((short)1, FieldType.SHORT, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromString() throws AtlasConversionException {
        assertEquals(BigInteger.valueOf(1), service.convertType("1", FieldType.STRING, FieldType.BIG_INTEGER));
        assertEquals(true, service.convertType("1", FieldType.STRING, FieldType.BOOLEAN));
        assertEquals((byte)0x01, service.convertType("1", FieldType.STRING, FieldType.BYTE));
        assertEquals('1', service.convertType("1", FieldType.STRING, FieldType.CHAR));
        assertEquals(LocalDate.class, service.convertType("1970-01-01", FieldType.STRING, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType("1970-01-01T00:00:00.000Z", FieldType.STRING, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType("1970-01-01T00:00:00.000Z", FieldType.STRING, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType("1970-01-01T00:00:00.000Z", FieldType.STRING, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType("1970-01-01T00:00:00.000Z", FieldType.STRING, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.valueOf(1), service.convertType("1", FieldType.STRING, FieldType.DECIMAL));
        assertEquals(1.0d, service.convertType("1", FieldType.STRING, FieldType.DOUBLE));
        assertEquals(1.0f, service.convertType("1", FieldType.STRING, FieldType.FLOAT));
        assertEquals(1, service.convertType("1", FieldType.STRING, FieldType.INTEGER));
        assertEquals(1L, service.convertType("1", FieldType.STRING, FieldType.LONG));
        assertEquals(BigInteger.valueOf(1), service.convertType("1", FieldType.STRING, FieldType.NUMBER));
        assertEquals((short)1, service.convertType("1", FieldType.STRING, FieldType.SHORT));
        assertEquals("1", service.convertType("1", FieldType.STRING, FieldType.STRING));
        assertEquals(LocalTime.class, service.convertType("00:00:00.000", FieldType.STRING, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeFromZonedDateTime() throws AtlasConversionException {
        assertEquals(BigInteger.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.BIG_INTEGER).getClass());
        assertEquals(LocalDate.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.DATE).getClass());
        assertEquals(Date.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.DATE_TIME).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.DATE_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.TIME_TZ).getClass());
        assertEquals(ZonedDateTime.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.DATE_TIME_TZ).getClass());
        assertEquals(BigDecimal.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.DECIMAL).getClass());
        assertEquals(Double.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.DOUBLE).getClass());
        assertEquals(Float.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.FLOAT).getClass());
        assertEquals(Long.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.LONG).getClass());
        assertEquals(Long.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.NUMBER).getClass());
        assertEquals(String.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.STRING).getClass());
        assertEquals(LocalTime.class, service.convertType(ZonedDateTime.now(), FieldType.DATE_TIME_TZ, FieldType.TIME).getClass());
    }

    @Test
    public void testConvertTypeToAny() throws AtlasConversionException {
        assertEquals("passthrough", service.convertType("passthrough", FieldType.ANY, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.BIG_INTEGER, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.BOOLEAN, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.BYTE, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.CHAR, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.DATE, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.DATE_TIME, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.DATE_TZ, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.TIME_TZ, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.DATE_TIME, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.DECIMAL, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.DOUBLE, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.FLOAT, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.INTEGER, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.LONG, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.NUMBER, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.SHORT, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.STRING, FieldType.ANY));
        assertEquals("passthrough", service.convertType("passthrough", FieldType.TIME, FieldType.ANY));
    }

    @Test(expected = AtlasConversionException.class)
    public void testConvertTypeAtlasConversionException() throws AtlasConversionException {
        assertNotNull(service.convertType(new Object(), null, null));
    }

}
