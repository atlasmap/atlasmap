package io.atlasmap.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import io.atlasmap.java.test.BaseAddress;
import io.atlasmap.java.test.BaseContact;
import io.atlasmap.java.test.BaseFlatPrimitiveClass;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.BaseOrderList;
import io.atlasmap.xml.test.v2.XmlAddressAttribute;
import io.atlasmap.xml.test.v2.XmlAddressElement;
import io.atlasmap.xml.test.v2.XmlContactAttribute;
import io.atlasmap.xml.test.v2.XmlContactElement;
import io.atlasmap.xml.test.v2.XmlFlatBoxedPrimitiveAttribute;
import io.atlasmap.xml.test.v2.XmlFlatBoxedPrimitiveElement;
import io.atlasmap.xml.test.v2.XmlFlatPrimitiveAttribute;
import io.atlasmap.xml.test.v2.XmlFlatPrimitiveElement;
import io.atlasmap.xml.test.v2.XmlOrderAttribute;
import io.atlasmap.xml.test.v2.XmlOrderElement;

public class AtlasTestUtil {

    public static BaseFlatPrimitiveClass generateFlatPrimitiveClass(Class<? extends BaseFlatPrimitiveClass> clazz)
            throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(clazz.getName());
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass) targetClazz.newInstance();

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
        BaseOrderList orderList = (BaseOrderList) targetClazz.newInstance();
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
        BaseOrder newObject = (BaseOrder) targetClazz.newInstance();

        newObject.setOrderId(8765309);
        newObject.setAddress(generateAddress(addressClazz));
        newObject.setContact(generateContact(contactClazz));
        return newObject;
    }

    public static BaseAddress generateAddress(Class<? extends BaseAddress> addressClass) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(addressClass.getName());
        BaseAddress newObject = (BaseAddress) targetClazz.newInstance();

        newObject.setAddressLine1("123 Main St");
        newObject.setAddressLine2("Suite 42b");
        newObject.setCity("Anytown");
        newObject.setState("NY");
        newObject.setZipCode("90210");
        return newObject;
    }

    public static BaseContact generateContact(Class<? extends BaseContact> contactClass) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(contactClass.getName());
        BaseContact newObject = (BaseContact) targetClazz.newInstance();

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

        assertEquals(new Integer(5), orderListObject.getNumberOrders());
        assertEquals(new Integer(4123562), orderListObject.getOrderBatchNumber());

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

        assertEquals(new Integer(5), orderListObject.getNumberOrders());
        assertEquals(new Integer(4123562), orderListObject.getOrderBatchNumber());

        if (orderListObject.getOrders() != null) {
            for (int i = 0; i < orderListObject.getOrders().size(); i++) {
                validateOrderNoAbstract(orderListObject.getOrders().get(i), i);
            }
        }
    }

    public static void validateOrderNoAbstract(BaseOrder orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(new Integer(expectedOrderId), orderObject.getOrderId());
        // https://github.com/atlasmap/atlasmap-runtime/issues/229 - Allow default
        // implementation for abstract target field
        assertNull(orderObject.getAddress());
        assertNull(orderObject.getContact());
    }

    public static void validateOrder(BaseOrder orderObject) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(new Integer(8765309), orderObject.getOrderId());
        validateAddress(orderObject.getAddress());
        validateContact(orderObject.getContact());
    }

    public static void validateOrder(BaseOrder orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(new Integer(expectedOrderId), orderObject.getOrderId());
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
        assertEquals(new Integer(3), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 4), new Short(targetObject.getShortField()));
        assertEquals(new Long(5L), new Long(targetObject.getLongField()));
        assertEquals(new Double(6d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(1f), new Float(targetObject.getFloatField()));
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Character('9'), new Character(targetObject.getCharField()));
        assertEquals(new Byte("2"), new Byte(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion2(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(4), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 5), new Short(targetObject.getShortField()));
        assertEquals(new Long(6L), new Long(targetObject.getLongField()));
        assertEquals(new Double(1.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(56f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('\u0002'), new Character(targetObject.getCharField()));
        assertEquals(new Byte((byte) 3), new Byte(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion3(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(5), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 6), new Short(targetObject.getShortField()));
        assertEquals(new Long(1L), new Long(targetObject.getLongField()));
        assertEquals(new Double(56d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(57.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('\u0003'), new Character(targetObject.getCharField()));
        assertEquals(new Byte((byte) 4), new Byte(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion4(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(6), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 1), new Short(targetObject.getShortField()));
        assertEquals(new Long(56L), new Long(targetObject.getLongField()));
        assertEquals(new Double(57.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(2.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('\u0004'), new Character(targetObject.getCharField()));
        assertEquals(new Byte((byte) 5), new Byte(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion5(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(1), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 56), new Short(targetObject.getShortField()));
        assertEquals(new Long(57L), new Long(targetObject.getLongField()));
        assertEquals(new Double(2.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(3.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('\u0005'), new Character(targetObject.getCharField()));
        assertEquals(new Byte((byte) 6), new Byte(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion6(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(56), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 57), new Short(targetObject.getShortField()));
        assertEquals(new Long(2L), new Long(targetObject.getLongField()));
        assertEquals(new Double(3.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(4.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('\u0006'), new Character(targetObject.getCharField()));
        assertEquals(new Byte((byte) 1), new Byte(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion7(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(57), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 2), new Short(targetObject.getShortField()));
        assertEquals(new Long(3L), new Long(targetObject.getLongField()));
        assertEquals(new Double(4.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(5.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('\u0001'), new Character(targetObject.getCharField()));
        assertEquals(new Byte((byte) 56), new Byte(targetObject.getByteField()));

        unusedPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFields(io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(50000000d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(40000000f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(2), new Integer(targetObject.getIntField()));
        assertEquals(new Long(30000L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Short((short) 1), new Short(targetObject.getShortField().shortValue()));
        assertEquals(Boolean.FALSE, targetObject.getBooleanField());
        // assertEquals(new Byte((byte) 99), new Byte(targetObject.getByteField()));
        assertEquals(new Character('a'), new Character(targetObject.getCharField().charAt(0)));
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
        assertEquals(new Double(90000000d), new Double(targetObject.getBoxedDoubleField()));
        assertEquals(new Float(70000000f), new Float(targetObject.getBoxedFloatField()));
        assertEquals(new Integer(5), new Integer(targetObject.getBoxedIntField()));
        assertEquals(new Long(20000L), new Long(targetObject.getBoxedLongField().longValue()));
        assertEquals(new Short((short) 5), new Short(targetObject.getBoxedShortField().shortValue()));
        assertEquals(Boolean.TRUE, targetObject.getBoxedBooleanField());
        // assertEquals(new Byte((byte) 87), new
        // Byte(targetObject.getBoxedByteField()));
        assertEquals(new Character('z'), new Character(targetObject.getBoxedCharField().charAt(0)));
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
        assertNotNull("Order object is null", orderObject);
        assertNotNull("orderId is null: " + orderObject.toString(), orderObject.getOrderId());
        assertEquals(new Integer(8765309), orderObject.getOrderId());
        validateJsonAddress(orderObject.getAddress());
        validateJsonContact(orderObject.getContact());
    }

    public static void validateJsonOrder(io.atlasmap.json.test.BaseOrder orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(new Integer(expectedOrderId), orderObject.getOrderId());
        validateJsonAddress(orderObject.getAddress());
        validateJsonContact(orderObject.getContact());
    }

    public static void validateJsonAddress(io.atlasmap.json.test.BaseAddress addressObject) {
        assertNotNull("Address object is null", addressObject);
        assertEquals("123 Main St", addressObject.getAddressLine1());
        assertEquals("Suite 42b", addressObject.getAddressLine2());
        assertEquals("Anytown", addressObject.getCity());
        assertEquals("NY", addressObject.getState());
        assertEquals("90210", addressObject.getZipCode());
    }

    public static void validateJsonContact(io.atlasmap.json.test.BaseContact contactObject) {
        assertNotNull("Contact object is null", contactObject);
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
        assertEquals(new Integer(3), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 4), new Short(targetObject.getShortField().shortValue()));
        assertEquals(new Long(5L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Double(6d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(1.0f), new Float(targetObject.getFloatField()));
        assertEquals(false, targetObject.getBooleanField());
        assertEquals(new Character('9'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 2), new Byte(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion2(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(4), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 5), new Short(targetObject.getShortField().shortValue()));
        assertEquals(new Long(6L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Double(1.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(56f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion3(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(5), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 6), new Short(targetObject.getShortField().shortValue()));
        assertEquals(new Long(1L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Double(56.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(57.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(new Character('\u0003'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 4), new Byte(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);

    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion4(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(6), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 1), new Short(targetObject.getShortField().shortValue()));
        assertEquals(new Long(56L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Double(57.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(2.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(new Character('\u0004'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 5), new Byte(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion5(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(1), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 56), new Short(targetObject.getShortField().shortValue()));
        assertEquals(new Long(57L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Double(2.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(3.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(new Character('\u0005'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 6), new Byte(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion6(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(56), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 57), new Short(targetObject.getShortField().shortValue()));
        assertEquals(new Long(2L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Double(3.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(4.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(new Character('\u0006'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 1), new Byte(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion7(
            io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(57), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 2), new Short(targetObject.getShortField().shortValue()));
        assertEquals(new Long(3L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Double(4.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(5.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(new Character('\u0001'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 56), new Byte(targetObject.getByteField().toString()));

        unusedJsonPrimativeMappingAsserts(targetObject);
    }

    public static void validateXmlOrderElement(XmlOrderElement orderObject) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals("8765309", orderObject.getOrderId());
        validateXmlAddressElement(orderObject.getAddress());
        validateXmlContactElement(orderObject.getContact());
    }

    public static void validateXmlOrderElement(XmlOrderElement orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(new Integer(expectedOrderId), orderObject.getOrderId());
        validateXmlAddressElement(orderObject.getAddress());
        validateXmlContactElement(orderObject.getContact());
    }

    public static void validateXmlAddressElement(XmlAddressElement addressObject) {
        assertNotNull(addressObject);
        assertEquals("123 Main St", addressObject.getAddressLine1());
        assertEquals("Suite 42b", addressObject.getAddressLine2());
        assertEquals("Anytown", addressObject.getCity());
        assertEquals("NY", addressObject.getState());
        assertEquals("90210", addressObject.getZipCode());
    }

    public static void validateXmlContactElement(XmlContactElement contactObject) {
        assertNotNull(contactObject);
        assertEquals("Ozzie", contactObject.getFirstName());
        assertEquals("Smith", contactObject.getLastName());
        assertEquals("5551212", contactObject.getPhoneNumber());
        assertEquals("81111", contactObject.getZipCode());
    }

    public static void validateXmlOrderAttribute(XmlOrderAttribute orderObject) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(new Integer(8765309), orderObject.getOrderId());
        validateXmlAddressAttribute(orderObject.getAddress());
        validateXmlContactAttribute(orderObject.getContact());
    }

    public static void validateXmlOrderAttribute(XmlOrderAttribute orderObject, int expectedOrderId) {
        assertNotNull(orderObject);
        assertNotNull(orderObject.getOrderId());
        assertEquals(new Integer(expectedOrderId), orderObject.getOrderId());
        validateXmlAddressAttribute(orderObject.getAddress());
        validateXmlContactAttribute(orderObject.getContact());
    }

    public static void validateXmlAddressAttribute(XmlAddressAttribute addressObject) {
        assertNotNull(addressObject);
        assertEquals("123 Main St", addressObject.getAddressLine1());
        assertEquals("Suite 42b", addressObject.getAddressLine2());
        assertEquals("Anytown", addressObject.getCity());
        assertEquals("NY", addressObject.getState());
        assertEquals("90210", addressObject.getZipCode());
    }

    public static void validateXmlContactAttribute(XmlContactAttribute contactObject) {
        assertNotNull(contactObject);
        assertEquals("Ozzie", contactObject.getFirstName());
        assertEquals("Smith", contactObject.getLastName());
        assertEquals("5551212", contactObject.getPhoneNumber());
        assertEquals("81111", contactObject.getZipCode());
    }

    public static void validateXmlFlatPrimitiveElement(XmlFlatPrimitiveElement targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(50000000d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(40000000f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(2), new Integer(targetObject.getIntField()));
        assertEquals(new Long(30000L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 1), new Short(targetObject.getShortField()));
        assertEquals(Boolean.FALSE, targetObject.isBooleanField());
        // assertEquals(new Byte((byte) 99), new Byte(targetObject.getByteField()));
        assertEquals(new Character('a'), new Character(targetObject.getCharField().charAt(0)));
        // assertNull(targetObject.getBooleanArrayField());
        // assertNull(targetObject.getBoxedBooleanArrayField());
        // assertNull(targetObject.getBoxedBooleanField());
        // assertNull(targetObject.getBoxedByteArrayField());
        // assertNull(targetObject.getBoxedByteField());
        // assertNull(targetObject.getBoxedCharArrayField());
        // assertNull(targetObject.getBoxedCharField());
        // assertNull(targetObject.getBoxedDoubleArrayField());
        // assertNull(targetObject.getBoxedDoubleField());
        // assertNull(targetObject.getBoxedFloatArrayField());
        // assertNull(targetObject.getBoxedFloatField());
        // assertNull(targetObject.getBoxedIntArrayField());
        // assertNull(targetObject.getBoxedIntField());
        // assertNull(targetObject.getBoxedLongArrayField());
        // assertNull(targetObject.getBoxedLongField());
        // assertNull(targetObject.getBoxedShortArrayField());
        // assertNull(targetObject.getBoxedShortField());
        // assertNull(targetObject.getBoxedStringArrayField());
        // assertNull(targetObject.getBoxedStringField());
    }

    public static void validateXmlFlatPrimitiveAttribute(XmlFlatPrimitiveAttribute targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(50000000d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(40000000f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(2), new Integer(targetObject.getIntField()));
        assertEquals(new Long(30000L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 1), new Short(targetObject.getShortField()));
        assertEquals(Boolean.FALSE, targetObject.isBooleanField());
        // assertEquals(new Byte((byte) 99), new Byte(targetObject.getByteField()));
        assertEquals(new Character('a'), new Character(targetObject.getCharField().charAt(0)));
        // assertNull(targetObject.getBooleanArrayField());
        // assertNull(targetObject.getBoxedBooleanArrayField());
        // assertNull(targetObject.getBoxedBooleanField());
        // assertNull(targetObject.getBoxedByteArrayField());
        // assertNull(targetObject.getBoxedByteField());
        // assertNull(targetObject.getBoxedCharArrayField());
        // assertNull(targetObject.getBoxedCharField());
        // assertNull(targetObject.getBoxedDoubleArrayField());
        // assertNull(targetObject.getBoxedDoubleField());
        // assertNull(targetObject.getBoxedFloatArrayField());
        // assertNull(targetObject.getBoxedFloatField());
        // assertNull(targetObject.getBoxedIntArrayField());
        // assertNull(targetObject.getBoxedIntField());
        // assertNull(targetObject.getBoxedLongArrayField());
        // assertNull(targetObject.getBoxedLongField());
        // assertNull(targetObject.getBoxedShortArrayField());
        // assertNull(targetObject.getBoxedShortField());
        // assertNull(targetObject.getBoxedStringArrayField());
        // assertNull(targetObject.getBoxedStringField());
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion1(XmlFlatPrimitiveElement targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(3), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 4), new Short(targetObject.getShortField()));
        assertEquals(new Long(5L), new Long(targetObject.getLongField()));
        assertEquals(new Double(6d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(0f), new Float(targetObject.getFloatField()));
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Character('9'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 2), new Byte(targetObject.getByteField()));
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion2(XmlFlatPrimitiveElement targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(4), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 5), new Short(targetObject.getShortField()));
        assertEquals(new Long(6L), new Long(targetObject.getLongField()));
        assertEquals(new Double(1.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(56f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('2'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 3), new Byte(targetObject.getByteField()));
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion3(XmlFlatPrimitiveElement targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(5), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 6), new Short(targetObject.getShortField()));
        assertEquals(new Long(0L), new Long(targetObject.getLongField()));
        assertEquals(new Double(56d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(57f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('3'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 4), new Byte(targetObject.getByteField()));
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion4(XmlFlatPrimitiveElement targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(6), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 1), new Short(targetObject.getShortField()));
        assertEquals(new Long(56L), new Long(targetObject.getLongField()));
        assertEquals(new Double(57.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(2.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('4'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 5), new Byte(targetObject.getByteField()));
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion5(XmlFlatPrimitiveElement targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(1), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 56), new Short(targetObject.getShortField()));
        assertEquals(new Long(57L), new Long(targetObject.getLongField()));
        assertEquals(new Double(2.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(3.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('5'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 6), new Byte(targetObject.getByteField()));
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion6(XmlFlatPrimitiveElement targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(56), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 57), new Short(targetObject.getShortField()));
        assertEquals(new Long(2L), new Long(targetObject.getLongField()));
        assertEquals(new Double(3.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(4.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('6'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 1), new Byte(targetObject.getByteField()));
    }

    public static void validateXmlFlatPrimitivePrimitiveElementAutoConversion7(XmlFlatPrimitiveElement targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Integer(57), new Integer(targetObject.getIntField()));
        assertEquals(new Short((short) 2), new Short(targetObject.getShortField()));
        assertEquals(new Long(3L), new Long(targetObject.getLongField()));
        assertEquals(new Double(4.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(5.0f), new Float(targetObject.getFloatField()));
        assertEquals(true, targetObject.isBooleanField());
        assertEquals(new Character('t'), new Character(targetObject.getCharField().charAt(0)));
        assertEquals(new Byte((byte) 56), new Byte(targetObject.getByteField()));
    }

    public static void validateXmlFlatPrimitiveBoxedPrimitiveElementFields(XmlFlatBoxedPrimitiveElement targetObject) {
        assertEquals(new Double(90000000d), new Double(targetObject.getBoxedDoubleField()));
        assertEquals(new Float(70000000f), new Float(targetObject.getBoxedFloatField()));
        assertEquals(new Integer(5), new Integer(targetObject.getBoxedIntField()));
        assertEquals(new Long(20000L), new Long(targetObject.getBoxedLongField()));
        assertEquals(new Short((short) 5), new Short(targetObject.getBoxedShortField()));
        assertEquals(Boolean.TRUE, targetObject.isBoxedBooleanField());
        assertEquals(new Byte((byte) 87), new Byte(targetObject.getBoxedByteField()));
        assertEquals(new Character('z'), new Character(targetObject.getBoxedCharField().charAt(0)));
        assertNull(targetObject.getBoxedStringField());
    }

    public static void validateXmlFlatPrimitiveBoxedPrimitiveAttributeFields(
            XmlFlatBoxedPrimitiveAttribute targetObject) {
        assertEquals(new Double(90000000d), new Double(targetObject.getBoxedDoubleField()));
        assertEquals(new Float(70000000f), new Float(targetObject.getBoxedFloatField()));
        assertEquals(new Integer(5), new Integer(targetObject.getBoxedIntField()));
        assertEquals(new Long(20000L), new Long(targetObject.getBoxedLongField()));
        assertEquals(new Short((short) 5), new Short(targetObject.getBoxedShortField()));
        assertEquals(Boolean.TRUE, targetObject.isBoxedBooleanField());
        assertEquals(new Byte((byte) 87), new Byte(targetObject.getBoxedByteField()));
        assertEquals(new Character('z'), new Character(targetObject.getBoxedCharField().charAt(0)));
        assertNull(targetObject.getBoxedStringField());
    }
}
