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

import java.io.File;
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
        assertNull(classInspectionService.detectArrayDimensions(null));
        assertEquals(new Integer(0), classInspectionService.detectArrayDimensions(String.class));
        assertEquals(new Integer(1), classInspectionService.detectArrayDimensions(int[].class));
        assertEquals(new Integer(2), classInspectionService.detectArrayDimensions(String[][].class));
        assertEquals(new Integer(3), classInspectionService.detectArrayDimensions(List[][][].class));
        assertEquals(new Integer(4), classInspectionService.detectArrayDimensions(Map[][][][].class));
        assertEquals(new Integer(64), classInspectionService.detectArrayDimensions(
                int[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][].class));
        // MAX_DIM_LIMIT NOTE: 255 is the JVM Spec limit
        assertEquals(new Integer(255), classInspectionService.detectArrayDimensions(
                int[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][].class));
    }

    @Test
    public void testDetectArrayClass() {
        assertNull(classInspectionService.detectArrayClass(null));
        assertEquals(String.class, classInspectionService.detectArrayClass(String.class));
        assertEquals(int.class, classInspectionService.detectArrayClass(int[].class));
        assertEquals(String.class, classInspectionService.detectArrayClass(String[][].class));
        assertEquals(List.class, classInspectionService.detectArrayClass(List[][][].class));
        assertEquals(Map.class, classInspectionService.detectArrayClass(Map[][][][].class));
    }

    @Test
    public void testClasspathToList() {
        // Null
        ClassInspectionService cis = new ClassInspectionService();
        assertNull(cis.classpathStringToList(null));

        // Zero
        assertNotNull(cis.classpathStringToList(""));
        assertEquals(new Integer(0), new Integer(cis.classpathStringToList("").size()));

        // One
        assertNotNull(cis.classpathStringToList("foo.jar"));
        assertEquals(new Integer(1), new Integer(cis.classpathStringToList("foo.jar").size()));
        assertEquals("foo.jar", cis.classpathStringToList("foo.jar").get(0));

        // Several
        assertNotNull(cis.classpathStringToList("foo.jar:bar.jar:blah.jar"));
        assertEquals(new Integer(3), new Integer(cis.classpathStringToList("foo.jar:bar.jar:blah.jar").size()));
        assertEquals("foo.jar", cis.classpathStringToList("foo.jar:bar.jar:blah.jar").get(0));
        assertEquals("bar.jar", cis.classpathStringToList("foo.jar:bar.jar:blah.jar").get(1));
        assertEquals("blah.jar", cis.classpathStringToList("foo.jar:bar.jar:blah.jar").get(2));

        // Several
        String tmpcp = File.separator + "foo.jar:" + File.separator + "bar.jar:" + File.separator + "blah.jar";
        assertNotNull(cis.classpathStringToList(tmpcp));
        assertEquals(new Integer(3), new Integer(cis.classpathStringToList(tmpcp).size()));
        assertEquals(File.separator + "foo.jar", cis.classpathStringToList(tmpcp).get(0));
        assertEquals(File.separator + "bar.jar", cis.classpathStringToList(tmpcp).get(1));
        assertEquals(File.separator + "blah.jar", cis.classpathStringToList(tmpcp).get(2));
    }

    @Test
    public void testDateTimeViaField() {
        JavaClass javaClass = classInspectionService.inspectClass(DateTimeField.class);
        // FIXME java.time.Month is Enum - https://github.com/atlasmap/atlasmap-runtime/issues/251
        // assertEquals(10, javaClass.getJavaFields().getJavaField().size());
        assertEquals(9, javaClass.getJavaFields().getJavaField().size());
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
            } else if ("date".equals(field.getName())) {
                assertEquals("java.util.Date", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PRIVATE));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PROTECTED));
                assertFalse(field.getModifiers().getModifier().contains(Modifier.PUBLIC));
                assertTrue(field.getModifiers().getModifier().contains(Modifier.PACKAGE_PRIVATE));
            } else if ("sqlDate".equals(field.getName())) {
                assertEquals("java.sql.Date", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
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
        assertEquals(10, javaClass.getJavaFields().getJavaField().size());
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
            } else if ("date".equals(field.getName())) {
                assertEquals("java.util.Date", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("getDate", field.getGetMethod());
            } else if ("sqlDate".equals(field.getName())) {
                assertEquals("java.sql.Date", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("getSqlDate", field.getGetMethod());
            } else {
                fail("Unsupported field was detected: " + field);
            }
        }
    }

    @Test
    public void testDateTimeViaSetter() {
        JavaClass javaClass = classInspectionService.inspectClass(DateTimeSetter.class);
        assertEquals(10, javaClass.getJavaFields().getJavaField().size());
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
            } else if ("date".equals(field.getName())) {
                assertEquals("java.util.Date", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("setDate", field.getSetMethod());
            } else if ("sqlDate".equals(field.getName())) {
                assertEquals("java.sql.Date", field.getClassName());
                assertEquals(FieldType.DATE_TIME_TZ, field.getFieldType());
                assertEquals("setSqlDate", field.getSetMethod());
            } else {
                fail("Unsupported field was detected: " + field);
            }
        }
    }

}
