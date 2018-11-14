package io.atlasmap.java.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import io.atlasmap.java.test.TargetAddress;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.FieldType;

@FixMethodOrder(MethodSorters.JVM)
public class DocumentJavaFieldReaderTest extends BaseDocumentReaderTest {

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
        assertEquals(AuditStatus.ERROR, audit.getStatus());
        assertEquals("/address/addressLine1", audit.getPath());
    }
}
