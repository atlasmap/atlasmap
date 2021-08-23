/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.java.test.TargetTestClassExtended;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.java.v2.Modifier;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

public class ClassInspectionServiceTest {

    private ClassInspectionService classInspectionService = null;

    @BeforeEach
    public void setUp() {
        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @AfterEach
    public void tearDown() {
        classInspectionService = null;
    }

    @Test
    public void testDetectArrayDimensions() {

        assertNull(classInspectionService.inspectClass(String.class, CollectionType.NONE, null).getArrayDimensions());
        assertEquals(Integer.valueOf(1), classInspectionService.inspectClass(int[].class, CollectionType.ARRAY, null).getArrayDimensions());
        assertEquals(Integer.valueOf(2), classInspectionService.inspectClass(String[][].class, CollectionType.ARRAY, null).getArrayDimensions());
        assertEquals(Integer.valueOf(3), classInspectionService.inspectClass(List[][][].class, CollectionType.ARRAY, null).getArrayDimensions());
        assertEquals(Integer.valueOf(4), classInspectionService.inspectClass(Map[][][][].class, CollectionType.ARRAY, null).getArrayDimensions());
        assertEquals(Integer.valueOf(64), classInspectionService.inspectClass(
                int[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][].class, CollectionType.ARRAY, null)
                .getArrayDimensions());
        // MAX_DIM_LIMIT NOTE: 255 is the JVM Spec limit
        assertEquals(Integer.valueOf(255), classInspectionService.inspectClass(
                int[][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][][].class, CollectionType.ARRAY, null)
                .getArrayDimensions());
    }

    @Test
    public void testDetectArrayClass() {
        assertEquals("int", classInspectionService.inspectClass(int[].class, CollectionType.ARRAY, null).getClassName());
        assertEquals("java.lang.String", classInspectionService.inspectClass(String[][].class, CollectionType.ARRAY, null).getClassName());
        assertEquals("java.util.List", classInspectionService.inspectClass(List[][][].class, CollectionType.ARRAY, null).getClassName());
        assertEquals("java.util.Map", classInspectionService.inspectClass(Map[][][][].class, CollectionType.ARRAY, null).getClassName());
    }

    @Test
    public void testDateTimeViaField() {
        JavaClass javaClass = classInspectionService.inspectClass(DateTimeField.class, CollectionType.NONE, null);
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
        JavaClass javaClass = classInspectionService.inspectClass(DateTimeGetter.class, CollectionType.NONE, null);
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
        JavaClass javaClass = classInspectionService.inspectClass(DateTimeSetter.class, CollectionType.NONE, null);
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

        JavaClass javaClass = classInspectionService.inspectClass(TestEnum.class, CollectionType.NONE, null);
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

    @Test
    public void testTopmostListString() {
        JavaClass javaClass = classInspectionService.inspectClass(String.class, CollectionType.LIST, null);
        assertEquals("java.lang.String", javaClass.getClassName());
        assertEquals(CollectionType.LIST, javaClass.getCollectionType());
        assertNull(javaClass.getCollectionClassName());
        assertEquals("/<>", javaClass.getPath());
        assertEquals(FieldType.STRING, javaClass.getFieldType());
        assertEquals(0, javaClass.getJavaFields().getJavaField().size());
        assertEquals(0, javaClass.getJavaEnumFields().getJavaEnumField().size());
    }

    @Test
    public void testTopmostArrayString() {
        JavaClass javaClass = classInspectionService.inspectClass(String.class, CollectionType.ARRAY, null);
        assertEquals("java.lang.String", javaClass.getClassName());
        assertEquals(CollectionType.ARRAY, javaClass.getCollectionType());
        assertNull(javaClass.getCollectionClassName());
        assertEquals("/[]", javaClass.getPath());
        assertEquals(FieldType.STRING, javaClass.getFieldType());
        assertEquals(0, javaClass.getJavaFields().getJavaField().size());
        assertEquals(0, javaClass.getJavaEnumFields().getJavaEnumField().size());
        javaClass = classInspectionService.inspectClass(String[].class, CollectionType.NONE, null);
        assertEquals("java.lang.String", javaClass.getClassName());
        assertEquals(CollectionType.ARRAY, javaClass.getCollectionType());
        assertNull(javaClass.getCollectionClassName());
        assertEquals("/[]", javaClass.getPath());
        assertEquals(FieldType.STRING, javaClass.getFieldType());
        assertEquals(0, javaClass.getJavaFields().getJavaField().size());
        assertEquals(0, javaClass.getJavaEnumFields().getJavaEnumField().size());
    }

    @Test
    public void testInnerClass() {
        JavaClass javaClass = classInspectionService.inspectClass(InnerClass.TheInnerClass.class, CollectionType.NONE, null);
        assertEquals("io.atlasmap.java.inspect.InnerClass$TheInnerClass", javaClass.getClassName());
        assertEquals(FieldType.COMPLEX, javaClass.getFieldType());
        assertEquals(1, javaClass.getJavaFields().getJavaField().size());
        JavaField f = javaClass.getJavaFields().getJavaField().get(0);
        assertEquals(FieldType.STRING, f.getFieldType());
        assertEquals("/someInnerString", f.getPath());
    }

    @Test
    public void testTargetTestClass() {
        JavaClass javaClass = classInspectionService.inspectClass(TargetTestClass.class, CollectionType.NONE, null);
        assertEquals(TargetTestClass.class.getName(), javaClass.getClassName());
        assertEquals(FieldType.COMPLEX, javaClass.getFieldType());
        JavaField orderArray = javaClass.getJavaFields().getJavaField().get(6);
        assertEquals(FieldType.COMPLEX, orderArray.getFieldType());
        assertEquals("/orderArray", orderArray.getPath());
        JavaField orderArrayOrders = ((JavaClass)orderArray).getJavaFields().getJavaField().get(0);
        assertEquals(CollectionType.ARRAY, orderArrayOrders.getCollectionType());
        assertEquals("/orderArray/orders[]", orderArrayOrders.getPath());
        JavaField listOrders = javaClass.getJavaFields().getJavaField().get(7);
        assertEquals(FieldType.COMPLEX, listOrders.getFieldType());
        assertEquals("/listOrders", listOrders.getPath());
        JavaField listOrdersOrders = ((JavaClass)listOrders).getJavaFields().getJavaField().get(0);
        assertEquals(CollectionType.LIST, listOrdersOrders.getCollectionType());
        assertEquals("/listOrders/orders<>", listOrdersOrders.getPath());
        JavaField contactList = javaClass.getJavaFields().getJavaField().get(12);
        assertEquals(CollectionType.LIST, contactList.getCollectionType());
        assertEquals("/contactList<>", contactList.getPath());
        JavaField contactArray = javaClass.getJavaFields().getJavaField().get(13);
        assertEquals(CollectionType.ARRAY, contactArray.getCollectionType());
        assertEquals("/contactArray[]", contactArray.getPath());
    }

    @Test
    public void testDuplicatedField() {
        JavaClass parent = classInspectionService.inspectClass(TargetTestClass.class, CollectionType.NONE, null);
        JavaClass extended = classInspectionService.inspectClass(TargetTestClassExtended.class, CollectionType.NONE, null);
        assertEquals(TargetTestClass.class.getName(), parent.getClassName());
        assertEquals(TargetTestClassExtended.class.getName(), extended.getClassName());
        assertEquals(
            parent.getJavaFields().getJavaField().size(),
            extended.getJavaFields().getJavaField().size()
        );
    }
}
