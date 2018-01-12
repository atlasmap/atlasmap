package io.atlasmap.java.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.atlasmap.api.AtlasException;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.StateEnumClassLong;
import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetOrderArray;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.java.test.TestListOrders;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.FieldType;

@FixMethodOrder(MethodSorters.JVM)
public class DocumentJavaFieldWriterTest extends BaseDocumentWriterTest {

    @Test
    public void testSimpleClassLookup() throws Exception {
        addClassForFieldPath("/", TargetTestClass.class);
        addClassForFieldPath("/address", TargetAddress.class);
        write("/address/addressLine1", "123 any street");
        TargetTestClass o = (TargetTestClass) writer.getRootObject();
        ensureNotNullAndClass(o, TargetTestClass.class);
        ensureNotNullAndClass(o.getAddress(), TargetAddress.class);
        assertEquals("123 any street", o.getAddress().getAddressLine1());
    }

    @Test
    public void testClassLookupFromField() throws Exception {
        JavaField f = createField("/", null);
        f.setClassName(TestListOrders.class.getName());
        f.setFieldType(FieldType.COMPLEX);
        write(f);
        TestListOrders o = (TestListOrders) writer.getRootObject();
        ensureNotNullAndClass(o, TestListOrders.class);

        f = createField("/orders<4>", null);
        f.setClassName(TargetOrder.class.getName());
        f.setFieldType(FieldType.COMPLEX);
        write(f);
        ensureNotNullAndClass(o.getOrders(), LinkedList.class);
        assertEquals(5, o.getOrders().size());
        for (int i = 0; i < 5; i++) {
            System.out.println("Checking #" + i);
            TargetOrder order = (TargetOrder) o.getOrders().get(i);
            if (i == 4) {
                ensureNotNullAndClass(order, TargetOrder.class);
            } else {
                assertNull(order);
            }
        }

        f = createField("/orders<7>", null);
        f.setClassName(TargetOrder.class.getName());
        f.setFieldType(FieldType.COMPLEX);
        write(f);
        ensureNotNullAndClass(o.getOrders(), LinkedList.class);
        assertEquals(8, o.getOrders().size());
        for (int i = 0; i < 8; i++) {
            System.out.println("Checking #" + i);
            TargetOrder order = (TargetOrder) o.getOrders().get(i);
            if (i == 4 || i == 7) {
                ensureNotNullAndClass(order, TargetOrder.class);
            } else {
                assertNull(order);
            }
        }

        f = createField("/orders<7>/address", null);
        f.setClassName(TargetAddress.class.getName());
        f.setFieldType(FieldType.COMPLEX);
        write(f);
        ensureNotNullAndClass(o.getOrders().get(7).getAddress(), TargetAddress.class);

        write(createField("/orders<7>/address/addressLine1", "hello world1."));
        assertEquals("hello world1.", o.getOrders().get(7).getAddress().getAddressLine1());
    }

    @Test
    public void testPrimitiveArrayLookup() throws Exception {
        addClassForFieldPath("/", TargetFlatPrimitiveClass.class);
        write("/intArrayField[10]", 3425);
        TargetFlatPrimitiveClass o = (TargetFlatPrimitiveClass) writer.getRootObject();
        ensureNotNullAndClass(o, TargetFlatPrimitiveClass.class);
        ensureNotNullAndClass(o.getIntArrayField(), int[].class);
        assertEquals(11, o.getIntArrayField().length);
        assertEquals(3425, o.getIntArrayField()[10]);

        write("/boxedStringArrayField[10]", "boxedString");
        ensureNotNullAndClass(o.getBoxedStringArrayField(), String[].class);
        assertEquals(11, o.getBoxedStringArrayField().length);
        for (int i = 0; i < 10; i++) {
            System.out.println("Checking #" + i);
            assertNull(o.getBoxedStringArrayField()[i]);
        }
        ensureNotNullAndClass(o.getBoxedStringArrayField()[10], String.class);
        assertEquals("boxedString", o.getBoxedStringArrayField()[10]);
    }

