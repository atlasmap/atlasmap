package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.Validations;

public class DefaultAtlasSessionTest {

    private DefaultAtlasSession session = null;

    @Before
    public void setUp() throws Exception {
        session = new DefaultAtlasSession(AtlasTestData.generateAtlasMapping());
    }

    @After
    public void tearDown() throws Exception {
        session = null;
    }

    @Test
    public void testInitializeDefaultAtlasSession() {
        assertNotNull(session);
        assertNotNull(session.getMapping());
        assertNull(session.getAtlasContext());
        assertNotNull(session.getAudits());
        assertNotNull(session.getProperties());
        assertNotNull(session.getValidations());
        assertNull(session.getDefaultSourceDocument());
        assertNull(session.getDefaultTargetDocument());
    }

    @Test
    public void testGetSetAtlasContext() throws Exception {
        session.setAtlasContext(new DefaultAtlasContext(new URI("file:foo")));
        assertNotNull(session.getAtlasContext());
        assertTrue(session.getAtlasContext() instanceof DefaultAtlasContext);
    }

    @Test
    public void testGetMapping() {
        assertNotNull(session.getMapping());
        assertNotNull(session.getMapping().getProperties());
        assertNotNull(session.getMapping().getProperties().getProperty());
        assertTrue(session.getMapping().getProperties().getProperty().size() > 0);
    }

    @Test
    public void testGetSetValidations() {
        assertNotNull(session.getValidations());
        assertNotNull(session.getValidations().getValidation());
        assertTrue(session.getValidations().getValidation().size() == 0);

        Validations validations = new Validations();
        Validation validation = new Validation();
        validation.setScope(ValidationScope.MAPPING);
        validation.setId("bar");
        validations.getValidation().add(validation);

        session.setValidations(validations);
        assertNotNull(session.getValidations());
        assertNotNull(session.getValidations().getValidation());
        assertTrue(session.getValidations().getValidation().size() == 1);
    }

    @Test
    public void testGetSetAudits() {
        assertNotNull(session.getAudits());
        assertNotNull(session.getAudits().getAudit());
        assertTrue(session.getAudits().getAudit().size() == 0);

        Audits audits = new Audits();
        Audit audit = new Audit();
        audit.setStatus(AuditStatus.INFO);
        audit.setMessage("hello");

        audits.getAudit().add(audit);

        session.setAudits(audits);
        assertNotNull(session.getAudits());
        assertNotNull(session.getAudits().getAudit());
        assertTrue(session.getAudits().getAudit().size() == 1);
    }

    @Test
    public void testGetSetInput() {
        session.setDefaultSourceDocument(new String("defaultInput"));
        assertNotNull(session.getDefaultSourceDocument());
        assertTrue(session.getDefaultSourceDocument() instanceof String);
        assertEquals("defaultInput", (String) session.getDefaultSourceDocument());
    }

    @Test
    public void testGetSetInputDocId() {
        session.setDefaultSourceDocument(new String("defaultInput"));
        assertNotNull(session.getDefaultSourceDocument());
        assertTrue(session.getDefaultSourceDocument() instanceof String);
        assertEquals("defaultInput", (String) session.getDefaultSourceDocument());

        session.setSourceDocument("second", new String("secondInput"));
        assertNotNull(session.getDefaultSourceDocument());
        assertTrue(session.getDefaultSourceDocument() instanceof String);
        assertEquals("defaultInput", (String) session.getDefaultSourceDocument());

        assertTrue(session.hasSourceDocument("second"));
        assertFalse(session.hasSourceDocument("third"));
        assertNotNull(session.getSourceDocument("second"));
        assertTrue(session.getSourceDocument("second") instanceof String);
        assertEquals("secondInput", (String) session.getSourceDocument("second"));
    }

    @Test
    public void testGetSetOutput() {
        session.setDefaultTargetDocument(new String("defaultOutput"));
        assertNotNull(session.getDefaultTargetDocument());
        assertTrue(session.getDefaultTargetDocument() instanceof String);
        assertEquals("defaultOutput", (String) session.getDefaultTargetDocument());
    }

    @Test
    public void testGetSetOutputDocId() {
        session.setDefaultTargetDocument(new String("defaultOutput"));
        assertNotNull(session.getDefaultTargetDocument());
        assertTrue(session.getDefaultTargetDocument() instanceof String);
        assertEquals("defaultOutput", (String) session.getDefaultTargetDocument());

        session.setTargetDocument("second", new String("secondOutput"));
        assertNotNull(session.getDefaultTargetDocument());
        assertTrue(session.getDefaultTargetDocument() instanceof String);
        assertEquals("defaultOutput", (String) session.getDefaultTargetDocument());

        assertTrue(session.hasTargetDocument("second"));
        assertFalse(session.hasTargetDocument("third"));
        assertNotNull(session.getTargetDocument("second"));
        assertTrue(session.getTargetDocument("second") instanceof String);
        assertEquals("secondOutput", (String) session.getTargetDocument("second"));
    }

    @Test
    public void testGetProperties() {
        assertNotNull(session);
        assertNotNull(session.getProperties());
        assertTrue(session.getProperties().size() == 0);

        session.getProperties().put("foo", "bar");
        assertTrue(session.getProperties().size() == 1);
        assertEquals("bar", (String) session.getProperties().get("foo"));
    }

    @Test
    public void testAuditErrors() {
        assertTrue(session.errorCount() == 0);
        assertFalse(session.hasErrors());
        assertTrue(session.warnCount() == 0);
        assertFalse(session.hasWarns());

        Audit error = new Audit();
        error.setStatus(AuditStatus.ERROR);
        session.getAudits().getAudit().add(error);

        assertTrue(session.errorCount() == 1);
        assertTrue(session.hasErrors());
        assertTrue(session.warnCount() == 0);
        assertFalse(session.hasWarns());
    }

    @Test
    public void testAuditWarns() {
        assertTrue(session.errorCount() == 0);
        assertFalse(session.hasErrors());
        assertTrue(session.warnCount() == 0);
        assertFalse(session.hasWarns());

        Audit warn = new Audit();
        warn.setStatus(AuditStatus.WARN);
        session.getAudits().getAudit().add(warn);

        assertTrue(session.errorCount() == 0);
        assertFalse(session.hasErrors());
        assertTrue(session.warnCount() == 1);
        assertTrue(session.hasWarns());
    }

}
