package io.atlasmap.java.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetOrder;
import io.atlasmap.java.test.TargetOrderArray;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;

@FixMethodOrder(MethodSorters.JVM)
public class JavaFieldReaderTest extends BaseJavaFieldReaderTest {

    @Test
    public void testRead() throws Exception {
        TargetTestClass source = new TargetTestClass();
        source.setAddress(new TargetAddress());
        source.getAddress().setAddressLine1("123 any street");
        reader.setDocument(source);
        read("/address/addressLine1", FieldType.STRING);
        assertEquals("123 any street", field.getValue());
        assertEquals(0, audits.size());
    }

    @Test
    public void testReadNullValue() throws Exception {
        TargetTestClass source = new TargetTestClass();
        reader.setDocument(source);
        read("/name", FieldType.STRING);
        assertNull(field.getValue());
        assertEquals(0, audits.size());
    }

    @Test
    public void testReadNotExistingPath() throws Exception {
        TargetTestClass source = new TargetTestClass();
        reader.setDocument(source);
        read("/address/addressLine1", FieldType.STRING);
        assertNull(field.getValue());
        assertEquals(1, audits.size());
        Audit audit = audits.get(0);
        assertEquals(AuditStatus.WARN, audit.getStatus());
        assertEquals("/address/addressLine1", audit.getPath());
    }

    @Test
    public void testReadTopmostArrayString() throws Exception {
        String[] stringArray = new String[] {"one", "two"};
        reader.setDocument(stringArray);
        read("/[0]", FieldType.STRING);
        assertEquals("one", field.getValue());
        assertEquals(0, audits.size());
        readGroup("/[]", FieldType.STRING);
        assertNotNull(fieldGroup);
        assertEquals(2, fieldGroup.getField().size());
        assertEquals("one", fieldGroup.getField().get(0).getValue());
        assertEquals("two", fieldGroup.getField().get(1).getValue());
        assertEquals(0, audits.size());
    }

    @Test
    public void testReadTopmostListString() throws Exception {
        List<String> stringList = Arrays.asList(new String[] {"one", "two"});
        reader.setDocument(stringList);
        read("/<0>", FieldType.STRING);
        assertEquals("one", field.getValue());
        assertEquals(0, audits.size());
        readGroup("/<>", FieldType.STRING);
        assertNotNull(fieldGroup);
        assertEquals(2, fieldGroup.getField().size());
        assertEquals("one", fieldGroup.getField().get(0).getValue());
        assertEquals("two", fieldGroup.getField().get(1).getValue());
        assertEquals(0, audits.size());
    }

    @Test
    public void testReadTopmostArrayComplex() throws Exception {
        TargetTestClass[] complexArray = new TargetTestClass[] {new TargetTestClass(), new TargetTestClass()};
        complexArray[0].setAddress(new TargetAddress());
        complexArray[0].getAddress().setAddressLine1("123 any street");
        reader.setDocument(complexArray);
        read("/[0]/address/addressLine1", FieldType.STRING);
        assertEquals("123 any street", field.getValue());
        assertEquals(0, audits.size());
        readGroup("/[]/address/addressLine1", FieldType.STRING);
        assertNotNull(fieldGroup);
        assertEquals(2, fieldGroup.getField().size());
        assertEquals("123 any street", fieldGroup.getField().get(0).getValue());
        assertEquals(1, audits.size());
        assertEquals(AuditStatus.WARN, audits.get(0).getStatus());
        assertEquals("/[1]/address/addressLine1", audits.get(0).getPath());
    }

    @Test
    public void testReadTopmostListComplex() throws Exception {
        List<TargetTestClass> complexList = Arrays.asList(new TargetTestClass[] {new TargetTestClass(), new TargetTestClass()});
        complexList.get(0).setAddress(new TargetAddress());
        complexList.get(0).getAddress().setAddressLine1("123 any street");
        reader.setDocument(complexList);
        read("/<0>/address/addressLine1", FieldType.STRING);
        assertEquals("123 any street", field.getValue());
        assertEquals(0, audits.size());
        readGroup("/<>/address/addressLine1", FieldType.STRING);
        assertNotNull(fieldGroup);
        assertEquals(2, fieldGroup.getField().size());
        assertEquals("123 any street", fieldGroup.getField().get(0).getValue());
        assertNull(fieldGroup.getField().get(1).getValue());
        assertEquals(1, audits.size());
        assertEquals(AuditStatus.WARN, audits.get(0).getStatus());
        assertEquals("/<1>/address/addressLine1", audits.get(0).getPath());
    }

