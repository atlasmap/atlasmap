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
package io.atlasmap.itests.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.xmlunit.assertj.XmlAssert.assertThat;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.Source;

import org.w3c.dom.Node;
import org.xmlunit.builder.Input;
import org.xmlunit.xpath.JAXPXPathEngine;

import io.atlasmap.java.test.BaseAddress;
import io.atlasmap.java.test.BaseContact;
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.BaseOrderList;

public class AtlasTestUtil {

    public static BaseFlatPrimitiveClass generateFlatPrimitiveClass(Class<? extends BaseFlatPrimitiveClass> clazz)
            throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(clazz.getName());
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass) targetClazz.getDeclaredConstructor().newInstance();

        newObject.setIntField(2);
        newObject.setShortField((short) 3);
        newObject.setLongField(4L);
        newObject.setDoubleField(5d);
        newObject.setFloatField(6f);
        newObject.setBooleanField(true);
        newObject.setCharField('8');
        newObject.setByteField((byte) 57);

        return newObject;
    }

    public static BaseOrderList generateOrderListClass(Class<? extends BaseOrderList> orderListClazz,
            Class<? extends BaseOrder> orderClazz, Class<? extends BaseAddress> addressClazz,
            Class<? extends BaseContact> contactClazz) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(orderListClazz.getName());
        BaseOrderList orderList = (BaseOrderList) targetClazz.getDeclaredConstructor().newInstance();
        orderList.setNumberOrders(5);
        orderList.setOrderBatchNumber(4123562);

        for (int i = 0; i < 5; i++) {
            BaseOrder baseOrder = generateOrderClass(orderClazz, addressClazz, contactClazz);
            baseOrder.setOrderId(i);
            if (orderList.getOrders() == null) {
                orderList.setOrders(new ArrayList<BaseOrder>());
            }
            orderList.getOrders().add(baseOrder);
        }
        return orderList;
    }

    public static BaseOrder generateOrderClass(Class<? extends BaseOrder> orderClazz,
            Class<? extends BaseAddress> addressClazz, Class<? extends BaseContact> contactClazz) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(orderClazz.getName());
        BaseOrder newObject = (BaseOrder) targetClazz.getDeclaredConstructor().newInstance();

        newObject.setOrderId(8765309);
        newObject.setAddress(generateAddress(addressClazz));
        newObject.setContact(generateContact(contactClazz));
        return newObject;
    }

    public static BaseAddress generateAddress(Class<? extends BaseAddress> addressClass) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(addressClass.getName());
        BaseAddress newObject = (BaseAddress) targetClazz.getDeclaredConstructor().newInstance();

        newObject.setAddressLine1("123 Main St");
        newObject.setAddressLine2("Suite 42b");
        newObject.setCity("Anytown");
        newObject.setState("NY");
        newObject.setZipCode("90210");
        return newObject;
    }

    public static BaseContact generateContact(Class<? extends BaseContact> contactClass) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(contactClass.getName());
        BaseContact newObject = (BaseContact) targetClazz.getDeclaredConstructor().newInstance();

        newObject.setFirstName("Ozzie");
        newObject.setLastName("Smith");
        newObject.setPhoneNumber("5551212");
        newObject.setZipCode("81111");
        return newObject;
    }

    public static void validateOrderList(BaseOrderList orderListObject) {
        assertNotNull(orderListObject);
        assertNotNull(orderListObject.getNumberOrders());
        assertNotNull(orderListObject.getOrderBatchNumber());

        assertEquals(Integer.valueOf(5), orderListObject.getNumberOrders());
        assertEquals(Integer.valueOf(4123562), orderListObject.getOrderBatchNumber());

        if (orderListObject.getOrders() != null) {
            for (int i = 0; i < orderListObject.getOrders().size(); i++) {
                validateOrder(orderListObject.getOrders().get(i), i);
            }
        }
    }

    public static void validateOrderList(NoAbstractTargetOrderList orderListObject) {
        assertNotNull(orderListObject);
        assertNotNull(orderListObject.getNumberOrders());
        assertNotNull(orderListObject.getOrderBatchNumber());

        assertEquals(Integer.valueOf(5), orderListObject.getNumberOrders());
        assertEquals(Integer.valueOf(4123562), orderListObject.getOrderBatchNumber());

        if (orderListObject.getOrders() != null) {
            for (int i = 0; i < orderListObject.getOrders().size(); i++) {
                validateOrderNoAbstract(orderListObject.getOrders().get(i), i);
            }
        }
    }

    public static void validateOrderNoAbstract(BaseOrder orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(Integer.valueOf(expectedOrderId), orderObject.getOrderId());
        // https://github.com/atlasmap/atlasmap-runtime/issues/229 - Allow default
        // implementation for abstract target field
        assertNull(orderObject.getAddress());
        assertNull(orderObject.getContact());
    }

    public static void validateOrder(BaseOrder orderObject) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(Integer.valueOf(8765309), orderObject.getOrderId());
        validateAddress(orderObject.getAddress());
        validateContact(orderObject.getContact());
    }

    public static void validateOrder(BaseOrder orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(Integer.valueOf(expectedOrderId), orderObject.getOrderId());
        validateAddress(orderObject.getAddress());
        validateContact(orderObject.getContact());
    }

    public static void validateAddress(BaseAddress addressObject) {
        assertNotNull(addressObject);
        assertEquals("123 Main St", addressObject.getAddressLine1());
        assertEquals("Suite 42b", addressObject.getAddressLine2());
        assertEquals("Anytown", addressObject.getCity());
        assertEquals("NY", addressObject.getState());
        assertEquals("90210", addressObject.getZipCode());
    }

    public static void validateContact(BaseContact contactObject) {
        assertNotNull(contactObject);
        assertEquals("Ozzie", contactObject.getFirstName());
        assertEquals("Smith", contactObject.getLastName());
        assertEquals("5551212", contactObject.getPhoneNumber());
        assertEquals("81111", contactObject.getZipCode());
    }

    public static String loadFileAsString(String filename) throws Exception {
        return new String(Files.readAllBytes(Paths.get(filename)));
    }

    protected static void unusedPrimativeMappingAsserts(BaseFlatPrimitiveClass targetObject) {
        // Unused by mapping
        assertNull(targetObject.getBooleanArrayField());
        assertNull(targetObject.getBoxedBooleanArrayField());
        assertNull(targetObject.getBoxedBooleanField());
        assertNull(targetObject.getBoxedByteArrayField());
        assertNull(targetObject.getBoxedByteField());
        assertNull(targetObject.getBoxedCharArrayField());
        assertNull(targetObject.getBoxedCharField());
        assertNull(targetObject.getBoxedDoubleArrayField());
        assertNull(targetObject.getBoxedDoubleField());
        assertNull(targetObject.getBoxedFloatArrayField());
        assertNull(targetObject.getBoxedFloatField());
        assertNull(targetObject.getBoxedIntArrayField());
        assertNull(targetObject.getBoxedIntField());
        assertNull(targetObject.getBoxedLongArrayField());
        assertNull(targetObject.getBoxedLongField());
        assertNull(targetObject.getBoxedShortArrayField());
        assertNull(targetObject.getBoxedShortField());
        assertNull(targetObject.getBoxedStringArrayField());
        assertNull(targetObject.getBoxedStringField());
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion1(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(3), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 4), Short.valueOf(targetObject.getShortField()));
        assertEquals(Long.valueOf(5L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Double.valueOf(6d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(1f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(Character.valueOf('9'), Character.valueOf(targetObject.getCharField()));
        assertEquals(Byte.valueOf("2"), Byte.valueOf(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion2(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(4), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 5), Short.valueOf(targetObject.getShortField()));
        assertEquals(Long.valueOf(6L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Double.valueOf(1.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(56f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(Character.valueOf('\u0002'), Character.valueOf(targetObject.getCharField()));
        assertEquals(Byte.valueOf((byte) 3), Byte.valueOf(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion3(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(5), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 6), Short.valueOf(targetObject.getShortField()));
        assertEquals(Long.valueOf(1L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Double.valueOf(56d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(57.0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(Character.valueOf('\u0003'), Character.valueOf(targetObject.getCharField()));
        assertEquals(Byte.valueOf((byte) 4), Byte.valueOf(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion4(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(6), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 1), Short.valueOf(targetObject.getShortField()));
        assertEquals(Long.valueOf(56L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Double.valueOf(57.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(2.0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(Character.valueOf('\u0004'), Character.valueOf(targetObject.getCharField()));
        assertEquals(Byte.valueOf((byte) 5), Byte.valueOf(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion5(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(1), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 56), Short.valueOf(targetObject.getShortField()));
        assertEquals(Long.valueOf(57L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Double.valueOf(2.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(3.0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(Character.valueOf('\u0005'), Character.valueOf(targetObject.getCharField()));
        assertEquals(Byte.valueOf((byte) 6), Byte.valueOf(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion6(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(56), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 57), Short.valueOf(targetObject.getShortField()));
        assertEquals(Long.valueOf(2L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Double.valueOf(3.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(4.0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(Character.valueOf('\u0006'), Character.valueOf(targetObject.getCharField()));
        assertEquals(Byte.valueOf((byte) 1), Byte.valueOf(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion7(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(57), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 2), Short.valueOf(targetObject.getShortField()));
        assertEquals(Long.valueOf(3L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Double.valueOf(4.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(5.0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(Character.valueOf('\u0001'), Character.valueOf(targetObject.getCharField()));
        assertEquals(Byte.valueOf((byte) 56), Byte.valueOf(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFields(io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(50000000d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(40000000d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(2), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(30000L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Short.valueOf((short) 1), Short.valueOf(targetObject.getShortField().shortValue()));
        assertEquals(Boolean.FALSE, targetObject.getBooleanField());
        // assertEquals(new Byte((byte) 99), new Byte(targetObject.getByteField()));
        assertEquals(Character.valueOf('a'), Character.valueOf(targetObject.getCharField().charAt(0)));
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
        // assertNull(targetObject.getBoxedgetBoxedByteArrayField());
        // assertNull(targetObject.getBoxedByteField());
        assertNotNull(targetObject.getBoxedCharArrayField());
        assertTrue(targetObject.getBoxedCharArrayField().isEmpty());
        assertNull(targetObject.getBoxedCharField());
        assertNotNull(targetObject.getBoxedDoubleArrayField());
        assertTrue(targetObject.getBoxedDoubleArrayField().isEmpty());
        assertNull(targetObject.getBoxedDoubleField());
        assertNotNull(targetObject.getBoxedFloatArrayField());
        assertTrue(targetObject.getBoxedFloatArrayField().isEmpty());
        assertNull(targetObject.getBoxedFloatField());
        assertNotNull(targetObject.getBoxedIntArrayField());
        assertTrue(targetObject.getBoxedIntArrayField().isEmpty());
        assertNull(targetObject.getBoxedIntField());
        assertNotNull(targetObject.getBoxedLongArrayField());
        assertTrue(targetObject.getBoxedLongArrayField().isEmpty());
        assertNull(targetObject.getBoxedLongField());
        assertNotNull(targetObject.getBoxedShortArrayField());
        assertTrue(targetObject.getBoxedShortArrayField().isEmpty());
        assertNull(targetObject.getBoxedShortField());
        assertNotNull(targetObject.getBoxedStringArrayField());
        assertTrue(targetObject.getBoxedStringArrayField().isEmpty());
        assertNull(targetObject.getBoxedStringField());
    }

    public static void validateJsonFlatPrimitiveBoxedPrimitiveFields(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertEquals(Double.valueOf(90000000d), Double.valueOf(targetObject.getBoxedDoubleField()));
        assertEquals(Double.valueOf(70000000d), Double.valueOf(targetObject.getBoxedFloatField()));
        assertEquals(Integer.valueOf(5), Integer.valueOf(targetObject.getBoxedIntField()));
        assertEquals(Long.valueOf(20000L), Long.valueOf(targetObject.getBoxedLongField().longValue()));
        assertEquals(Short.valueOf((short) 5), Short.valueOf(targetObject.getBoxedShortField().shortValue()));
        assertEquals(Boolean.TRUE, targetObject.getBoxedBooleanField());
        // assertEquals(new Byte((byte) 87), new
        // Byte(targetObject.getBoxedByteField()));
        assertEquals(Character.valueOf('z'), Character.valueOf(targetObject.getBoxedCharField().charAt(0)));
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBooleanField());
        // assertNull(targetObject.getBoxedByteArrayField());
        // assertTrue((byte)0 == targetObject.getByteField());
        assertNotNull(targetObject.getBoxedCharArrayField());
        assertTrue(targetObject.getBoxedCharArrayField().isEmpty());
        assertNull(targetObject.getCharField());
        assertNotNull(targetObject.getBoxedDoubleArrayField());
        assertTrue(targetObject.getBoxedDoubleArrayField().isEmpty());
        assertNull(targetObject.getDoubleField());
        assertNotNull(targetObject.getBoxedFloatArrayField());
        assertTrue(targetObject.getBoxedFloatArrayField().isEmpty());
        assertNull(targetObject.getFloatField());
        assertNotNull(targetObject.getBoxedIntArrayField());
        assertTrue(targetObject.getBoxedIntArrayField().isEmpty());
        assertNull(targetObject.getIntField());
        assertNotNull(targetObject.getBoxedLongArrayField());
        assertTrue(targetObject.getBoxedLongArrayField().isEmpty());
        assertNull(targetObject.getLongField());
        assertNotNull(targetObject.getBoxedShortArrayField());
        assertTrue(targetObject.getBoxedShortArrayField().isEmpty());
        assertNull(targetObject.getShortField());
        assertNotNull(targetObject.getBoxedStringArrayField());
        assertTrue(targetObject.getBoxedStringArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedStringField());
        assertEquals("boxedStringValue", targetObject.getBoxedStringField());
    }

    public static void validateJsonOrder(io.atlasmap.json.test.BaseOrder orderObject) {
        assertNotNull(orderObject, "Order object is null");
        assertNotNull(orderObject.getOrderId(), "orderId is null: " + orderObject.toString());
        assertEquals(Integer.valueOf(8765309), orderObject.getOrderId());
        validateJsonAddress(orderObject.getAddress());
        validateJsonContact(orderObject.getContact());
    }

    public static void validateJsonOrder(io.atlasmap.json.test.BaseOrder orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(Integer.valueOf(expectedOrderId), orderObject.getOrderId());
        validateJsonAddress(orderObject.getAddress());
        validateJsonContact(orderObject.getContact());
    }

    public static void validateJsonAddress(io.atlasmap.json.test.BaseAddress addressObject) {
        assertNotNull(addressObject, "Address object is null");
        assertEquals("123 Main St", addressObject.getAddressLine1());
        assertEquals("Suite 42b", addressObject.getAddressLine2());
        assertEquals("Anytown", addressObject.getCity());
        assertEquals("NY", addressObject.getState());
        assertEquals("90210", addressObject.getZipCode());
    }

    public static void validateJsonContact(io.atlasmap.json.test.BaseContact contactObject) {
        assertNotNull(contactObject, "Contact object is null");
        assertEquals("Ozzie", contactObject.getFirstName());
        assertEquals("Smith", contactObject.getLastName());
        assertEquals("5551212", contactObject.getPhoneNumber());
        assertEquals("81111", contactObject.getZipCode());
    }

    protected static void unusedJsonPrimativeMappingAsserts(io.atlasmap.json.test.BaseFlatPrimitive targetObject) {

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
        assertNotNull(targetObject.getBoxedCharArrayField());
        assertTrue(targetObject.getBoxedCharArrayField().isEmpty());
        assertNull(targetObject.getBoxedCharField());
        assertNotNull(targetObject.getBoxedDoubleArrayField());
        assertTrue(targetObject.getBoxedDoubleArrayField().isEmpty());
        assertNull(targetObject.getBoxedDoubleField());
        assertNotNull(targetObject.getBoxedFloatArrayField());
        assertTrue(targetObject.getBoxedFloatArrayField().isEmpty());
        assertNull(targetObject.getBoxedFloatField());
        assertNotNull(targetObject.getBoxedIntArrayField());
        assertTrue(targetObject.getBoxedIntArrayField().isEmpty());
        assertNull(targetObject.getBoxedIntField());
        assertNotNull(targetObject.getBoxedLongArrayField());
        assertTrue(targetObject.getBoxedLongArrayField().isEmpty());
        assertNull(targetObject.getBoxedLongField());
        assertNotNull(targetObject.getBoxedShortArrayField());
        assertTrue(targetObject.getBoxedShortArrayField().isEmpty());
        assertNull(targetObject.getBoxedShortField());
        assertNotNull(targetObject.getBoxedStringArrayField());
        assertTrue(targetObject.getBoxedStringArrayField().isEmpty());
        assertNull(targetObject.getBoxedStringField());
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion1(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(3), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 4), Short.valueOf(targetObject.getShortField().shortValue()));
        assertEquals(Long.valueOf(5L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Double.valueOf(6d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(1.0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(Character.valueOf('9'), Character.valueOf(targetObject.getCharField().charAt(0)));
        assertEquals(Byte.valueOf((byte) 2), Byte.valueOf(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion2(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(4), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 5), Short.valueOf(targetObject.getShortField().shortValue()));
        assertEquals(Long.valueOf(6L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Double.valueOf(1.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(56d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion3(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(5), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 6), Short.valueOf(targetObject.getShortField().shortValue()));
        assertEquals(Long.valueOf(1L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Double.valueOf(56.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(57.0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(Character.valueOf('\u0003'), Character.valueOf(targetObject.getCharField().charAt(0)));
        assertEquals(Byte.valueOf((byte) 4), Byte.valueOf(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);

    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion4(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(6), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 1), Short.valueOf(targetObject.getShortField().shortValue()));
        assertEquals(Long.valueOf(56L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Double.valueOf(57.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(2.0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(Character.valueOf('\u0004'), Character.valueOf(targetObject.getCharField().charAt(0)));
        assertEquals(Byte.valueOf((byte) 5), Byte.valueOf(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion5(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(1), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 56), Short.valueOf(targetObject.getShortField().shortValue()));
        assertEquals(Long.valueOf(57L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Double.valueOf(2.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(3.0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(Character.valueOf('\u0005'), Character.valueOf(targetObject.getCharField().charAt(0)));
        assertEquals(Byte.valueOf((byte) 6), Byte.valueOf(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion6(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(56), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 57), Short.valueOf(targetObject.getShortField().shortValue()));
        assertEquals(Long.valueOf(2L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Double.valueOf(3.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(4.0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(Character.valueOf('\u0006'), Character.valueOf(targetObject.getCharField().charAt(0)));
        assertEquals(Byte.valueOf((byte) 1), Byte.valueOf(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion7(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Integer.valueOf(57), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Short.valueOf((short) 2), Short.valueOf(targetObject.getShortField().shortValue()));
        assertEquals(Long.valueOf(3L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Double.valueOf(4.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(5.0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(Character.valueOf('\u0001'), Character.valueOf(targetObject.getCharField().charAt(0)));
        assertEquals(Byte.valueOf((byte) 56), Byte.valueOf(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateXmlOrderElement(Object orderObject) {
        validateXmlOrderElement(orderObject, 8765309);
    }

    public static void validateXmlOrderElement(Object orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(orderObject).withNamespaceContext(ns).valueByXPath("/ns:XmlOE/ns:orderId").isEqualTo(expectedOrderId);
        Source source = Input.from(orderObject).build();
        JAXPXPathEngine engine = new JAXPXPathEngine();
        engine.setNamespaceContext(ns);
        Iterable<Node> addresses = engine.selectNodes("/ns:XmlOE/ns:Address", source);
        validateXmlAddressElement(addresses.iterator().next());
        Iterable<Node> contacts = engine.selectNodes("/ns:XmlOE/ns:Contact", source);
        validateXmlContactElement(contacts.iterator().next());
    }

    public static void validateXmlAddressElement(Object addressObject) {
        assertNotNull(addressObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:addressLine1").isEqualTo("123 Main St");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:Address/ns:addressLine2").isEqualTo("Suite 42b");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:Address/ns:city").isEqualTo("Anytown");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:Address/ns:state").isEqualTo("NY");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:Address/ns:zipCode").isEqualTo("90210");
    }

    public static void validateXmlContactElement(Object contactObject) {
        assertNotNull(contactObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(contactObject).withNamespaceContext(ns).valueByXPath("//ns:Contact/ns:firstName").isEqualTo("Ozzie");
        assertThat(contactObject).withNamespaceContext(ns).valueByXPath("//ns:Contact/ns:lastName").isEqualTo("Smith");
        assertThat(contactObject).withNamespaceContext(ns).valueByXPath("//ns:Contact/ns:phoneNumber").isEqualTo("5551212");
        assertThat(contactObject).withNamespaceContext(ns).valueByXPath("//ns:Contact/ns:zipCode").isEqualTo("81111");
    }

    public static void validateXmlOrderAttribute(Object orderObject) {
        validateXmlOrderAttribute(orderObject, 8765309);
    }

    public static void validateXmlOrderAttribute(Object orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(orderObject).withNamespaceContext(ns).valueByXPath("/ns:XmlOA/@orderId").isEqualTo(expectedOrderId);
        Source source = Input.from(orderObject).build();
        JAXPXPathEngine engine = new JAXPXPathEngine();
        engine.setNamespaceContext(ns);
        Iterable<Node> addresses = engine.selectNodes("/ns:XmlOA/ns:Address", source);
        validateXmlAddressAttribute(addresses.iterator().next());
        Iterable<Node> contacts = engine.selectNodes("/ns:XmlOA/ns:Contact", source);
        validateXmlContactAttribute(contacts.iterator().next());
    }

    public static void validateXmlAddressAttribute(Object addressObject) {
        assertNotNull(addressObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:Address/@addressLine1").isEqualTo("123 Main St");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:Address/@addressLine2").isEqualTo("Suite 42b");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:Address/@city").isEqualTo("Anytown");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:Address/@state").isEqualTo("NY");
        assertThat(addressObject).withNamespaceContext(ns).valueByXPath("//ns:Address/@zipCode").isEqualTo("90210");
    }

    public static void validateXmlContactAttribute(Object contactObject) {
        assertNotNull(contactObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(contactObject).withNamespaceContext(ns).valueByXPath("//ns:Contact/@firstName").isEqualTo("Ozzie");
        assertThat(contactObject).withNamespaceContext(ns).valueByXPath("//ns:Contact/@lastName").isEqualTo("Smith");
        assertThat(contactObject).withNamespaceContext(ns).valueByXPath("//ns:Contact/@phoneNumber").isEqualTo("5551212");
        assertThat(contactObject).withNamespaceContext(ns).valueByXPath("//ns:Contact/@zipCode").isEqualTo("81111");
    }

    public static void validateXmlContactAttributeNoNS(Object contactObject) {
        assertNotNull(contactObject);
        assertThat(contactObject).valueByXPath("//Contact/@firstName").isEqualTo("Ozzie");
        assertThat(contactObject).valueByXPath("//Contact/@lastName").isEqualTo("Smith");
        assertThat(contactObject).valueByXPath("//Contact/@phoneNumber").isEqualTo("5551212");
        assertThat(contactObject).valueByXPath("//Contact/@zipCode").isEqualTo("81111");
    }

    public static void validateXmlFlatPrimitiveElement(Object targetObject) {
        assertNotNull(targetObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPE/ns:doubleField").isEqualTo(50000000d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPE/ns:floatField").isEqualTo(40000000f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPE/ns:intField").isEqualTo(2);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPE/ns:longField").isEqualTo(30000L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPE/ns:shortField").isEqualTo((short)1);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPE/ns:booleanField").isEqualTo(false);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPE/ns:charField").isEqualTo("a");
    }

    public static void validateXmlFlatPrimitiveAttribute(Object targetObject) {
        assertNotNull(targetObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPA/@doubleField").isEqualTo(50000000d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPA/@floatField").isEqualTo(40000000f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPA/@intField").isEqualTo(2);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPA/@longField").isEqualTo(30000L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPA/@shortField").isEqualTo((short)1);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPA/@booleanField").isEqualTo(false);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFPA/@charField").isEqualTo("a");
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion1(Object targetObject) {
        assertNotNull(targetObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("xt1", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:intField").isEqualTo(3);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:shortField").isEqualTo((short)4);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:longField").isEqualTo(5L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:doubleField").isEqualTo(6d);
        //assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:floatField").isEqualTo(1f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:booleanField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:charField").isEqualTo(9);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:byteField").isEqualTo((byte)2);
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion2(Object targetObject) {
        assertNotNull(targetObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("xt1", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:intField").isEqualTo(4);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:shortField").isEqualTo((short)5);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:longField").isEqualTo(6L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:doubleField").isEqualTo(1.0d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:floatField").isEqualTo(56f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:booleanField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:charField").isEqualTo(2);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:byteField").isEqualTo((byte) 3);
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion3(Object targetObject) {
        assertNotNull(targetObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("xt1", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:intField").isEqualTo(5);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:shortField").isEqualTo((short)6);
        //assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:longField").isEqualTo(1L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:doubleField").isEqualTo(56d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:floatField").isEqualTo(57f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:booleanField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:charField").isEqualTo(3);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:byteField").isEqualTo((byte) 4);
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion4(Object targetObject) {
        assertNotNull(targetObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("xt1", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:intField").isEqualTo(6);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:shortField").isEqualTo((short) 1);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:longField").isEqualTo(56L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:doubleField").isEqualTo(57.0d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:floatField").isEqualTo(2.0f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:booleanField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:charField").isEqualTo(4);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:byteField").isEqualTo((byte)5);
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion5(Object targetObject) {
        assertNotNull(targetObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("xt1", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:intField").isEqualTo(1);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:shortField").isEqualTo((short) 56);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:longField").isEqualTo(57L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:doubleField").isEqualTo(2.0d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:floatField").isEqualTo(3.0f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:booleanField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:charField").isEqualTo(5.0);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:byteField").isEqualTo((byte) 6);
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion6(Object targetObject) {
        assertNotNull(targetObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("xt1", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:intField").isEqualTo(56);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:shortField").isEqualTo((short) 57);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:longField").isEqualTo(2L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:doubleField").isEqualTo(3.0d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:floatField").isEqualTo(4.0f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:booleanField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:charField").isEqualTo(6.0d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:byteField").isEqualTo((byte) 1);
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion7(Object targetObject) {
        assertNotNull(targetObject);
        HashMap<String,String> ns = new HashMap<>();
        ns.put("xt1", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:intField").isEqualTo(57);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:shortField").isEqualTo((short) 2);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:longField").isEqualTo(3L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:doubleField").isEqualTo(4.0d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:floatField").isEqualTo(5.0f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:booleanField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:charField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/xt1:XmlFPE/xt1:byteField").isEqualTo((byte) 56);
    }

    public static void validateXmlFlatPrimitiveBoxedPrimitiveElementFields(Object targetObject) {
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPE/ns:boxedDoubleField").isEqualTo(90000000d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPE/ns:boxedFloatField").isEqualTo(70000000f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPE/ns:boxedIntField").isEqualTo(5);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPE/ns:boxedLongField").isEqualTo(20000L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPE/ns:boxedShortField").isEqualTo((short) 5);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPE/ns:boxedBooleanField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPE/ns:boxedByteField").isEqualTo((byte) 87);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPE/ns:boxedCharField").isEqualTo("z");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPE/ns:boxedStringField").isNullOrEmpty();
    }

    public static void validateXmlFlatPrimitiveBoxedPrimitiveAttributeFields(Object targetObject) {
        HashMap<String,String> ns = new HashMap<>();
        ns.put("ns", "http://atlasmap.io/xml/test/v2");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPA/@boxedDoubleField").isEqualTo(90000000d);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPA/@boxedFloatField").isEqualTo(70000000f);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPA/@boxedIntField").isEqualTo(5);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPA/@boxedLongField").isEqualTo(20000L);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPA/@boxedShortField").isEqualTo((short) 5);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPA/@boxedBooleanField").isEqualTo(true);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPA/@boxedByteField").isEqualTo((byte) 87);
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPA/@boxedCharField").isEqualTo("z");
        assertThat(targetObject).withNamespaceContext(ns).valueByXPath("/ns:XmlFBPA/@boxedStringField").isNullOrEmpty();
    }
}
