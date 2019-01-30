package io.atlasmap.java.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.v2.CollectionType;

public class JavaFieldWriterUtilTest extends BaseJavaFieldWriterTest {

    private TargetAddress[] targetAddressArray;
    private List<TargetContact> targetContactList;
    private Map<String, TargetOrder> targetOrderMap;

    @Before
    public void before() {
        reset();
        targetContactList = new ArrayList<>();
        targetAddressArray = new TargetAddress[0];
        targetOrderMap = new HashMap<>();
    }

    @Test
    public void testGetChildObjectNotExist() throws Exception {
        assertNull(writerUtil.getChildObject(targetTestClassInstance, new SegmentContext("nothing")));
    }

    @Test
    public void testChildObject() throws Exception {
        Object contact = writerUtil.getChildObject(targetTestClassInstance, new SegmentContext("contact"));
        assertEquals(targetTestClassInstance.getContact(), contact);
        assertNull(targetTestClassInstance.getContact().getFirstName());
        writerUtil.setChildObject(contact, "foo", new SegmentContext("firstName"));
        assertEquals("foo", targetTestClassInstance.getContact().getFirstName());
        TargetContact created = new TargetContact();
        created.setLastName("last");
        writerUtil.setChildObject(targetTestClassInstance, created, new SegmentContext("contact"));
        assertEquals(created, targetTestClassInstance.getContact());
        assertNull(targetTestClassInstance.getContact().getFirstName());
        assertEquals("last", targetTestClassInstance.getContact().getLastName());
        writerUtil.createComplexChildObject(targetTestClassInstance, new SegmentContext("contact"));
        assertNotEquals(contact, targetTestClassInstance.getContact());
        assertNotEquals(created, targetTestClassInstance.getContact());
        assertNull(targetTestClassInstance.getContact().getLastName());
    }

    @Test
    public void testChildArray() throws Exception {
        Object orders = writerUtil.getChildObject(targetOrderArrayInstance, new SegmentContext("orders[1]"));
        assertEquals(BaseOrder[].class, orders.getClass());
        Object order1 = writerUtil.getCollectionItem(orders, new SegmentContext("orders[1]"));
        assertEquals(order1, targetOrderArrayInstance.getOrders()[1]);
        assertEquals(2, targetOrderArrayInstance.getOrders().length);
        Object adjusted = writerUtil.adjustCollectionSize(targetOrderArrayInstance.getOrders(), new SegmentContext("orders[5]"));
        writerUtil.setChildObject(targetOrderArrayInstance, adjusted, new SegmentContext("orders[5]"));
        assertEquals(adjusted, targetOrderArrayInstance.getOrders());
        assertEquals(6, targetOrderArrayInstance.getOrders().length);
        assertNull(targetOrderArrayInstance.getOrders()[4]);
        Object created = writerUtil.createComplexCollectionItem(targetOrderArrayInstance.getOrders(), TargetOrder.class, new SegmentContext("orders[4]"));
        assertEquals(created, targetOrderArrayInstance.getOrders()[4]);

        assertEquals(0, targetAddressArray.length);
        created = writerUtil.adjustCollectionSize(targetAddressArray, new SegmentContext("targetAddressArray[2]"));
        writerUtil.setChildObject(this, created, new SegmentContext("targetAddressArray[2]"));
        assertEquals(3, targetAddressArray.length);
        assertNull(targetAddressArray[2]);
        created = writerUtil.createComplexCollectionItem(this, targetAddressArray, new SegmentContext("targetAddressArray[2]"));
        assertEquals(created, targetAddressArray[2]);
        assertNull(targetAddressArray[1]);
        created = writerUtil.createComplexCollectionItem(targetAddressArray, TargetAddress.class, new SegmentContext("targetAddressArray[1]"));
        assertEquals(created, targetAddressArray[1]);
        assertNull(targetAddressArray[0]);
        created = new TargetAddress();
        writerUtil.setCollectionItem(targetAddressArray, created, new SegmentContext("targetAddressArray[0]"));
        assertEquals(created, targetAddressArray[0]);
    }

