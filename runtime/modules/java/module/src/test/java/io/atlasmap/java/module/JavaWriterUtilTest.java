package io.atlasmap.java.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;

public class JavaWriterUtilTest extends BaseDocumentWriterTest {
    @Test
    public void testGetObjectFromParent() throws Exception {
        JavaWriterUtil writerUtil = new JavaWriterUtil(DefaultAtlasConversionService.getInstance());

        reset();
        setupPath("/contact");
        assertTrue(writerUtil.getObjectFromParent(field, targetTestClassInstance,
                lastSegmentContext) == targetTestClassInstance.getContact());

        reset();
        setupPath("/address");
        assertTrue(writerUtil.getObjectFromParent(field, targetTestClassInstance,
                lastSegmentContext) == targetTestClassInstance.getAddress());

        reset();
        setupPath("/nothing");
        assertTrue(writerUtil.getObjectFromParent(field, targetTestClassInstance, lastSegmentContext) == null);

        // get child is supposed to just return the direct child to the parent class, in
        // this instance that's a collection, the List part of List<Contact>.

        reset();
        setupPath("/orders<1>");
        assertTrue(writerUtil.getObjectFromParent(field, targetOrderListInstance,
                lastSegmentContext) == targetOrderListInstance.getOrders());

        reset();
        setupPath("/orders[1]");
        assertTrue(writerUtil.getObjectFromParent(field, targetOrderArrayInstance,
                lastSegmentContext) == targetOrderArrayInstance.getOrders());
    }

    @Test
    public void testSetObjectOnParent() throws Exception {
        JavaWriterUtil writerUtil = new JavaWriterUtil(DefaultAtlasConversionService.getInstance());

        reset();
        setupPath("/contact");
        targetTestClassInstance.setContact(null);
        TargetContact testContact = new TargetContact();
        writerUtil.setObjectOnParent(field, lastSegmentContext, targetTestClassInstance, testContact);
        assertTrue(targetTestClassInstance.getContact() == testContact);

        reset();
        setupPath("/address");
        targetTestClassInstance.setAddress(null);
        TargetAddress testAddress = new TargetAddress();
        writerUtil.setObjectOnParent(field, lastSegmentContext, targetTestClassInstance, testAddress);
        assertTrue(targetTestClassInstance.getAddress() == testAddress);

        reset();
        setupPath("/address/addressLine1");
        targetTestClassInstance.getAddress().setAddressLine1(null);
        String addressLine1 = "123 any street";
        writerUtil.setObjectOnParent(field, lastSegmentContext, targetTestClassInstance.getAddress(), addressLine1);
        assertTrue(targetTestClassInstance.getAddress().getAddressLine1() == addressLine1);

        reset();
        setupPath("/listOrders/orders<5>");
        List<TargetOrder> testListOrders = new LinkedList<>();
        targetTestClassInstance.getListOrders().setOrders(null);
        writerUtil.setObjectOnParent(field, lastSegmentContext, targetTestClassInstance.getListOrders(),
                testListOrders);
        assertTrue(((Object) targetTestClassInstance.getListOrders().getOrders()) == testListOrders);

        reset();
        setupPath("/orderArray/orders[10]");
        targetTestClassInstance.getOrderArray().setOrders(null);
        Object[] testArrayOrders = new TargetOrder[12];
        writerUtil.setObjectOnParent(field, lastSegmentContext, targetTestClassInstance.getOrderArray(),
                testArrayOrders);
        assertTrue(targetTestClassInstance.getOrderArray().getOrders() == testArrayOrders);
    }

    @Test
    public void testInstantiateObject() throws Exception {
        runInstantiateObjectTest(String.class, false);
        runInstantiateObjectTest(String.class, true);
        runInstantiateObjectTest(LinkedList.class, false);
        runInstantiateObjectTest(HashMap.class, false);
        runInstantiateObjectTest(TargetAddress.class, false);
        runInstantiateObjectTest(TargetAddress.class, true);
    }

    @Test(expected = AtlasException.class)
    public void testInstantiateAbstractClassError() throws Exception {
        runInstantiateObjectTest(Object[].class, false);
    }

    public void runInstantiateObjectTest(Class<?> clz, boolean createWrapperArray) throws Exception {
        SegmentContext sc = new SegmentContext();
        sc.setSegment("blah[52]");
        sc.setSegmentPath("/blah[52]");
        JavaWriterUtil writerUtil = new JavaWriterUtil(DefaultAtlasConversionService.getInstance());
        Object o = writerUtil.instantiateObject(clz, sc, createWrapperArray);
        assertNotNull(o);
        if (createWrapperArray) {
            assertTrue(o.getClass().isArray());
            assertEquals(53, Array.getLength(o));
            assertEquals(clz, o.getClass().getComponentType());
        } else {
            assertEquals(clz, o.getClass());
        }
    }
}