    @Test(expected = AtlasException.class)
    public void testClassLookupAbstract() throws Exception {
        addClassForFieldPath("/", TargetTestClass.class);
        write(createField("/orders[4]/address/addressLine1", "hello world."));
    }

    @Test
    public void testClassLookupReflection() throws Exception {
        addClassForFieldPath("/", TargetTestClass.class);
        write("/address/addressLine1", "123 any street");
        TargetTestClass o = (TargetTestClass) writer.getRootObject();
        ensureNotNullAndClass(o, TargetTestClass.class);
        ensureNotNullAndClass(o.getAddress(), TargetAddress.class);
        assertEquals("123 any street", o.getAddress().getAddressLine1());
    }

    @Test
    public void testSimpleWrite() throws Exception {
        addClassForFieldPath("/", TargetAddress.class);
        write(createField("/addressLine1", "1234 some street."));
        write(createField("/addressLine2", "po box wherever"));
        write(createField("/city", "Round Rock"));
        write(createField("/state", "VA"));
        write(createField("/zipCode", "12345-6789"));
        TargetAddress o = (TargetAddress) writer.getRootObject();
        ensureNotNullAndClass(o, TargetAddress.class);
        assertEquals("1234 some street.", o.getAddressLine1());
        assertEquals("po box wherever", o.getAddressLine2());
        assertEquals("Round Rock", o.getCity());
        assertEquals("VA", o.getState());
        assertEquals("12345-6789", o.getZipCode());
    }

    @Test
    public void testSimpleWriteCollectionList() throws Exception {
        addClassForFieldPath("/", TestListOrders.class);
        addClassForFieldPath("/orders<5>", TargetOrder.class);
        addClassForFieldPath("/orders<5>/address", TargetAddress.class);
        write("/orders<4>/address/addressLine1", "hello world.");
        TestListOrders o = (TestListOrders) writer.getRootObject();
        ensureNotNullAndClass(o, TestListOrders.class);
        ensureNotNullAndClass(o.getOrders(), LinkedList.class);
        assertEquals(5, o.getOrders().size());
        for (int i = 0; i < 5; i++) {
            System.out.println("Checking #" + i);
            if (i == 4) {
                ensureNotNullAndClass(o.getOrders().get(i), TargetOrder.class);
                ensureNotNullAndClass(o.getOrders().get(i).getAddress(), TargetAddress.class);
            } else {
                assertNull(o.getOrders().get(i));
            }
        }
        assertEquals("hello world.", o.getOrders().get(4).getAddress().getAddressLine1());
    }

    @Test
    public void testSimpleWriteCollectionArray() throws Exception {
        addClassForFieldPath("/", TargetOrderArray.class);
        addClassForFieldPath("/orders[5]", TargetOrder.class);
        addClassForFieldPath("/orders[5]/address", TargetAddress.class);
        write("/orders[4]/address/addressLine1", "hello world.");
        TargetOrderArray o = (TargetOrderArray) writer.getRootObject();
        ensureNotNullAndClass(o, TargetOrderArray.class);
        ensureNotNullAndClass(o.getOrders(), TargetOrder[].class);
        assertEquals(5, o.getOrders().length);
        for (int i = 0; i < 5; i++) {
            System.out.println("Checking #" + i);
            if (i == 4) {
                ensureNotNullAndClass(o.getOrders()[i], TargetOrder.class);
                ensureNotNullAndClass(o.getOrders()[i].getAddress(), TargetAddress.class);
            } else {
                assertNull(o.getOrders()[i]);
            }
        }
        assertEquals("hello world.", o.getOrders()[4].getAddress().getAddressLine1());
    }

