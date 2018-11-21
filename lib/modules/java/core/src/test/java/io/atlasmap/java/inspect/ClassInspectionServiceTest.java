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
package io.atlasmap.java.inspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.java.v2.Modifier;
import io.atlasmap.v2.FieldType;

public class ClassInspectionServiceTest {

    private ClassInspectionService classInspectionService = null;

    @Before
    public void setUp() {
        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @After
    public void tearDown() {
        classInspectionService = null;
    }

    @Test
    public void testDetectArrayDimensions() {

        assertNull(null, classInspectionService.inspectClass(String.class).getArrayDimensions());
        assertEquals(new Integer(1), classInspectionService.inspectClass(int[].class).getArrayDimensions());
        assertEquals(new Integer(2), classInspectionService.inspectClass(String[][].class).getArrayDimensions());
        assertEquals(new Integer(3), classInspectionService.inspectClass(List[][][].class).getArrayDimensions());
        assertEquals(new Integer(4), classInspectionService.inspectClass(Map[][][][].class).getArrayDimensions());
        assertEquals(new Integer(64), classInspectionService.inspectClass(
                int[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][].class)
                .getArrayDimensions());
        // MAX_DIM_LIMIT NOTE: 255 is the JVM Spec limit
        assertEquals(new Integer(255), classInspectionService.inspectClass(
                int[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][].class)
                .getArrayDimensions());
    }

    @Test
    public void testDetectArrayClass() {
        assertEquals("int", classInspectionService.inspectClass(int[].class).getClassName());
        assertEquals("java.lang.String", classInspectionService.inspectClass(String[][].class).getClassName());
        assertEquals("java.util.List", classInspectionService.inspectClass(List[][][].class).getClassName());
        assertEquals("java.util.Map", classInspectionService.inspectClass(Map[][][][].class).getClassName());
    }

    @Test
    public void testDateTimeViaField() {
        JavaClass javaClass = classInspectionService.inspectClass(DateTimeField.class);
        assertEquals(14, javaClass.getJavaFields().getJavaField().size());
        assertFalse(javaClass.getModifiers().getModifier().contains(Modifier.PRIVATE));
        assertFalse(javaClass.getModifiers().getModifier().contains(Modifier.PROTECTED));
        assertFalse(javaClass.getModifiers().getModifier().contains(Modifier.PUBLIC));
        assertTrue(javaClass.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
        for (JavaField field : javaClass.getJavaFields().getJavaField()) {
            if ("year".equals(field.getName())) {
                assertEquals("java.time.Year", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("month".equals(field.getName())) {
                assertEquals("java.time.Month", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("yearMonth".equals(field.getName())) {
                assertEquals("java.time.YearMonth", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("monthDay".equals(field.getName())) {
                assertEquals("java.time.MonthDay", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("localDate".equals(field.getName())) {
                assertEquals("java.time.LocalDate", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("localTime".equals(field.getName())) {
                assertEquals("java.time.LocalTime", field.getClassName());
                assertEquals(FieldType.TIME, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("localDateTime".equals(field.getName())) {
                assertEquals("java.time.LocalDateTime", field.getClassName());
                assertEquals(FieldType.DATE_TIME, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("zonedDateTime".equals(field.getName())) {
                assertEquals("java.time.ZonedDateTime", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("calendar".equals(field.getName())) {
                assertEquals("java.util.Calendar", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("date".equals(field.getName())) {
                assertEquals("java.util.Date", field.getClassName());
                assertEquals(FieldType.DATE_TIME, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("gregorianCalendar".equals(field.getName())) {
                assertEquals("java.util.GregorianCalendar", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("sqlDate".equals(field.getName())) {
                assertEquals("java.sql.Date", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("sqlTime".equals(field.getName())) {
                assertEquals("java.sql.Time", field.getClassName());
                assertEquals(FieldType.TIME, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("sqlTimestamp".equals(field.getName())) {
                assertEquals("java.sql.Timestamp", field.getClassName());
                assertEquals(FieldType.DATE_TIME, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else {
                fail("Unsupported field was detected: " + field);
            }
        }
    }

    @Test
    public void testDateTimeViaGetter() {
        JavaClass javaClass = classInspectionService.inspectClass(DateTimeGetter.class);
        assertEquals(14, javaClass.getJavaFields().getJavaField().size());
        for (JavaField field : javaClass.getJavaFields().getJavaField()) {
            if ("year".equals(field.getName())) {
                assertEquals("java.time.Year", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("getYear", field.getGetMethod());
            } else if ("month".equals(field.getName())) {
                assertEquals("java.time.Month", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("getMonth", field.getGetMethod());
            } else if ("yearMonth".equals(field.getName())) {
                assertEquals("java.time.YearMonth", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("getYearMonth", field.getGetMethod());
            } else if ("monthDay".equals(field.getName())) {
                assertEquals("java.time.MonthDay", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("getMonthDay", field.getGetMethod());
            } else if ("localDate".equals(field.getName())) {
                assertEquals("java.time.LocalDate", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("getLocalDate", field.getGetMethod());
            } else if ("localTime".equals(field.getName())) {
                assertEquals("java.time.LocalTime", field.getClassName());
                assertEquals(FieldType.TIME, field.getFieldType());
                assertEquals("getLocalTime", field.getGetMethod());
            } else if ("localDateTime".equals(field.getName())) {
                assertEquals("java.time.LocalDateTime", field.getClassName());
                assertEquals(FieldType.DATE_TIME, field.getFieldType());
                assertEquals("getLocalDateTime", field.getGetMethod());
            } else if ("zonedDateTime".equals(field.getName())) {
                assertEquals("java.time.ZonedDateTime", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("getZonedDateTime", field.getGetMethod());
            } else if ("calendar".equals(field.getName())) {
                assertEquals("java.util.Calendar", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("getCalendar", field.getGetMethod());
            } else if ("date".equals(field.getName())) {
                assertEquals("java.util.Date", field.getClassName());
                assertEquals(FieldType.DATE_TIME, field.getFieldType());
                assertEquals("getDate", field.getGetMethod());
            } else if ("gregorianCalendar".equals(field.getName())) {
                assertEquals("java.util.GregorianCalendar", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("getGregorianCalendar", field.getGetMethod());
            } else if ("sqlDate".equals(field.getName())) {
                assertEquals("java.sql.Date", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("getSqlDate", field.getGetMethod());
            } else if ("sqlTime".equals(field.getName())) {
                assertEquals("java.sql.Time", field.getClassName());
                assertEquals(FieldType.TIME, field.getFieldType());
                assertEquals("getSqlTime", field.getGetMethod());
            } else if ("sqlTimestamp".equals(field.getName())) {
                assertEquals("java.sql.Timestamp", field.getClassName());
                assertEquals(FieldType.DATE_TIME, field.getFieldType());
                assertEquals("getSqlTimestamp", field.getGetMethod());
            } else {
                fail("Unsupported field was detected: " + field);
            }
        }
    }

    @Test
    public void testDateTimeViaSetter() {
        JavaClass javaClass = classInspectionService.inspectClass(DateTimeSetter.class);
        assertEquals(14, javaClass.getJavaFields().getJavaField().size());
        for (JavaField field : javaClass.getJavaFields().getJavaField()) {
            if ("year".equals(field.getName())) {
                assertEquals("java.time.Year", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("setYear", field.getSetMethod());
            } else if ("month".equals(field.getName())) {
                assertEquals("java.time.Month", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("setMonth", field.getSetMethod());
            } else if ("yearMonth".equals(field.getName())) {
                assertEquals("java.time.YearMonth", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("setYearMonth", field.getSetMethod());
            } else if ("monthDay".equals(field.getName())) {
                assertEquals("java.time.MonthDay", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("setMonthDay", field.getSetMethod());
            } else if ("localDate".equals(field.getName())) {
                assertEquals("java.time.LocalDate", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("setLocalDate", field.getSetMethod());
            } else if ("localTime".equals(field.getName())) {
                assertEquals("java.time.LocalTime", field.getClassName());
                assertEquals(FieldType.TIME, field.getFieldType());
                assertEquals("setLocalTime", field.getSetMethod());
            } else if ("localDateTime".equals(field.getName())) {
                assertEquals("java.time.LocalDateTime", field.getClassName());
                assertEquals(FieldType.DATE_TIME, field.getFieldType());
                assertEquals("setLocalDateTime", field.getSetMethod());
            } else if ("zonedDateTime".equals(field.getName())) {
                assertEquals("java.time.ZonedDateTime", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("setZonedDateTime", field.getSetMethod());
            } else if ("calendar".equals(field.getName())) {
                assertEquals("java.util.Calendar", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("setCalendar", field.getSetMethod());
            } else if ("date".equals(field.getName())) {
                assertEquals("java.util.Date", field.getClassName());
                assertEquals(FieldType.DATE_TIME, field.getFieldType());
                assertEquals("setDate", field.getSetMethod());
            } else if ("gregorianCalendar".equals(field.getName())) {
                assertEquals("java.util.GregorianCalendar", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("setGregorianCalendar", field.getSetMethod());
            } else if ("sqlDate".equals(field.getName())) {
                assertEquals("java.sql.Date", field.getClassName());
                assertEquals(FieldType.DATE, field.getFieldType());
                assertEquals("setSqlDate", field.getSetMethod());
            } else if ("sqlTime".equals(field.getName())) {
                assertEquals("java.sql.Time", field.getClassName());
                assertEquals(FieldType.TIME, field.getFieldType());
                assertEquals("setSqlTime", field.getSetMethod());
            } else if ("sqlTimestamp".equals(field.getName())) {
                assertEquals("java.sql.Timestamp", field.getClassName());
                assertEquals(FieldType.DATE_TIME, field.getFieldType());
                assertEquals("setSqlTimestamp", field.getSetMethod());
            } else {
                fail("Unsupported field was detected: " + field);
            }
        }
    }

    @Test
    public void testEnum() {

        JavaClass javaClass = classInspectionService.inspectClass(TestEnum.class);
        assertNotNull(javaClass.getJavaFields());
        assertEquals(2, javaClass.getJavaFields().getJavaField().size());

        for (JavaField field : javaClass.getJavaFields().getJavaField()) {
            if ("extendedStatus".equals(field.getName())) {
                assertNotNull(((JavaClass) field).getJavaEnumFields());
                assertEquals(4, ((JavaClass) field).getJavaEnumFields().getJavaEnumField().size());

            } else if ("status".equals(field.getName())) {
                assertNotNull(field.getGetMethod());
                assertNotNull(field.getSetMethod());
                assertNotNull(((JavaClass) field).getJavaEnumFields());
                assertNotNull(((JavaClass) field).getJavaEnumFields());
                assertEquals(2, ((JavaClass) field).getJavaEnumFields().getJavaEnumField().size());

            }

        }

    }

}
