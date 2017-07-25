package io.atlasmap.validation;

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

public class AtlasTestUtil {

    
    public static BaseFlatPrimitiveClass generateFlatPrimitiveClass(Class<? extends BaseFlatPrimitiveClass> clazz) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(clazz.getName());
        BaseFlatPrimitiveClass newObject = (BaseFlatPrimitiveClass)targetClazz.newInstance();
        
        newObject.setBooleanField(false);
        newObject.setByteField((byte) 99);
        newObject.setCharField((char)'a');
        newObject.setDoubleField(50000000d);
        newObject.setFloatField(40000000f);
        newObject.setIntField(2);
        newObject.setLongField(30000L);        
        newObject.setShortField((short)1);
        return newObject;
    }
    
    public static BaseOrderList generateOrderListClass(Class<? extends BaseOrderList> orderListClazz, Class<? extends BaseOrder> orderClazz, Class<? extends BaseAddress> addressClazz, Class<? extends BaseContact> contactClazz) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(orderListClazz.getName());
        BaseOrderList orderList = (BaseOrderList)targetClazz.newInstance();
        orderList.setNumberOrders(5);
        orderList.setOrderBatchNumber(4123562);

        for(int i=0; i < 5; i++) {
            BaseOrder baseOrder = generateOrderClass(orderClazz, addressClazz, contactClazz);
            baseOrder.setOrderId(i);
            if(orderList.getOrders() == null) {
                orderList.setOrders(new ArrayList<BaseOrder>());
            }
            orderList.getOrders().add(baseOrder);
        }
        return orderList;
    }
    
    public static BaseOrder generateOrderClass(Class<? extends BaseOrder> orderClazz, Class<? extends BaseAddress> addressClazz, Class<? extends BaseContact> contactClazz) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(orderClazz.getName());
        BaseOrder newObject = (BaseOrder)targetClazz.newInstance();
        
        newObject.setOrderId(8765309);
        newObject.setAddress(generateAddress(addressClazz));
        newObject.setContact(generateContact(contactClazz));
        return newObject;
    }
     
    public static BaseAddress generateAddress(Class<? extends BaseAddress> addressClass) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(addressClass.getName());
        BaseAddress newObject = (BaseAddress)targetClazz.newInstance();
        
        newObject.setAddressLine1("123 Main St");
        newObject.setAddressLine2("Suite 42b");
        newObject.setCity("Anytown");
        newObject.setState("NY");
        newObject.setZipCode("90210");
        return newObject;
    }
    
    public static BaseContact generateContact(Class<? extends BaseContact> contactClass) throws Exception {
        Class<?> targetClazz = AtlasTestUtil.class.getClassLoader().loadClass(contactClass.getName());
        BaseContact newObject = (BaseContact)targetClazz.newInstance();
        
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
        
        if(orderListObject.getOrders() != null) {
            for(int i=0; i < orderListObject.getOrders().size(); i++) {
                validateOrder(orderListObject.getOrders().get(i), i);
            }
        }
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
    
    public static void validateFlatPrimitiveClassPrimitiveFieldAutoConversion1(BaseFlatPrimitiveClass targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(40000000d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(1), new Integer(targetObject.getIntField()));
        assertEquals(new Long(50000000L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 30000), new Short(targetObject.getShortField()));
        assertEquals(new Character('2'), new Character(targetObject.getCharField()));
        
        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        
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
        assertEquals(new Double(0.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(97f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(30000), new Integer(targetObject.getIntField()));
        assertEquals(new Long(40000000L), new Long(targetObject.getLongField()));
        // TODO: Fix char validateion
        //assertTrue(Character.valueOf((char)1) == targetObject.getCharField());
        
        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertEquals(new Short((short) 0), new Short(targetObject.getShortField()));

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
        assertEquals(new Double(97d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(2.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(50000000), new Integer(targetObject.getIntField()));
        assertEquals(new Long(0L), new Long(targetObject.getLongField()));
        // TODO: fix char validation
        //assertTrue(Character.valueOf((char)30000) == targetObject.getCharField());
        
        // Primitive auto-initialized values
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertEquals(new Short((short) 0), new Short(targetObject.getShortField()));

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
        assertEquals(new Double(2.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(1.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(40000000), new Integer(targetObject.getIntField()));
        assertEquals(new Long(97L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 0), new Short(targetObject.getShortField()));

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertTrue(Character.valueOf((char)0) == targetObject.getCharField());

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
        assertEquals(new Double(1.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(30000.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(0), new Integer(targetObject.getIntField()));
        assertEquals(new Long(2L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 97), new Short(targetObject.getShortField()));

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertTrue(Character.valueOf((char)0) == targetObject.getCharField());

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
        assertEquals(new Double(30000.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(50000000.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(97), new Integer(targetObject.getIntField()));
        assertEquals(new Long(1L), new Long(targetObject.getLongField()));
        assertEquals(new Short((short) 2), new Short(targetObject.getShortField()));
        assertTrue(Character.valueOf((char)0) == targetObject.getCharField());

        // Primitive auto-initialized values
        assertEquals(false, targetObject.isBooleanField());
        assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));

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
        assertEquals(new Double(50000000d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(40000000f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(2), new Integer(targetObject.getIntField()));
        assertEquals(new Long(30000L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Short((short) 1), new Short(targetObject.getShortField().shortValue()));
        assertEquals(Boolean.FALSE, targetObject.getBooleanField());
        //assertEquals(new Byte((byte) 99), new Byte(targetObject.getByteField()));
        assertEquals(new Character('a'), new Character(targetObject.getCharField().charAt(0)));
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
        //assertNull(targetObject.getBoxedgetBoxedByteArrayField());
        //assertNull(targetObject.getBoxedByteField());
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
    
    public static void validateJsonFlatPrimitiveBoxedPrimitiveFields(io.atlasmap.json.test.BaseFlatPrimitive  targetObject) {        
        assertEquals(new Double(90000000d), new Double(targetObject.getBoxedDoubleField()));
        assertEquals(new Float(70000000f), new Float(targetObject.getBoxedFloatField()));
        assertEquals(new Integer(5), new Integer(targetObject.getBoxedIntField()));
        assertEquals(new Long(20000L), new Long(targetObject.getBoxedLongField().longValue()));
        assertEquals(new Short((short) 5), new Short(targetObject.getBoxedShortField().shortValue()));
        assertEquals(new Boolean(Boolean.TRUE), targetObject.getBoxedBooleanField());
        //assertEquals(new Byte((byte) 87), new Byte(targetObject.getBoxedByteField()));
        assertEquals(new Character('z'), new Character(targetObject.getBoxedCharField().charAt(0)));
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBooleanField());
//        assertNull(targetObject.getBoxedByteArrayField());
//        assertTrue((byte)0 == targetObject.getByteField());
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
    
    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion1(io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(40000000d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(1), new Integer(targetObject.getIntField()));
        assertEquals(new Long(50000000L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Short((short) 30000), new Short(targetObject.getShortField().shortValue()));
        assertEquals(new Character('2'), new Character(targetObject.getCharField().charAt(0)));
        
        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        //assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        
        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
//        assertNull(targetObject.getBoxedByteArrayField());
//        assertNull(targetObject.getBoxedByteField());
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
    
    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion2(io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(0.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(97f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(30000), new Integer(targetObject.getIntField()));
        assertEquals(new Long(40000000L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Character('1'), new Character(targetObject.getCharField().charAt(0)));
        
        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        //assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertNull(targetObject.getShortField());

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
//        assertNull(targetObject.getBoxedByteArrayField());
//        assertNull(targetObject.getBoxedByteField());
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
    
    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion3(io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(true, targetObject.getBooleanField());
        assertEquals(new Double(97d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(2.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(50000000), new Integer(targetObject.getIntField()));
        assertEquals(new Long(0L), new Long(targetObject.getLongField().longValue()));
        // TODO: fix char validation
        //assertTrue(Character.valueOf((char)30000) == targetObject.getCharField());
        
        // Primitive auto-initialized values
        //assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertNull(targetObject.getShortField());

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
//        assertNull(targetObject.getBoxedByteArrayField());
//        assertNull(targetObject.getBoxedByteField());
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
    
    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion4(io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(2.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(1.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(40000000), new Integer(targetObject.getIntField()));
        assertEquals(new Long(97L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Short((short) 0), new Short(targetObject.getShortField().shortValue()));

        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        //assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertNull(targetObject.getCharField());

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
//        assertNull(targetObject.getBoxedByteArrayField());
//        assertNull(targetObject.getBoxedByteField());
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
    
    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion5(io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(1.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(30000.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(0), new Integer(targetObject.getIntField()));
        assertEquals(new Long(2L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Short((short) 97), new Short(targetObject.getShortField().shortValue()));

        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        //assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));
        assertNull(targetObject.getCharField());

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
//        assertNull(targetObject.getBoxedByteArrayField());
//        assertNull(targetObject.getBoxedByteField());
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
    
    public static void validateJsonFlatPrimitivePrimitiveFieldAutoConversion6(io.atlasmap.json.test.BaseFlatPrimitive targetObject) {
        assertNotNull(targetObject);
        assertEquals(new Double(30000.0d), new Double(targetObject.getDoubleField()));
        assertEquals(new Float(50000000.0f), new Float(targetObject.getFloatField()));
        assertEquals(new Integer(97), new Integer(targetObject.getIntField()));
        assertEquals(new Long(1L), new Long(targetObject.getLongField().longValue()));
        assertEquals(new Short((short) 2), new Short(targetObject.getShortField().shortValue()));
        assertTrue(Character.valueOf((char)0) == targetObject.getCharField().charAt(0));

        // Primitive auto-initialized values
        assertNull(targetObject.getBooleanField());
        //assertEquals(new Byte((byte) 0), new Byte(targetObject.getByteField()));

        // Unused by mapping
        assertNotNull(targetObject.getBooleanArrayField());
        assertTrue(targetObject.getBooleanArrayField().isEmpty());
        assertNotNull(targetObject.getBoxedBooleanArrayField());
        assertTrue(targetObject.getBoxedBooleanArrayField().isEmpty());
        assertNull(targetObject.getBoxedBooleanField());
//        assertNull(targetObject.getBoxedByteArrayField());
//        assertNull(targetObject.getBoxedByteField());
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