    @Test
    public void testExpandCollectionList() throws Exception {
        addClassForFieldPath("/", TestListOrders.class);
        addClassForFieldPath("/orders<5>", TargetOrder.class);
        addClassForFieldPath("/orders<5>/address", TargetAddress.class);
        write("/orders<4>/address/addressLine1", "hello world1.");
        write("/orders<14>/address/addressLine1", "hello world2.");
        write("/orders<2>/address/addressLine1", "hello world3.");
        TestListOrders o = (TestListOrders) writer.getRootObject();
        ensureNotNullAndClass(o, TestListOrders.class);
        ensureNotNullAndClass(o.getOrders(), LinkedList.class);
        assertEquals(15, o.getOrders().size());
        for (int i = 0; i < 15; i++) {
            System.out.println("Checking #" + i);
            if (i == 4 || i == 14 || i == 2) {
                ensureNotNullAndClass(o.getOrders().get(i), TargetOrder.class);
                ensureNotNullAndClass(o.getOrders().get(i).getAddress(), TargetAddress.class);
            } else {
                assertNull(o.getOrders().get(i));
            }
        }
        assertEquals("hello world1.", o.getOrders().get(4).getAddress().getAddressLine1());
        assertEquals("hello world2.", o.getOrders().get(14).getAddress().getAddressLine1());
        assertEquals("hello world3.", o.getOrders().get(2).getAddress().getAddressLine1());
    }

    @Test
    public void testExpandCollectionArray() throws Exception {
        addClassForFieldPath("/", TargetOrderArray.class);
        addClassForFieldPath("/orders[5]", TargetOrder.class);
        addClassForFieldPath("/orders[5]/address", TargetAddress.class);
        write("/orders[4]/address/addressLine1", "hello world1.");
        write("/orders[14]/address/addressLine1", "hello world2.");
        write("/orders[2]/address/addressLine1", "hello world3.");
        TargetOrderArray o = (TargetOrderArray) writer.getRootObject();
        ensureNotNullAndClass(o, TargetOrderArray.class);
        ensureNotNullAndClass(o.getOrders(), TargetOrder[].class);
        assertEquals(15, o.getOrders().length);
        for (int i = 0; i < 15; i++) {
            System.out.println("Checking #" + i);
            if (i == 4 || i == 14 || i == 2) {
                ensureNotNullAndClass(o.getOrders()[i], TargetOrder.class);
                ensureNotNullAndClass(o.getOrders()[i].getAddress(), TargetAddress.class);
            } else {
                assertNull(o.getOrders()[i]);
            }
        }
        assertEquals("hello world1.", o.getOrders()[4].getAddress().getAddressLine1());
        assertEquals("hello world2.", o.getOrders()[14].getAddress().getAddressLine1());
        assertEquals("hello world3.", o.getOrders()[2].getAddress().getAddressLine1());
    }

    @Test
    public void testWritingPrimitiveArrays() throws Exception {
        addClassForFieldPath("/", TargetFlatPrimitiveClass.class);
        addClassForFieldPath("/intArrayField[34]", int.class);
        addClassForFieldPath("/boxedStringArrayField[312]", String.class);

        write("/intArrayField[10]", 3425);

        TargetFlatPrimitiveClass o = (TargetFlatPrimitiveClass) writer.getRootObject();
        ensureNotNullAndClass(o, TargetFlatPrimitiveClass.class);
        ensureNotNullAndClass(o.getIntArrayField(), int[].class);
        assertEquals(11, o.getIntArrayField().length);
        assertEquals(3425, o.getIntArrayField()[10]);

        write("/boxedStringArrayField[10]", "boxedString");
        ensureNotNullAndClass(o.getBoxedStringArrayField(), String[].class);
        assertEquals(11, o.getBoxedStringArrayField().length);
        for (int i = 0; i < 10; i++) {
            System.out.println("Checking #" + i);
            assertNull(o.getBoxedStringArrayField()[i]);
        }
        ensureNotNullAndClass(o.getBoxedStringArrayField()[10], String.class);
        assertEquals("boxedString", o.getBoxedStringArrayField()[10]);
    }

