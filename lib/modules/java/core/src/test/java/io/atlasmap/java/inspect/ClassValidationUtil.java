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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

public class ClassValidationUtil {

    public static void validateFlatPrimitiveClass(ClassInspectionService classInspectionService, Class<?> clazz,
            String className) {
        JavaClass flatClass = classInspectionService.inspectClass(clazz, CollectionType.NONE, null);
        validateFlatClass(flatClass);
        assertEquals(CollectionType.NONE, flatClass.getCollectionType());
        assertEquals(null, flatClass.getArrayDimensions());
        assertEquals(null, flatClass.getArraySize());
        assertFalse(flatClass.isInterface());
        assertEquals(className, flatClass.getClassName());
        validateFlatPrimitiveFields(flatClass);
    }

    public static void validateFlatPrimitiveClassArray(ClassInspectionService classInspectionService, Class<?> clazz,
            String className) {
        JavaClass flatClass = classInspectionService.inspectClass(clazz, CollectionType.NONE, null);
        validateFlatClass(flatClass);
        assertEquals(CollectionType.ARRAY, flatClass.getCollectionType());
        assertEquals(Integer.valueOf(1), flatClass.getArrayDimensions());
        assertEquals(null, flatClass.getArraySize());
        assertFalse(flatClass.isInterface());
        assertEquals(className, flatClass.getClassName());
        validateFlatPrimitiveFields(flatClass);
    }

    public static void validateFlatPrimitiveClassTwoDimArray(ClassInspectionService classInspectionService,
            Class<?> clazz, String className) {
        JavaClass flatClass = classInspectionService.inspectClass(clazz, CollectionType.NONE, null);
        validateFlatClass(flatClass);
        assertEquals(CollectionType.ARRAY, flatClass.getCollectionType());
        assertEquals(Integer.valueOf(2), flatClass.getArrayDimensions());
        assertEquals(null, flatClass.getArraySize());
        assertFalse(flatClass.isInterface());
        assertEquals(className, flatClass.getClassName());
        validateFlatPrimitiveFields(flatClass);
    }

    public static void validateFlatPrimitiveClassThreeDimArray(ClassInspectionService classInspectionService,
            Class<?> clazz, String className) {
        JavaClass flatClass = classInspectionService.inspectClass(clazz, CollectionType.NONE, null);
        validateFlatClass(flatClass);
        assertEquals(CollectionType.ARRAY, flatClass.getCollectionType());
        assertEquals(Integer.valueOf(3), flatClass.getArrayDimensions());
        assertFalse(flatClass.isInterface());
        assertEquals(className, flatClass.getClassName());
        validateFlatPrimitiveFields(flatClass);
    }

    public static void validateFlatPrimitiveInterface(ClassInspectionService classInspectionService, Class<?> clazz) {
        JavaClass flatClass = classInspectionService.inspectClass(clazz, CollectionType.NONE, null);
        validateFlatClass(flatClass);
        assertEquals("io.atlasmap.java.test.FlatPrimitiveInterface", flatClass.getClassName());
        validateFlatPrimitiveFields(flatClass);
    }

    public static void validateFlatPrimitiveInterfaceArray(ClassInspectionService classInspectionService,
            Class<?> clazz) {
        JavaClass flatClass = classInspectionService.inspectClass(clazz, CollectionType.NONE, null);
        validateFlatClass(flatClass);
        assertEquals(CollectionType.ARRAY, flatClass.getCollectionType());
        assertEquals(Integer.valueOf(1), flatClass.getArrayDimensions());
        assertTrue(flatClass.isInterface());
        assertEquals("io.atlasmap.java.test.FlatPrimitiveInterface", flatClass.getClassName());
        validateFlatPrimitiveFields(flatClass);
    }

    public static void validateFlatPrimitiveInterfaceTwoDimArray(ClassInspectionService classInspectionService,
            Class<?> clazz) {
        JavaClass flatClass = classInspectionService.inspectClass(clazz, CollectionType.NONE, null);
        validateFlatClass(flatClass);
        assertEquals(CollectionType.ARRAY, flatClass.getCollectionType());
        assertEquals(Integer.valueOf(2), flatClass.getArrayDimensions());
        assertTrue(flatClass.isInterface());
        assertEquals("io.atlasmap.java.test.FlatPrimitiveInterface", flatClass.getClassName());
        validateFlatPrimitiveFields(flatClass);
    }

    public static void validateFlatPrimitiveInterfaceThreeDimArray(ClassInspectionService classInspectionService,
            Class<?> clazz) {
        JavaClass flatClass = classInspectionService.inspectClass(clazz, CollectionType.NONE, null);
        validateFlatClass(flatClass);
        assertEquals(CollectionType.ARRAY, flatClass.getCollectionType());
        assertEquals(Integer.valueOf(3), flatClass.getArrayDimensions());
        assertTrue(flatClass.isInterface());
        assertEquals("io.atlasmap.java.test.FlatPrimitiveInterface", flatClass.getClassName());
        validateFlatPrimitiveFields(flatClass);
    }

