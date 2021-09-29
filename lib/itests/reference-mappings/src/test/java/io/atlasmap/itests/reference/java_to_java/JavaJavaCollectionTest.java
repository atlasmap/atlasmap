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
package io.atlasmap.itests.reference.java_to_java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceCollectionsClass;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.TargetCollectionsClass;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.java.test.TargetAddress;

public class JavaJavaCollectionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCollectionList() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-list.json").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(20, object.getContactList().size());
        for (int i = 0; i < 20; i++) {
            TargetContact contact = object.getContactList().get(i);
            if (i == 4 || i == 19) {
                assertEquals("Ozzie", contact.getFirstName());
            } else {
                assertNull(contact);
            }
        }
    }

    @Test
    public void testProcessCollectionListEmpty() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-list-empty.json").toURI());
        AtlasSession session = context.createSession();
        TargetTestClass source = new TargetTestClass();
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(0, object.getContactList().size());
    }

    @Test
    public void testProcessCollectionArray() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/javaToJava/atlasmapping-collection-array.json").toURI());
        AtlasSession session = context.createSession();
        BaseOrder sourceOrder = AtlasTestUtil.generateOrderClass(SourceOrder.class, SourceAddress.class,
                SourceContact.class);
        session.setDefaultSourceDocument(sourceOrder);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(TargetTestClass.class.getName(), object.getClass().getName());
        assertEquals(20, object.getContactArray().length);
        for (int i = 0; i < 20; i++) {
            TargetContact contact = object.getContactArray()[i];
            if (i == 6 || i == 19) {
                assertEquals("Ozzie", contact.getFirstName());
            } else {
                assertNull(contact);
            }
        }
    }

    @Test
    public void testProcessCollectionListSimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-list-simple.json").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContactList(new LinkedList<>());
        for (int i = 0; i < 5; i++) {
            input.getContactList().add(new TargetContact());
            input.getContactList().get(i).setFirstName("fname" + i);
        }
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(5, object.getContactList().size());
        for (int i = 0; i < 5; i++) {
            assertEquals(input.getContactList().get(i).getFirstName(), object.getContactList().get(i).getFirstName());
        }
    }

    @Test
    public void testProcessCollectionArraySimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-array-simple.json").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContactList(new LinkedList<>());
        for (int i = 0; i < 5; i++) {
            input.getContactList().add(new TargetContact());
            input.getContactList().get(i).setFirstName("fname" + i);
        }
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(5, object.getContactList().size());
        for (int i = 0; i < 5; i++) {
            assertEquals(input.getContactList().get(i).getFirstName(), object.getContactList().get(i).getFirstName());
        }
    }

    @Test
    public void testProcessCollectionToNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-to-noncollection.json").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContactList(new LinkedList<>());
        for (int i = 0; i < 5; i++) {
            input.getContactList().add(new TargetContact());
            input.getContactList().get(i).setFirstName("fname" + i);
        }
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertNull(object.getContactArray());
        assertNull(object.getContactList());
        assertEquals("fname4", object.getContact().getFirstName());
    }

    @Test
    public void testProcessCollectionFromNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-from-noncollection.json").toURI());
        TargetTestClass input = new TargetTestClass();
        input.setContact(new TargetContact());
        input.getContact().setFirstName("first name");
        input.getContact().setLastName("last name");

        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", input);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetTestClass object = (TargetTestClass) session.getDefaultTargetDocument();
        assertEquals(1, object.getContactList().size());
        assertEquals("first name", object.getContactList().get(0).getFirstName());
        assertEquals("last name", object.getContactList().get(0).getLastName());
    }

    @Test
    public void testProcessCollectionPrimitive() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-flatprimitive.json").toURI());
        SourceFlatPrimitiveClass source = new SourceFlatPrimitiveClass();
        source.setBoxedStringField("fuga");
        source.setBoxedStringArrayField(new String[] {"foo", "bar", "hoge", "fuga"});
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetFlatPrimitiveClass target = (TargetFlatPrimitiveClass) session.getDefaultTargetDocument();
        assertEquals("fuga", target.getBoxedStringField());
        assertEquals(1, target.getBoxedStringArrayField().length);
        assertEquals("fuga", target.getBoxedStringArrayField()[0]);
    }

    @Test
    public void testProcessCollectionImpls() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-impls.json").toURI());
        SourceCollectionsClass source = new SourceCollectionsClass();
        List<String> list = new LinkedList<>();
        list.addAll(Arrays.asList(new String[] {"list0", "list1", "list2"}));
        source.setList(list);
        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.addAll(Arrays.asList(new String[] {"linkedList0", "linkedList1", "linkedList2"}));
        source.setLinkedList(linkedList);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList(new String[] {"arrayList0", "arrayList1", "arrayList2"}));
        source.setArrayList(arrayList);
        Set<String> set = new HashSet<>();
        set.addAll(Arrays.asList(new String[] {"set0", "set1", "set2"}));
        source.setSet(new HashSet<>(set));
        HashSet<String> hashSet = new HashSet<>();
        hashSet.addAll(Arrays.asList(new String[] {"hashSet0", "hashSet1", "hashSet2"}));
        source.setHashSet(hashSet);
        AtlasSession session = context.createSession();
        session.setSourceDocument("SourceCollectionsClass", source);

        SourceFlatPrimitiveClass sfpc0 = new SourceFlatPrimitiveClass();
        sfpc0.setBoxedStringField("sfpc0");
        session.setSourceDocument("sfpc0", sfpc0);
        SourceFlatPrimitiveClass sfpc1 = new SourceFlatPrimitiveClass();
        sfpc1.setBoxedStringField("sfpc1");
        session.setSourceDocument("sfpc1", sfpc1);
        SourceFlatPrimitiveClass sfpc2 = new SourceFlatPrimitiveClass();
        sfpc2.setBoxedStringField("sfpc2");
        session.setSourceDocument("sfpc2", sfpc2);
        SourceFlatPrimitiveClass sfpc3 = new SourceFlatPrimitiveClass();
        sfpc3.setBoxedStringField("sfpc3");
        session.setSourceDocument("sfpc3", sfpc3);
        SourceFlatPrimitiveClass sfpc4 = new SourceFlatPrimitiveClass();
        sfpc4.setBoxedStringField("sfpc4");
        session.setSourceDocument("sfpc4", sfpc4);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetCollectionsClass targetCollections = (TargetCollectionsClass) session.getTargetDocument("TargetCollectionsClass");
        list = targetCollections.getList();
        assertEquals(4, list.size());
        assertTrue(list.contains("arrayList0"));
        assertTrue(list.contains("arrayList1"));
        assertTrue(list.contains("arrayList2"));
        assertTrue(list.contains("sfpc0"));
        linkedList = targetCollections.getLinkedList();
        assertEquals(4, linkedList.size());
        assertTrue(linkedList.contains("set0"));
        assertTrue(linkedList.contains("set1"));
        assertTrue(linkedList.contains("set2"));
        assertTrue(linkedList.contains("sfpc1"));
        arrayList = targetCollections.getArrayList();
        assertEquals(4, arrayList.size());
        assertTrue(arrayList.contains("hashSet0"));
        assertTrue(arrayList.contains("hashSet1"));
        assertTrue(arrayList.contains("hashSet2"));
        assertTrue(arrayList.contains("sfpc2"));
        set = targetCollections.getSet();
        assertEquals(4, set.size());
        assertTrue(set.contains("list0"));
        assertTrue(set.contains("list1"));
        assertTrue(set.contains("list2"));
        assertTrue(set.contains("sfpc3"));
        hashSet = targetCollections.getHashSet();
        assertEquals(4, hashSet.size());
        assertTrue(hashSet.contains("linkedList0"));
        assertTrue(hashSet.contains("linkedList1"));
        assertTrue(hashSet.contains("linkedList2"));
        assertTrue(hashSet.contains("sfpc4"));
        TargetFlatPrimitiveClass tfpc0 = (TargetFlatPrimitiveClass) session.getTargetDocument("tfpc0");
        assertTrue(tfpc0.getBoxedStringField().startsWith("list"));
        TargetFlatPrimitiveClass tfpc1 = (TargetFlatPrimitiveClass) session.getTargetDocument("tfpc1");
        assertTrue(tfpc1.getBoxedStringField().startsWith("linkedList"));
        TargetFlatPrimitiveClass tfpc2 = (TargetFlatPrimitiveClass) session.getTargetDocument("tfpc2");
        assertTrue(tfpc2.getBoxedStringField().startsWith("arrayList"));
        TargetFlatPrimitiveClass tfpc3 = (TargetFlatPrimitiveClass) session.getTargetDocument("tfpc3");
        assertTrue(tfpc3.getBoxedStringField().startsWith("set"));
        TargetFlatPrimitiveClass tfpc4 = (TargetFlatPrimitiveClass) session.getTargetDocument("tfpc4");
        assertTrue(tfpc4.getBoxedStringField().startsWith("hashSet"));
    }

    @Test
    public void testProcessCollectionFieldAction() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-fieldaction2.json").toURI());
        SourceCollectionsClass source = new SourceCollectionsClass();
        LinkedList<String> list = new LinkedList<>();
        list.addAll(Arrays.asList(new String[] {"linkedList0", "linkedList1", "linkedList2"}));
        source.setLinkedList(list);
        ArrayList<SourceOrder> orderList = new ArrayList<>();
        for (int i=0; i<3; i++) {
            SourceOrder order = new SourceOrder();
            order.setOrderId(i);
            order.setCreated(Date.from(Instant.now()));
            orderList.add(order);
        }
        source.setOrderList(orderList);
        ArrayList<SourceAddress> addressList = new ArrayList<>();
        for (int i=0; i<3; i++) {
            SourceAddress addr = new SourceAddress();
            addr.setCity("city" + i);
            addr.setState("state" + i);
            addressList.add(addr);
        }
        source.setAddressList(addressList);
        ArrayList<SourceContact> contactList = new ArrayList<>();
        for (int i=0; i<3; i++) {
            SourceContact ct = new SourceContact();
            ct.setFirstName("first" + i);
            ct.setLastName("last" + i);
            contactList.add(ct);
        }
        source.setContactList(contactList);
        AtlasSession session = context.createSession();
        session.setSourceDocument("SourceCollectionsClass", source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetCollectionsClass targetCollections = (TargetCollectionsClass) session.getTargetDocument("TargetCollectionsClass");
        ArrayList<String> result = targetCollections.getArrayList();
        assertEquals(1, result.size());
        assertEquals("linkedList0#linkedList1#linkedList2", result.get(0));

        List<TargetOrder> targetOrderList = targetCollections.getOrderList();
        assertEquals(3, targetOrderList.size());
        for (int i=0; i<3; i++) {
            TargetOrder order = targetOrderList.get(i);
            assertEquals(i, order.getOrderId());
            assertNotNull(order.getCreated());
        }
        List<TargetAddress> targetAddrList = targetCollections.getAddressList();
        assertEquals(3, targetAddrList.size());
        for (int i=0; i<3; i++) {
            TargetAddress addr = targetAddrList.get(i);
            assertEquals("city" + i, addr.getCity());
            assertEquals("STATE" + i, addr.getState());
        }
        List<TargetContact> targetContactList = targetCollections.getContactList();
        assertEquals(3, targetContactList.size());
        for (int i=0; i<3; i++) {
            TargetContact contact = targetContactList.get(i);
            assertEquals("First" + i, contact.getFirstName());
            assertEquals("last" + i, contact.getLastName());
        }
    }

    @Test
    public void testProcessCollectionNullItem() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-fieldaction2.json").toURI());
        SourceCollectionsClass source = new SourceCollectionsClass();
        ArrayList<SourceOrder> orderList = new ArrayList<>();
        for (int i=0; i<3; i++) {
            if (i == 1) {
                orderList.add(null);
                continue;
            }
            SourceOrder order = new SourceOrder();
            order.setOrderId(i);
            order.setCreated(Date.from(Instant.now()));
            orderList.add(order);
        }
        source.setOrderList(orderList);
        ArrayList<SourceAddress> addressList = new ArrayList<>();
        for (int i=0; i<3; i++) {
            if (i == 1) {
                addressList.add(null);
                continue;
            }
            SourceAddress addr = new SourceAddress();
            addr.setCity("city" + i);
            addr.setState("state" + i);
            addressList.add(addr);
        }
        source.setAddressList(addressList);
        ArrayList<SourceContact> contactList = new ArrayList<>();
        for (int i=0; i<3; i++) {
            if (i == 1) {
                contactList.add(null);
                continue;
            }
            SourceContact ct = new SourceContact();
            ct.setFirstName("first" + i);
            ct.setLastName("last" + i);
            contactList.add(ct);
        }
        source.setContactList(contactList);

        AtlasSession session = context.createSession();
        session.setSourceDocument("SourceCollectionsClass", source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetCollectionsClass targetCollections = (TargetCollectionsClass) session.getTargetDocument("TargetCollectionsClass");
        List<TargetOrder> targetOrderList = targetCollections.getOrderList();
        assertEquals(3, targetOrderList.size());
        for (int i=0; i<3; i++) {
            TargetOrder order = targetOrderList.get(i);
            if (i == 1) {
                assertNull(order);
            } else {
                assertEquals(i, order.getOrderId());
                assertNotNull(order.getCreated());
            }
        }
        List<TargetAddress> targetAddrList = targetCollections.getAddressList();
        assertEquals(3, targetAddrList.size());
        for (int i=0; i<3; i++) {
            TargetAddress addr = targetAddrList.get(i);
            if (i == 1) {
                assertNull(addr);
            } else {
                assertEquals("city" + i, addr.getCity());
                assertEquals("STATE" + i, addr.getState());
            }
        }
        List<TargetContact> targetContactList = targetCollections.getContactList();
        assertEquals(3, targetContactList.size());
        for (int i=0; i<3; i++) {
            TargetContact contact = targetContactList.get(i);
            if (i == 1) {
                assertNull(contact);
            } else {
                assertEquals("First" + i, contact.getFirstName());
                assertEquals("last" + i, contact.getLastName());
            }
        }
    }

    @Test
    public void testProcessCollectionEmptyItem() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/javaToJava/atlasmapping-collection-fieldaction2.json").toURI());
        SourceCollectionsClass source = new SourceCollectionsClass();
        ArrayList<SourceOrder> orderList = new ArrayList<>();
        for (int i=0; i<3; i++) {
            SourceOrder order = new SourceOrder();
            if (i != 1) {
                order.setOrderId(i);
                order.setCreated(Date.from(Instant.now()));
            }
            orderList.add(order);
        }
        source.setOrderList(orderList);
        ArrayList<SourceAddress> addressList = new ArrayList<>();
        for (int i=0; i<3; i++) {
            SourceAddress addr = new SourceAddress();
            if (i != 1) {
                addr.setCity("city" + i);
                addr.setState("state" + i);
            }
            addressList.add(addr);
        }
        source.setAddressList(addressList);
        ArrayList<SourceContact> contactList = new ArrayList<>();
        for (int i=0; i<3; i++) {
            SourceContact ct = new SourceContact();
            if (i != 1) {
                ct.setFirstName("first" + i);
                ct.setLastName("last" + i);
            }
            contactList.add(ct);
        }
        source.setContactList(contactList);

        AtlasSession session = context.createSession();
        session.setSourceDocument("SourceCollectionsClass", source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        TargetCollectionsClass targetCollections = (TargetCollectionsClass) session.getTargetDocument("TargetCollectionsClass");
        List<TargetOrder> targetOrderList = targetCollections.getOrderList();
        assertEquals(3, targetOrderList.size());
        for (int i=0; i<3; i++) {
            TargetOrder order = targetOrderList.get(i);
            if (i == 1) {
                assertNull(order);
            } else {
                assertEquals(i, order.getOrderId());
                assertNotNull(order.getCreated());
            }
        }
        List<TargetAddress> targetAddrList = targetCollections.getAddressList();
        assertEquals(3, targetAddrList.size());
        for (int i=0; i<3; i++) {
            TargetAddress addr = targetAddrList.get(i);
            if (i == 1) {
                assertNull(addr);
            } else {
                assertEquals("city" + i, addr.getCity());
                assertEquals("STATE" + i, addr.getState());
            }
        }
        List<TargetContact> targetContactList = targetCollections.getContactList();
        assertEquals(3, targetContactList.size());
        for (int i=0; i<3; i++) {
            TargetContact contact = targetContactList.get(i);
            if (i == 1) {
                assertNull(contact);
            } else {
                assertEquals("First" + i, contact.getFirstName());
                assertEquals("last" + i, contact.getLastName());
            }
        }
    }
}