    @Test
    public void testFullWrite() throws Exception {
        addClassForFieldPath("/", TargetTestClass.class);
        addClassForFieldPath("/address", TargetAddress.class);
        addClassForFieldPath("/listOrders", TestListOrders.class);
        addClassForFieldPath("/listOrders/orders<5>", TargetOrder.class);
        addClassForFieldPath("/listOrders/orders<5>/address", TargetAddress.class);
        addClassForFieldPath("/orderArray", TargetOrderArray.class);
        addClassForFieldPath("/orderArray/orders[5]", TargetOrder.class);
        addClassForFieldPath("/orderArray/orders[5]/contact", TargetContact.class);
        addClassForFieldPath("/primitives", TargetFlatPrimitiveClass.class);
        addClassForFieldPath("/primitives/intArrayField[]", int.class);
        addClassForFieldPath("/primitives/boxedStringArrayField[19]", String.class);
        addClassForFieldPath("/statesLong", StateEnumClassLong.class);

        write("/name", "someName");

        TargetTestClass o = (TargetTestClass) writer.getRootObject();
        ensureNotNullAndClass(o, TargetTestClass.class);
        assertEquals("someName", o.getName());

        write("/address/addressLine1", "123 any street");
        ensureNotNullAndClass(o.getAddress(), TargetAddress.class);
        assertEquals("123 any street", o.getAddress().getAddressLine1());

        write("/listOrders/orders<5>/orderId", 1234);
        ensureNotNullAndClass(o.getListOrders(), TestListOrders.class);
        ensureNotNullAndClass(o.getListOrders().getOrders(), LinkedList.class);
        assertEquals(6, o.getListOrders().getOrders().size());
        for (int i = 0; i < 6; i++) {
            System.out.println("Checking #" + i);
            BaseOrder order = o.getListOrders().getOrders().get(i);
            if (i == 5) {
                ensureNotNullAndClass(order, TargetOrder.class);
            } else {
                assertNull(order);
            }

        }
        BaseOrder order = o.getListOrders().getOrders().get(5);
        assertEquals((Integer) 1234, order.getOrderId());

        write("/listOrders/orders<2>/address/city", "Austin");
        assertEquals(6, o.getListOrders().getOrders().size());
        // ensure earlier fields are ok
        order = o.getListOrders().getOrders().get(5);
        assertEquals((Integer) 1234, order.getOrderId());
        // ensure our new field is ok
        order = o.getListOrders().getOrders().get(2);
        ensureNotNullAndClass(order.getAddress(), TargetAddress.class);
        assertEquals("Austin", order.getAddress().getCity());

        write("/orderArray/numberOrders", 56);
        ensureNotNullAndClass(o.getOrderArray(), TargetOrderArray.class);
        assertEquals((Integer) 56, o.getOrderArray().getNumberOrders());

        write("/orderArray/orders[2]/contact/firstName", "fName");
        ensureNotNullAndClass(o.getOrderArray().getOrders(), TargetOrder[].class);
        assertEquals(3, o.getOrderArray().getOrders().length);
        for (int i = 0; i < 3; i++) {
            System.out.println("Checking #" + i);
            BaseOrder order2 = o.getOrderArray().getOrders()[i];
            if (i == 2) {
                ensureNotNullAndClass(order2, TargetOrder.class);
            } else {
                assertNull(order2);
            }
        }
        ensureNotNullAndClass(o.getOrderArray().getOrders()[2].getContact(), TargetContact.class);
        // ensure previous fields are ok
        assertEquals((Integer) 56, o.getOrderArray().getNumberOrders());
        // ensure new field is ok
        assertEquals("fName", o.getOrderArray().getOrders()[2].getContact().getFirstName());

        write("/orderArray/orders[2]/contact/lastName", "lName");
        // ensure previous fields are ok
        assertEquals((Integer) 56, o.getOrderArray().getNumberOrders());
        assertEquals("fName", o.getOrderArray().getOrders()[2].getContact().getFirstName());
        // ensure new field is ok
        assertEquals("lName", o.getOrderArray().getOrders()[2].getContact().getLastName());

        // test writing primitive array values
        write("/primitives/intArrayField[10]", 3425);
        ensureNotNullAndClass(o.getPrimitives(), TargetFlatPrimitiveClass.class);
        ensureNotNullAndClass(o.getPrimitives().getIntArrayField(), int[].class);
        assertEquals(11, o.getPrimitives().getIntArrayField().length);
        assertEquals(3425, o.getPrimitives().getIntArrayField()[10]);

        write("/primitives/boxedStringArrayField[10]", "boxedString");
        ensureNotNullAndClass(o.getPrimitives().getBoxedStringArrayField(), String[].class);
        assertEquals(11, o.getPrimitives().getBoxedStringArrayField().length);
        for (int i = 0; i < 10; i++) {
            System.out.println("Checking #" + i);
            assertNull(o.getPrimitives().getBoxedStringArrayField()[i]);
        }
        ensureNotNullAndClass(o.getPrimitives().getBoxedStringArrayField()[10], String.class);
        assertEquals("boxedString", o.getPrimitives().getBoxedStringArrayField()[10]);

        // test writing enum values
        write("/statesLong", StateEnumClassLong.Massachusetts);
        assertNotNull(o.getStatesLong());
        assertEquals(StateEnumClassLong.Massachusetts, o.getStatesLong());

        // test overwriting values
        assertEquals("123 any street", o.getAddress().getAddressLine1());
        write("/address/addressLine1", "123 any street (2)");
        assertEquals("123 any street (2)", o.getAddress().getAddressLine1());

        order = o.getListOrders().getOrders().get(5);
        assertEquals((Integer) 1234, order.getOrderId());
        write("/listOrders/orders<5>/orderId", 2221234);
        assertEquals((Integer) 2221234, order.getOrderId());

        order = o.getListOrders().getOrders().get(2);
        assertEquals("Austin", order.getAddress().getCity());
        write("/listOrders/orders<2>/address/city", "Austin (2)");
        assertEquals("Austin (2)", order.getAddress().getCity());

        assertEquals((Integer) 56, o.getOrderArray().getNumberOrders());
        write("/orderArray/numberOrders", 22256);
        assertEquals((Integer) 22256, o.getOrderArray().getNumberOrders());

        assertEquals("fName", o.getOrderArray().getOrders()[2].getContact().getFirstName());
        write("/orderArray/orders[2]/contact/firstName", "fName (2)");
        assertEquals("fName (2)", o.getOrderArray().getOrders()[2].getContact().getFirstName());

        assertEquals("lName", o.getOrderArray().getOrders()[2].getContact().getLastName());
        write("/orderArray/orders[2]/contact/lastName", "lName (2)");
        assertEquals("lName (2)", o.getOrderArray().getOrders()[2].getContact().getLastName());

        assertEquals(3425, o.getPrimitives().getIntArrayField()[10]);
        write("/primitives/intArrayField[10]", 2223425);
        assertEquals(2223425, o.getPrimitives().getIntArrayField()[10]);

        assertEquals("boxedString", o.getPrimitives().getBoxedStringArrayField()[10]);
        write("/primitives/boxedStringArrayField[10]", "boxedString (2)");
        assertEquals("boxedString (2)", o.getPrimitives().getBoxedStringArrayField()[10]);

        assertEquals(StateEnumClassLong.Massachusetts, o.getStatesLong());
        write("/statesLong", StateEnumClassLong.Alabama);
        assertEquals(StateEnumClassLong.Alabama, o.getStatesLong());

    }