    @Test
    public void testReadParent() throws Exception {
        TargetTestClass source = new TargetTestClass();
        LinkedList<TargetContact> contactList = new LinkedList<>();
        for (int i=0; i<5; i++) {
            TargetContact c = new TargetContact();
            c.setFirstName("f" + i);
            c.setLastName("l" + i);
            c.setPhoneNumber("p" + i);
            c.setZipCode("z" + i);
            contactList.add(c);
        }
        source.setContactList(contactList);

        reader.setDocument(source);
        FieldGroup contactListGroup = new FieldGroup();
        contactListGroup.setDocId("TargetTestClass");
        contactListGroup.setPath("/contactList<>");
        contactListGroup.setFieldType(FieldType.COMPLEX);
        contactListGroup.setCollectionType(CollectionType.LIST);
        Field firstNameField = new JavaField();
        firstNameField.setDocId("TargetTestClass");
        firstNameField.setPath("/contactList<>/firstName");
        firstNameField.setFieldType(FieldType.STRING);
        contactListGroup.getField().add(firstNameField);
        fieldGroup = (FieldGroup) read(contactListGroup);
        assertEquals("/contactList<>", fieldGroup.getPath());
        assertEquals(FieldType.COMPLEX, fieldGroup.getFieldType());
        assertEquals(CollectionType.LIST, fieldGroup.getCollectionType());
        assertEquals(5, fieldGroup.getField().size());
        Field contact0 = fieldGroup.getField().get(0);
        assertEquals(FieldGroup.class, contact0.getClass());
        FieldGroup contact0Group = (FieldGroup)contact0;
        assertEquals(1, contact0Group.getField().size());
        assertEquals("/contactList<0>", contact0Group.getPath());
        assertEquals(FieldType.COMPLEX, contact0Group.getFieldType());
        Field firstName0 = contact0Group.getField().get(0);
        assertEquals("/contactList<0>/firstName", firstName0.getPath());
        assertEquals(FieldType.STRING, firstName0.getFieldType());
        assertEquals("f0", firstName0.getValue());
        contactListGroup.setPath("/contactList<1>");
        firstNameField.setPath("/contactList<1>/firstName");
        fieldGroup = (FieldGroup) read(contactListGroup);
        assertEquals(1, fieldGroup.getField().size());
        Field firstName1 = fieldGroup.getField().get(0);
        assertEquals("/contactList<1>/firstName", firstName1.getPath());
        assertEquals(FieldType.STRING, firstName1.getFieldType());
        assertEquals("f1", firstName1.getValue());
    }

    @Test
    public void testReadParentCollectionsChildren() throws Exception {
        TargetTestClass source = new TargetTestClass();
        TargetOrderArray orderArray = new TargetOrderArray();
        source.setOrderArray(orderArray);
        TargetOrder[] orders = new TargetOrder[5];
        orderArray.setOrders(orders);
        for (int i=0; i<5; i++) {
            TargetOrder order = new TargetOrder();
            TargetContact c = new TargetContact();
            c.setFirstName("f" + i);
            c.setLastName("l" + i);
            c.setPhoneNumber("p" + i);
            c.setZipCode("z" + i);
            order.setContact(c);
            orders[i] = order;
        }

        reader.setDocument(source);
        FieldGroup contactGroup = new FieldGroup();
        contactGroup.setDocId("TargetTestClass");
        contactGroup.setPath("/orderArray/orders[]/contact");
        contactGroup.setFieldType(FieldType.COMPLEX);
        Field firstNameField = new JavaField();
        firstNameField.setDocId("TargetTestClass");
        firstNameField.setPath("/orderArray/orders[]/contact/firstName");
        firstNameField.setFieldType(FieldType.STRING);
        contactGroup.getField().add(firstNameField);
        fieldGroup = (FieldGroup) read(contactGroup);
        assertEquals("/orderArray/orders[]/contact", fieldGroup.getPath());
        assertEquals(FieldType.COMPLEX, fieldGroup.getFieldType());
        assertNull(fieldGroup.getCollectionType());
        assertEquals(5, fieldGroup.getField().size());
        Field contact0 = fieldGroup.getField().get(0);
        assertEquals(FieldGroup.class, contact0.getClass());
        FieldGroup contact0Group = (FieldGroup)contact0;
        assertEquals(1, contact0Group.getField().size());
        assertEquals("/orderArray/orders[0]/contact", contact0Group.getPath());
        assertEquals(FieldType.COMPLEX, contact0Group.getFieldType());
        assertNull(contact0Group.getCollectionType());
        Field firstName0 = contact0Group.getField().get(0);
        assertEquals("/orderArray/orders[0]/contact/firstName", firstName0.getPath());
        assertEquals(FieldType.STRING, firstName0.getFieldType());
        assertNull(firstName0.getCollectionType());
        assertEquals("f0", firstName0.getValue());
        contactGroup.setPath("/orderArray/orders[1]/contact");
        firstNameField.setPath("/orderArray/orders[1]/contact/firstName");
        fieldGroup = (FieldGroup) read(contactGroup);
        assertEquals(1, fieldGroup.getField().size());
        assertEquals("/orderArray/orders[1]/contact", fieldGroup.getPath());
        assertEquals(FieldType.COMPLEX, fieldGroup.getFieldType());
        Field firstName1 = fieldGroup.getField().get(0);
        assertEquals("/orderArray/orders[1]/contact/firstName", firstName1.getPath());
        assertEquals(FieldType.STRING, firstName1.getFieldType());
        assertEquals("f1", firstName1.getValue());
    }
}
