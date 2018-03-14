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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasFieldWriter;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.Validations;

public class DefaultAtlasSessionTest {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasSessionTest.class);
    private DefaultAtlasSession session = null;

    @Before
    public void setUp() {
        session = new DefaultAtlasSession(AtlasTestData.generateAtlasMapping());
    }

    @After
    public void tearDown() {
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
        assertEquals("defaultInput", session.getDefaultSourceDocument());
    }

    @Test
    public void testGetSetInputDocId() {
        session.setDefaultSourceDocument(new String("defaultInput"));
        assertNotNull(session.getDefaultSourceDocument());
        assertTrue(session.getDefaultSourceDocument() instanceof String);
        assertEquals("defaultInput", session.getDefaultSourceDocument());
        assertNotNull(session.getSourceDocument(null));
        assertNotNull(session.getSourceDocument(""));
        assertNotNull(session.getSourceDocument("docId"));

        session.setSourceDocument("second", new String("secondInput"));
        assertNotNull(session.getDefaultSourceDocument());
        assertTrue(session.getDefaultSourceDocument() instanceof String);
        assertEquals("defaultInput", session.getDefaultSourceDocument());

        assertTrue(session.hasSourceDocument("second"));
        assertFalse(session.hasSourceDocument("third"));
        assertTrue(session.hasSourceDocument(null));
        assertTrue(session.hasSourceDocument(""));
        assertNotNull(session.getSourceDocument("second"));
        assertTrue(session.getSourceDocument("second") instanceof String);
        assertEquals("secondInput", session.getSourceDocument("second"));
        assertNull(session.getSourceDocument("docId"));
        assertNotNull(session.getSourceDocumentMap());
    }

    @Test
    public void testGetSetOutput() {
        session.setDefaultTargetDocument(new String("defaultOutput"));
        assertNotNull(session.getDefaultTargetDocument());
        assertTrue(session.getDefaultTargetDocument() instanceof String);
        assertEquals("defaultOutput", session.getDefaultTargetDocument());
    }

    @Test
    public void testGetSetOutputDocId() {
        session.setDefaultTargetDocument(new String("defaultOutput"));
        assertNotNull(session.getDefaultTargetDocument());
        assertTrue(session.getDefaultTargetDocument() instanceof String);
        assertEquals("defaultOutput", session.getDefaultTargetDocument());
        assertNotNull(session.getTargetDocument(null));
        assertNotNull(session.getTargetDocument(""));
        assertNotNull(session.getTargetDocument("second"));

        session.setTargetDocument("second", new String("secondOutput"));
        assertNotNull(session.getDefaultTargetDocument());
        assertTrue(session.getDefaultTargetDocument() instanceof String);
        assertEquals("defaultOutput", session.getDefaultTargetDocument());

        assertTrue(session.hasTargetDocument("second"));
        assertFalse(session.hasTargetDocument("third"));
        assertNotNull(session.getTargetDocument("second"));
        assertTrue(session.getTargetDocument("second") instanceof String);
        assertEquals("secondOutput", session.getTargetDocument("second"));
        assertNull(session.getTargetDocument("docId"));
        assertNotNull(session.getTargetDocumentMap());
    }

    @Test
    public void testGetProperties() {
        assertNotNull(session);
        assertNotNull(session.getProperties());
        assertTrue(session.getProperties().size() == 0);

        session.getProperties().put("foo", "bar");
        assertTrue(session.getProperties().size() == 1);
        assertEquals("bar", session.getProperties().get("foo"));
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

    @Test
    public void testSetTargetDocument() {
        session.setTargetDocument("target", "defaultOutput");
        session.setTargetDocument(null, "defaultOutput");
        session.setTargetDocument("", "defaultOutput");
        session.hasTargetDocument(null);
        session.hasTargetDocument("");
    }

    @Test
    public void testSetSourceDocument() {
        session.setSourceDocument("source", "defaultInput");
        session.setSourceDocument(null, "defaultInput");
        session.setSourceDocument("", "defaultInput");
    }

    @Test
    public void testGetFieldReader() {
        AtlasFieldReader reader = new AtlasFieldReader() {
            @Override
            public void read(AtlasInternalSession session) throws AtlasException {
                LOG.debug("read method");
            }
        };
        session.setFieldReader(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, reader);
        assertNotNull(session.getFieldReader(null));
        assertNotNull(session.getFieldReader(""));
        assertNotNull(session.getFieldReader(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID));
        assertNotNull(session.getFieldReader("", AtlasFieldReader.class));
    }

    @Test
    public void testSetFieldReader() {
        AtlasFieldReader reader = new AtlasFieldReader() {
            @Override
            public void read(AtlasInternalSession session) throws AtlasException {
                LOG.debug("read method");
            }
        };
        session.setFieldReader(null, reader);
        session.setFieldReader("", reader);
    }

    @Test
    public void testRemoveFieldReader() {
        session.removeFieldReader(null);
        session.removeFieldReader("");
        session.removeFieldReader(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
    }

    @Test
    public void testGetFieldWriter() {
        AtlasFieldWriter writer = new AtlasFieldWriter() {
            @Override
            public void write(AtlasInternalSession session) throws AtlasException {
                LOG.debug("write method");
            }
        };
        session.setFieldWriter(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID, writer);
        assertNotNull(session.getFieldWriter(null));
        assertNotNull(session.getFieldWriter(""));
        assertNotNull(session.getFieldWriter(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID));
        assertNotNull(session.getFieldWriter("", AtlasFieldWriter.class));
    }

    @Test
    public void testSetFieldWriter() {
        AtlasFieldWriter writer = new AtlasFieldWriter() {
            @Override
            public void write(AtlasInternalSession session) throws AtlasException {
                LOG.debug("write method");
            }
        };
        session.setFieldWriter(null, writer);
        session.setFieldWriter("", writer);
    }

    @Test
    public void testRemoveFieldWriter() {
        session.removeFieldWriter(null);
        session.removeFieldWriter("");
        session.removeFieldWriter(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID);
    }

    @Test
    public void testHead() {
        Head head = session.head();
        assertNotNull(head);
        assertNull(head.getLookupTable());
        assertNull(head.getMapping());
        assertNull(head.getSourceField());
        assertNull(head.getTargetField());
        head.setLookupTable(null);
        head.setMapping(null);
        head.setSourceField(null);
        head.setTargetField(null);
        assertNotNull(head.unset());

    }
}