    public void ensureNotNullAndClass(Object o, Class<?> clz) {
        assertNotNull(o);
        assertEquals(clz, o.getClass());
    }

    @Test
    public void testFindChildObject() throws Exception {
        setupPath("/contact");
        assertTrue(findChildObject(field, lastSegmentContext, targetTestClassInstance) == targetTestClassInstance.getContact());

        reset();
        setupPath("/address");
        assertTrue(findChildObject(field, lastSegmentContext, targetTestClassInstance) == targetTestClassInstance.getAddress());

        reset();
        setupPath("/nothing");
        assertTrue(findChildObject(field, lastSegmentContext, targetTestClassInstance) == null);

        reset();
        setupPath("/orders<0>");
        assertTrue(findChildObject(field, lastSegmentContext, targetOrderListInstance) == targetOrderListInstance.getOrders().get(0));

        reset();
        setupPath("/orders<1>");
        assertTrue(findChildObject(field, lastSegmentContext, targetOrderListInstance) == targetOrderListInstance.getOrders().get(1));

        reset();
        setupPath("/orders<2>");
        assertTrue(findChildObject(field, lastSegmentContext, targetOrderListInstance) == null);

        reset();
        setupPath("/orders[0]");
        assertTrue(findChildObject(field, lastSegmentContext, targetOrderArrayInstance) == targetOrderArrayInstance.getOrders()[0]);

        reset();
        setupPath("/orders[1]");
        assertTrue(findChildObject(field, lastSegmentContext, targetOrderArrayInstance) == targetOrderArrayInstance.getOrders()[1]);

        reset();
        setupPath("/orders[2]");
        assertTrue(findChildObject(field, lastSegmentContext, targetOrderArrayInstance) == null);
    }