    @Test
    public void testChildList() throws Exception {
        Object orders = writerUtil.getChildObject(targetOrderListInstance, new SegmentContext("orders<1>"));
        assertEquals(LinkedList.class, orders.getClass());
        Object order1 = writerUtil.getCollectionItem(orders, new SegmentContext("orders<1>"));
        assertEquals(order1, targetOrderListInstance.getOrders().get(1));
        assertEquals(2, targetOrderListInstance.getOrders().size());
        Object adjusted = writerUtil.adjustCollectionSize(targetOrderListInstance.getOrders(), new SegmentContext("orders<5>"));
        writerUtil.setChildObject(targetOrderListInstance, adjusted, new SegmentContext("orders<5>"));
        assertEquals(adjusted, targetOrderListInstance.getOrders());
        assertEquals(6, targetOrderListInstance.getOrders().size());
        assertNull(targetOrderListInstance.getOrders().get(4));
        Object created = writerUtil.createComplexCollectionItem(targetOrderListInstance.getOrders(), TargetOrder.class, new SegmentContext("orders<4>"));
        assertEquals(created, targetOrderListInstance.getOrders().get(4));

        assertEquals(0, targetContactList.size());
        adjusted = writerUtil.adjustCollectionSize(targetContactList, new SegmentContext("targetContactList<3>"));
        writerUtil.setChildObject(this, adjusted, new SegmentContext("targetContactList<3>"));
        assertEquals(4, targetContactList.size());
        assertNull(targetContactList.get(1));
        created = writerUtil.createComplexCollectionItem(this, targetContactList, new SegmentContext("targetContactList<2>"));
        assertEquals(created, targetContactList.get(2));
        assertNull(targetContactList.get(1));
        created = writerUtil.createComplexCollectionItem(targetContactList, TargetContact.class, new SegmentContext("targetContactList<1>"));
        assertEquals(created, targetContactList.get(1));
        assertNull(targetContactList.get(0));
        created = new TargetContact();
        writerUtil.setCollectionItem(targetContactList, created, new SegmentContext("targetContactList<0>"));
        assertEquals(created, targetContactList.get(0));
    }

    @Test
    public void testGetDefaultCollectionImplClass() throws Exception {
        assertNull(writerUtil.getDefaultCollectionImplClass(CollectionType.NONE));
        assertNull(writerUtil.getDefaultCollectionImplClass(CollectionType.ARRAY));
        assertEquals(LinkedList.class, writerUtil.getDefaultCollectionImplClass(CollectionType.LIST));
        assertEquals(HashMap.class, writerUtil.getDefaultCollectionImplClass(CollectionType.MAP));
    }

    @Test
    public void testChildMap() throws Exception {
        Object adjusted = writerUtil.adjustCollectionSize(targetOrderMap, new SegmentContext("targetOrderMap{foo}"));
        assertEquals(targetOrderMap, adjusted);

        try {
            writerUtil.setCollectionItem(targetOrderMap, new TargetOrder(), new SegmentContext("targetOrderMap{foo}"));
            fail("AtlasException is expected");
        } catch (AtlasException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("TODO"));
        }
        try {
            writerUtil.createComplexCollectionItem(targetOrderMap, TargetOrder.class, new SegmentContext("targetOrderMap{foo}"));
            fail("AtlasException is expected");
        } catch (AtlasException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("TODO"));
        }
        try {
            writerUtil.createComplexCollectionItem(this, targetOrderMap, new SegmentContext("targetOrderMap{foo}"));
            fail("AtlasException is expected");
        } catch (AtlasException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("TODO"));
        }
        try {
            writerUtil.setCollectionItem(targetOrderMap, new TargetOrder(), new SegmentContext("targetOrderMap{foo}"));
            fail("AtlasException is expected");
        } catch (AtlasException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("TODO"));
        }
    }
}
