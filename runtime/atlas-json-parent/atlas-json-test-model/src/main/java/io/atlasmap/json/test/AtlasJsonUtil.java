package io.atlasmap.json.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

public class AtlasJsonUtil {

    public static BaseOrderList generateOrderListClass(Class<? extends BaseOrderList> orderListClazz,
            Class<? extends BaseOrder> orderClazz, Class<? extends BaseAddress> addressClazz,
            Class<? extends BaseContact> contactClazz) throws Exception {
        Class<?> targetClazz = AtlasJsonUtil.class.getClassLoader().loadClass(orderListClazz.getName());
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
        Class<?> targetClazz = AtlasJsonUtil.class.getClassLoader().loadClass(orderClazz.getName());
        BaseOrder newObject = (BaseOrder) targetClazz.newInstance();

        newObject.setOrderId(8765309);
        newObject.setAddress(generateAddress(addressClazz));
        newObject.setContact(generateContact(contactClazz));
        return newObject;
    }

    public static BaseAddress generateAddress(Class<? extends BaseAddress> addressClass) throws Exception {
        Class<?> targetClazz = AtlasJsonUtil.class.getClassLoader().loadClass(addressClass.getName());
        BaseAddress newObject = (BaseAddress) targetClazz.newInstance();

        newObject.setAddressLine1("123 Main St");
        newObject.setAddressLine2("Suite 42b");
        newObject.setCity("Anytown");
        newObject.setState("NY");
        newObject.setZipCode("90210");
        return newObject;
    }

    public static BaseContact generateContact(Class<? extends BaseContact> contactClass) throws Exception {
        Class<?> targetClazz = AtlasJsonUtil.class.getClassLoader().loadClass(contactClass.getName());
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

}