    /* these are less critical and are exercised by above tests for now */

    /*
     * @Test(expected=AtlasException.class) public void testFindChildObjectError()
     * throws Exception { throw new Exception("Not implemented yet."); }
     *
     * @Test public void testCreateObject() throws Exception { }
     *
     * @Test(expected=AtlasException.class) public void testCreateObjectError()
     * throws Exception { }
     *
     * @Test(expected=AtlasException.class) public void testWriteError() throws
     * Exception { throw new Exception("Not implemented yet."); }
     *
     * @Test public void testCreateParentObject() throws Exception { throw new
     * Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void
     * testCreateParentObjectError() throws Exception { throw new
     * Exception("Not implemented yet."); }
     *
     * @Test public void testExpandCollectionToFitItem() throws Exception { throw
     * new Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void
     * testExpandCollectionToFitItemError() throws Exception { throw new
     * Exception("Not implemented yet."); }
     *
     * @Test public void testAddChildObject() throws Exception { throw new
     * Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void testAddChildObjectError()
     * throws Exception { throw new Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void
     * testGetObjectFromParentError() throws Exception { throw new
     * Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void testSetObjectOnParentError()
     * throws Exception { throw new Exception("Not implemented yet."); }
     *
     * @Test public void testConvertValue() throws Exception { throw new
     * Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void testConvertValueError()
     * throws Exception { throw new Exception("Not implemented yet."); }
     *
     * @Test public void testGetCollectionSize() throws Exception { throw new
     * Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void testGetCollectionSizeError()
     * throws Exception { throw new Exception("Not implemented yet."); }
     *
     * @Test public void testCreateCollectionWrapperObject() throws Exception {
     * throw new Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void
     * testCreateCollectionWrapperObjectError() throws Exception { throw new
     * Exception("Not implemented yet."); }
     *
     * @Test public void testGetCollectionItem() throws Exception { throw new
     * Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void testGetCollectionItemError()
     * throws Exception { throw new Exception("Not implemented yet."); }
     *
     * @Test public void testCollectionHasRoomForIndex() throws Exception { throw
     * new Exception("Not implemented yet."); }
     *
     * @Test(expected=AtlasException.class) public void
     * testCollectionHasRoomForIndexError() throws Exception { throw new
     * Exception("Not implemented yet."); }
     */
}
