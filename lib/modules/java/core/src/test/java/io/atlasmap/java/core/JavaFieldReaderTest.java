package io.atlasmap.java.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
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
        assertNull(fieldGroup.getField().get(1).getValue());
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

}
