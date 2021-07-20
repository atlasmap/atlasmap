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
package io.atlasmap.itests.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

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

        newObject.setBooleanField(false);
        newObject.setByteField((byte) 99);
        newObject.setCharField('a');
        newObject.setDoubleField(50000000d);
        newObject.setFloatField(40000000f);
        newObject.setIntField(2);
        newObject.setLongField(30000L);
        newObject.setShortField((short) 1);
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

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion1(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(40000000d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(1), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(50000000L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Short.valueOf((short) 30000), Short.valueOf(targetObject.getShortField()));
        assertEquals(Character.valueOf('2'), Character.valueOf(targetObject.getCharField()));

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(Byte.valueOf((byte) 0), Byte.valueOf(targetObject.getByteField()));

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

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion2(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(0.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(97f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(30000), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(40000000L), Long.valueOf(targetObject.getLongField()));
        // TODO: Fix char validateion
        // assertTrue(Character.valueOf((char)1) == targetObject.getCharField());

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(Byte.valueOf((byte) 0), Byte.valueOf(targetObject.getByteField()));
        assertEquals(Short.valueOf((short) 0), Short.valueOf(targetObject.getShortField()));

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

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion3(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(Double.valueOf(97d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(2.0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(50000000), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(0L), Long.valueOf(targetObject.getLongField()));
        // TODO: fix char validation
        // assertTrue(Character.valueOf((char)30000) == targetObject.getCharField());

        // Primitive auto-initialized values
        assertEquals(Byte.valueOf((byte) 0), Byte.valueOf(targetObject.getByteField()));
        assertEquals(Short.valueOf((short) 0), Short.valueOf(targetObject.getShortField()));

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

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion4(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(2.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(1.0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(40000000), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(97L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Short.valueOf((short) 0), Short.valueOf(targetObject.getShortField()));

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(Byte.valueOf((byte) 0), Byte.valueOf(targetObject.getByteField()));
        assertTrue(Character.valueOf((char) 0) == targetObject.getCharField());

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

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion5(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(1.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(30000.0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(0), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(2L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Short.valueOf((short) 97), Short.valueOf(targetObject.getShortField()));

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(Byte.valueOf((byte) 0), Byte.valueOf(targetObject.getByteField()));
        assertTrue(Character.valueOf((char) 0) == targetObject.getCharField());

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

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion6(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(30000.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Float.valueOf(50000000.0f), Float.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(97), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(1L), Long.valueOf(targetObject.getLongField()));
        assertEquals(Short.valueOf((short) 2), Short.valueOf(targetObject.getShortField()));
        assertTrue(Character.valueOf((char) 0) == targetObject.getCharField());

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(Byte.valueOf((byte) 0), Byte.valueOf(targetObject.getByteField()));

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
        assertEquals(Double.valueOf(70000000f), Double.valueOf(targetObject.getBoxedFloatField()));
        assertEquals(Integer.valueOf(5), Integer.valueOf(targetObject.getBoxedIntField()));
        assertEquals(Long.valueOf(20000L), Long.valueOf(targetObject.getBoxedLongField().longValue()));
        assertEquals(Short.valueOf((short) 5), Short.valueOf(targetObject.getBoxedShortField().shortValue()));
        assertEquals(Boolean.valueOf(Boolean.TRUE), targetObject.getBoxedBooleanField());
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
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
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
        assertNotNull(addressObject);
        assertEquals("123 Main St", addressObject.getAddressLine1());
        assertEquals("Suite 42b", addressObject.getAddressLine2());
        assertEquals("Anytown", addressObject.getCity());
        assertEquals("NY", addressObject.getState());
        assertEquals("90210", addressObject.getZipCode());
    }

    public static void validateJsonContact(io.atlasmap.json.test.BaseContact contactObject) {
        assertNotNull(contactObject);
        assertEquals("Ozzie", contactObject.getFirstName());
        assertEquals("Smith", contactObject.getLastName());
        assertEquals("5551212", contactObject.getPhoneNumber());
        assertEquals("81111", contactObject.getZipCode());
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion1(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(40000000d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(1), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(50000000L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Short.valueOf((short) 30000), Short.valueOf(targetObject.getShortField().shortValue()));
        assertEquals(Character.valueOf('2'), Character.valueOf(targetObject.getCharField().charAt(0)));

        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        // assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
        // assertNull(targetObject.getBoxedByteArrayField());
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

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion2(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(0.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(97d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(30000), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(40000000L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Character.valueOf('1'),  Character.valueOf(targetObject.getCharField().charAt(0)));

        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        // assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertNull(targetObject.getShortField());

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
        // assertNull(targetObject.getBoxedByteArrayField());
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

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion3(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(Double.valueOf(97d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(2.0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(50000000), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(0L), Long.valueOf(targetObject.getLongField().longValue()));
        // TODO: fix char validation
        // assertTrue(Character.valueOf((char)30000) == targetObject.getCharField());

        // Primitive auto-initialized values
        // assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertNull(targetObject.getShortField());

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
        // assertNull(targetObject.getBoxedByteArrayField());
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

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion4(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(2.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(1.0f), Double.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(40000000), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(97L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Short.valueOf((short) 0), Short.valueOf(targetObject.getShortField().shortValue()));

        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        // assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertNull(targetObject.getCharField());

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
        // assertNull(targetObject.getBoxedByteArrayField());
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

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion5(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(1.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(30000.0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(0), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(2L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Short.valueOf((short) 97), Short.valueOf(targetObject.getShortField().shortValue()));

        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        // assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertNull(targetObject.getCharField());

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
        // assertNull(targetObject.getBoxedByteArrayField());
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

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion6(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(Double.valueOf(30000.0d), Double.valueOf(targetObject.getDoubleField()));
        assertEquals(Double.valueOf(50000000.0d), Double.valueOf(targetObject.getFloatField()));
        assertEquals(Integer.valueOf(97), Integer.valueOf(targetObject.getIntField()));
        assertEquals(Long.valueOf(1L), Long.valueOf(targetObject.getLongField().longValue()));
        assertEquals(Short.valueOf((short) 2), Short.valueOf(targetObject.getShortField().shortValue()));
        assertTrue(Character.valueOf((char) 0) == targetObject.getCharField().charAt(0));

        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        // assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
        // assertNull(targetObject.getBoxedByteArrayField());
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
}
