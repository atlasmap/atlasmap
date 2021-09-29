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
package io.atlasmap.java.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.atlasmap.api.AtlasException;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.StateEnumClassLong;
import io.atlasmap.java.test.StringTestClass;
import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetCollectionsClass;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetOrderArray;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.java.test.TestListOrders;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.FieldType;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class JavaFieldWriterTest extends BaseJavaFieldWriterTest {

    @Test
    public void testSimpleClassLookup() throws Exception {
        this.writer.setRootObject(new TargetTestClass());
        write("/address/addressLine1", "123 any street");
        TargetTestClass o = (TargetTestClass) writer.getRootObject();
        ensureNotNullAndClass(o, TargetTestClass.class);
        ensureNotNullAndClass(o.getAddress(), TargetAddress.class);
        assertEquals("123 any street", o.getAddress().getAddressLine1());
    }

    @Test
    public void testClassLookupFromField() throws Exception {
        TestListOrders o = new TestListOrders();
        writer.setRootObject(o);

        JavaField f = createField("/orders<4>", null);
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
        writer.setRootObject(new TargetFlatPrimitiveClass());
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
    public void testClassLookupAbstract() throws Exception {
        writer.setRootObject(new TargetTestClass());
        assertThrows(AtlasException.class, () -> {
            write(createField("/orders[4]/address/addressLine1", "hello world."));
        });
    }

    @Test
    public void testClassLookupReflection() throws Exception {
        writer.setRootObject(new TargetTestClass());
        write("/address/addressLine1", "123 any street");
        TargetTestClass o = (TargetTestClass) writer.getRootObject();
        ensureNotNullAndClass(o, TargetTestClass.class);
        ensureNotNullAndClass(o.getAddress(), TargetAddress.class);
        assertEquals("123 any street", o.getAddress().getAddressLine1());
    }

    @Test
    public void testSimpleWrite() throws Exception {
        writer.setRootObject(new TargetAddress());
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
    public void testWriteStringTypes() throws Exception {
        writer.setRootObject(new StringTestClass());
        write(createField("/testCharBuffer", "testCharBuffer", FieldType.STRING, "java.nio.CharBuffer"));
        write(createField("/testCharSequence", "testCharSequence", FieldType.STRING, "java.lang.CharSequence"));
        write(createField("/testString", "testString"));
        write(createField("/testStringBuffer", "testStringBuffer", FieldType.STRING, "java.lang.StringBuffer"));
        write(createField("/testStringBuilder", "testStringBuilder", FieldType.STRING, "java.lang.StringBuilder"));
        StringTestClass o = (StringTestClass) writer.getRootObject();
        assertEquals("testCharBuffer", o.getTestCharBuffer().toString());
        assertEquals("testCharSequence", o.getTestCharSequence().toString());
        assertEquals("testString", o.getTestString());
        assertEquals("testStringBuffer", o.getTestStringBuffer().toString());
        assertEquals("testStringBuilder", o.getTestStringBuilder().toString());
    }

    @Test
    public void testSimpleWriteCollectionList() throws Exception {
        writer.setRootObject(new TestListOrders());
        writeComplex("/orders<4>", new TargetOrder());
        writeComplex("/orders<4>/address", new TargetAddress());
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
    public void testWriteCollectionImpls() throws Exception {
        writer.setRootObject(new TargetCollectionsClass());
        write("/list<0>", "list0");
        write("/linkedList<1>", "linkedList1");
        write("/arrayList<2>", "arrayList2");
        write("/set<3>", "set3");
        write("/hashSet<4>", "hashSet4");
        TargetCollectionsClass tcc = (TargetCollectionsClass) writer.getRootObject();
        ensureNotNullAndClass(tcc, TargetCollectionsClass.class);
        assertTrue(tcc.getList().contains("list0"));
        assertTrue(tcc.getLinkedList().contains("linkedList1"));
        assertTrue(tcc.getArrayList().contains("arrayList2"));
        assertTrue(tcc.getSet().contains("set3"));
        assertTrue(tcc.getHashSet().contains("hashSet4"));
    }

    @Test
    public void testSimpleWriteCollectionArray() throws Exception {
        writer.setRootObject(new TargetOrderArray());
        writeComplex("/orders[4]", new TargetOrder());
        writeComplex("/orders[4]/address", new TargetAddress());
        write("/orders[4]/address/addressLine1", "hello world.");
        TargetOrderArray o = (TargetOrderArray) writer.getRootObject();
        ensureNotNullAndClass(o, TargetOrderArray.class);
        ensureNotNullAndClass(o.getOrders(), BaseOrder[].class);
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
        writer.setRootObject(new TestListOrders());
        writeComplex("/orders<4>", new TargetOrder());
        writeComplex("/orders<4>/address", new TargetAddress());
        write("/orders<4>/address/addressLine1", "hello world1.");
        writeComplex("/orders<14>", new TargetOrder());
        writeComplex("/orders<14>/address", new TargetAddress());
        write("/orders<14>/address/addressLine1", "hello world2.");
        writeComplex("/orders<2>", new TargetOrder());
        writeComplex("/orders<2>/address", new TargetAddress());
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
        writer.setRootObject(new TargetOrderArray());
        writeComplex("/orders[4]", new TargetOrder());
        writeComplex("/orders[4]/address", new TargetAddress());
        write("/orders[4]/address/addressLine1", "hello world1.");
        writeComplex("/orders[14]", new TargetOrder());
        writeComplex("/orders[14]/address", new TargetAddress());
        write("/orders[14]/address/addressLine1", "hello world2.");
        writeComplex("/orders[2]", new TargetOrder());
        writeComplex("/orders[2]/address", new TargetAddress());
        write("/orders[2]/address/addressLine1", "hello world3.");
        TargetOrderArray o = (TargetOrderArray) writer.getRootObject();
        ensureNotNullAndClass(o, TargetOrderArray.class);
        ensureNotNullAndClass(o.getOrders(), BaseOrder[].class);
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
        writer.setRootObject(new TargetFlatPrimitiveClass());
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
        writer.setRootObject(new TargetTestClass());
        write("/name", "someName");

        TargetTestClass o = (TargetTestClass) writer.getRootObject();
        ensureNotNullAndClass(o, TargetTestClass.class);
        assertEquals("someName", o.getName());

        writeComplex("/address", new TargetAddress());
        write("/address/addressLine1", "123 any street");
        ensureNotNullAndClass(o.getAddress(), TargetAddress.class);
        assertEquals("123 any street", o.getAddress().getAddressLine1());

        writeComplex("/listOrders/orders<5>", new TargetOrder());
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

        writeComplex("/listOrders/orders<2>", new TargetOrder());
        writeComplex("/listOrders/orders<2>/address", new TargetAddress());
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

        writeComplex("/orderArray/orders[2]", new TargetOrder());
        writeComplex("/orderArray/orders[2]/contact", new TargetContact());
        write("/orderArray/orders[2]/contact/firstName", "fName");
        ensureNotNullAndClass(o.getOrderArray().getOrders(), BaseOrder[].class);
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

        // test writing null enum values
        write("/statesLong", (StateEnumClassLong)null);
        assertNull(o.getStatesLong());

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
    public void testTopmostArrayString() throws Exception {
        writer.setRootObject(new String[0]);
        write("/[0]", "zero");
        write("/[1]", "one");
        String[] o = (String[]) writer.getRootObject();
        ensureNotNullAndClass(o, String[].class);
        assertEquals("zero", o[0]);
        assertEquals("one", o[1]);
    }

    @Test
    public void testTopmostListString() throws Exception {
        writer.setRootObject(new ArrayList<String>());
        write("/<0>", "zero");
        write("/<1>", "one");
        List<String> o = (List<String>) writer.getRootObject();
        ensureNotNullAndClass(o, ArrayList.class);
        assertEquals("zero", o.get(0));
        assertEquals("one", o.get(1));
    }

    @Test
    public void testTopmostArrayComplex() throws Exception {
        writer.setRootObject(new TargetTestClass[0]);
        writer.setCollectionItemClass(TargetTestClass.class);
        writeComplex("/[0]/address", new TargetAddress());
        write("/[0]/address/addressLine1", "zero");
        writeComplex("/[1]/address", new TargetAddress());
        write("/[1]/address/addressLine1", "one");
        TargetTestClass[] o = (TargetTestClass[]) writer.getRootObject();
        ensureNotNullAndClass(o, TargetTestClass[].class);
        assertEquals("zero", o[0].getAddress().getAddressLine1());
        assertEquals("one", o[1].getAddress().getAddressLine1());
    }

    @Test
    public void testTopmostListComplex() throws Exception {
        writer.setRootObject(new ArrayList<TargetTestClass>());
        writer.setCollectionItemClass(TargetTestClass.class);
        writeComplex("/<0>/address", new TargetAddress());
        write("/<0>/address/addressLine1", "zero");
        writeComplex("/<1>/address", new TargetAddress());
        write("/<1>/address/addressLine1", "one");
        List<TargetTestClass> o = (List<TargetTestClass>) writer.getRootObject();
        ensureNotNullAndClass(o, ArrayList.class);
        assertEquals("zero", o.get(0).getAddress().getAddressLine1());
        assertEquals("one", o.get(1).getAddress().getAddressLine1());
    }

    @Test
    public void testListComplexSkipIndexedPath() throws Exception {
        writer.setRootObject(new TargetCollectionsClass());
        writeComplex("/contactList<0>", new TargetContact());
        write("/contactList<0>/firstName", "first0");
        writeComplex("/contactList<2>", new TargetContact());
        write("/contactList<2>/firstName", "first2");
        TargetCollectionsClass o = (TargetCollectionsClass)writer.getRootObject();
        assertEquals(3, o.getContactList().size());
        assertEquals("first0", o.getContactList().get(0).getFirstName());
        assertNull(o.getContactList().get(1));
        assertEquals("first2", o.getContactList().get(2).getFirstName());
    }
}