    public static void validateFlatClass(JavaClass flatClass) {
        assertNotNull(flatClass);
        assertNotNull(flatClass.getClassName());
        assertFalse(flatClass.isAnnonymous());
        assertFalse(flatClass.isEnumeration());
        assertFalse(flatClass.isLocalClass());
        assertFalse(flatClass.isMemberClass());
        assertFalse(flatClass.isPrimitive());
        assertFalse(flatClass.isSynthetic());
        assertNotNull(flatClass.getUri());
        assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT, flatClass.getClassName()), flatClass.getUri());
    }

    public static void validateFlatPrimitiveFields(JavaClass flatClass) {
        assertNotNull(flatClass.getPackageName());
        assertEquals("io.atlasmap.java.test", flatClass.getPackageName());
        assertNotNull(flatClass.getJavaFields());
        assertNotNull(flatClass.getJavaFields().getJavaField());
        assertFalse(flatClass.getJavaFields().getJavaField().isEmpty());
        assertEquals(Integer.valueOf(35), Integer.valueOf(flatClass.getJavaFields().getJavaField().size()));
        assertNotNull(flatClass.getJavaEnumFields());
        assertNotNull(flatClass.getJavaEnumFields().getJavaEnumField());
        assertTrue(flatClass.getJavaEnumFields().getJavaEnumField().isEmpty());

        for (JavaField f : flatClass.getJavaFields().getJavaField()) {
            assertNotNull(f);
            JavaField j = f;
            assertNotNull(j.getName());
            switch (j.getFieldType()) {
            case BOOLEAN:
                if (CollectionType.ARRAY.equals(j.getCollectionType())) {
                    validatePrimitiveField("boolean", "Boolean", j, false);
                } else {
                    validatePrimitiveField("boolean", "Boolean", j, true);
                }
                break;
            case BYTE:
                validatePrimitiveField("byte", "Byte", j);
                break;
            case CHAR:
                validatePrimitiveField("char", "Char", j);
                break;
            case DOUBLE:
                validatePrimitiveField("double", "Double", j);
                break;
            case FLOAT:
                validatePrimitiveField("float", "Float", j);
                break;
            case INTEGER:
                validatePrimitiveField("int", "Int", j);
                break;
            case LONG:
                validatePrimitiveField("long", "Long", j);
                break;
            case SHORT:
                validatePrimitiveField("short", "Short", j);
                break;
            case STRING:
                validatePrimitiveField("string", "String", j);
                break;
            default:
                fail(String.format("Extra field detected: name:'%s', type:'%s'", j.getName(), j.getFieldType()));
            }
        }
    }

    public static void validatePrimitiveField(String lowName, String capName, JavaField j) {
        validatePrimitiveField(lowName, capName, j, false);
    }

    public static void validatePrimitiveField(String lowName, String capName, JavaField j, boolean usesIs) {
        assertNotNull("Field: " + j.getName(), j.getGetMethod());
        assertNotNull("Field: " + j.getName(), j.getSetMethod());
        assertNotNull(j.getFieldType(), "Field: " + j.getName());
        assertNull(j.getValue(), "Field: " + j.getName());
        assertNull(j.getAnnotations(), "Field: " + j.getName());

        assertEquals(FieldStatus.SUPPORTED, j.getStatus());
        assertTrue(j.isPrimitive());
        assertFalse(j.isSynthetic());

        String fieldText = "Field";
        if (CollectionType.ARRAY.equals(j.getCollectionType())) {
            fieldText = "ArrayField";
            assertEquals(Integer.valueOf(1), j.getArrayDimensions());
        } else if (CollectionType.LIST.equals(j.getCollectionType())) {
            fieldText = "ListField";
            assertNotNull(j.getCollectionClassName());
        }

        if (String.format("%s%s", lowName, fieldText).equals(j.getName())) {
            if (usesIs) {
                assertEquals(String.format("is%s%s", capName, fieldText), j.getGetMethod());
            } else {
                assertEquals(String.format("get%s%s", capName, fieldText), j.getGetMethod());
            }
            assertEquals(String.format("set%s%s", capName, fieldText), j.getSetMethod());
        } else if (String.format("boxed%s%s", capName, fieldText).equals(j.getName())) {
            assertEquals(String.format("getBoxed%s%s", capName, fieldText), j.getGetMethod());
            assertEquals(String.format("setBoxed%s%s", capName, fieldText), j.getSetMethod());
        } else {
            fail("Extra field detected: " + j.getName());
        }
    }

    public static void validateSimpleTestContact(JavaClass c) {
        assertNotNull(c);
        assertFalse(c.isAnnonymous());
        assertFalse(c.isAnnotation());
        assertTrue(c.getCollectionType() == null);
        assertFalse(c.isEnumeration());
        assertFalse(c.isInterface());
        assertFalse(c.isLocalClass());
        assertFalse(c.isMemberClass());
        assertFalse(c.isPrimitive());
        assertFalse(c.isSynthetic());
        assertNotNull(c.getUri());
        assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT, c.getClassName()), c.getUri());
        assertEquals("io.atlasmap.java.test.BaseContact", c.getClassName());
        assertEquals("io.atlasmap.java.test", c.getPackageName());
        assertNotNull(c.getJavaEnumFields());
        assertNotNull(c.getJavaEnumFields().getJavaEnumField());
        assertEquals(Integer.valueOf(0), Integer.valueOf(c.getJavaEnumFields().getJavaEnumField().size()));
        assertNotNull(c.getJavaFields());
        assertNotNull(c.getJavaFields().getJavaField());
        assertEquals(Integer.valueOf(5), Integer.valueOf(c.getJavaFields().getJavaField().size()));

        for (JavaField f : c.getJavaFields().getJavaField()) {
            switch (f.getName()) {
            case "serialVersionUID":
                validateSerialVersionUID(f);
                break;
            case "firstName":
                break;
            case "lastName":
                break;
            case "phoneNumber":
                break;
            case "zipCode":
                break;
            default:
                fail("Unexpected field detected: " + f.getName());
            }
        }
    }

    public static void validateSimpleTestAddress(JavaClass c) {
        assertNotNull(c);
        assertFalse(c.isAnnonymous());
        assertFalse(c.isAnnotation());
        // assertTrue(c.getCollectionType() == null || c.getCollectionT());
        assertFalse(c.isEnumeration());
        assertFalse(c.isInterface());
        assertFalse(c.isLocalClass());
        assertFalse(c.isMemberClass());
        assertFalse(c.isPrimitive());
        assertFalse(c.isSynthetic());
        assertNotNull(c.getUri());
        assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT, c.getClassName()), c.getUri());
        assertEquals("io.atlasmap.java.test.BaseAddress", c.getClassName());
        assertEquals("io.atlasmap.java.test", c.getPackageName());
        assertNotNull(c.getJavaEnumFields());
        assertNotNull(c.getJavaEnumFields().getJavaEnumField());
        assertEquals(Integer.valueOf(0), Integer.valueOf(c.getJavaEnumFields().getJavaEnumField().size()));
        assertNotNull(c.getJavaFields());
        assertNotNull(c.getJavaFields().getJavaField());
        assertEquals(Integer.valueOf(6), Integer.valueOf(c.getJavaFields().getJavaField().size()));

        for (JavaField f : c.getJavaFields().getJavaField()) {
            switch (f.getName()) {
            case "serialVersionUID":
                validateSerialVersionUID(f);
                break;
            case "addressLine1":
                break;
            case "addressLine2":
                break;
            case "city":
                break;
            case "state":
                break;
            case "zipCode":
                break;
            default:
                fail("Unexpected field detected: " + f.getName());
            }
        }
    }

    public static void validateSerialVersionUID(JavaField f) {
        assertNotNull(f);
        assertEquals("serialVersionUID", f.getName());
        assertEquals("long", f.getClassName());
        assertEquals(FieldType.LONG, f.getFieldType());
        assertEquals(true, f.isPrimitive());
        assertNull(f.getCollectionType());
        assertEquals(false, f.isSynthetic());
    }

    public static void validateOrderId(JavaField f) {
        assertNotNull(f);
        assertEquals("orderId", f.getName());
        assertEquals("java.lang.Integer", f.getClassName());
        assertEquals(FieldType.INTEGER, f.getFieldType());
        assertEquals(true, f.isPrimitive());
        assertNull(f.getCollectionType());
        assertEquals(false, f.isSynthetic());
    }

    public static void validateCreated(JavaField f) {
        assertNotNull(f);
        assertEquals("created", f.getName());
        assertEquals("java.util.Date", f.getClassName());
        assertEquals(FieldType.DATE_TIME, f.getFieldType());
        assertEquals(false, f.isPrimitive());
        assertNull(f.getCollectionType());
        assertEquals(false, f.isSynthetic());
    }

    public static void validateSomeStaticClass(JavaField f) {
        assertNotNull(f);
        assertEquals("someStaticClass", f.getName());
        assertEquals("io.atlasmap.java.test.BaseOrder$SomeStaticClass", f.getClassName());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        assertNotEquals(FieldStatus.NOT_FOUND, f.getStatus());
        assertEquals(false, f.isPrimitive());
        assertNull(f.getCollectionType());
        assertEquals(false, f.isSynthetic());
    }
}
