package io.atlasmap.core.v3;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.MappingDocument.DataDocumentRole;
import io.atlasmap.core.transformation.MapTransformation;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;

public class MappingDocumentImplTest {

    Context context;
    MappingDocumentImpl doc;

    @Before
    public void before() throws AtlasException {
        File mappingFile = new File("target/" + getClass().getSimpleName() + ".json");
        mappingFile.delete();
        context = new Context(mappingFile);
        context.loadDataHandlers(Context.DATA_HANDLER_META_FILE_PATH + ".good");
        context.loadTransformations(Context.TRANSFORMATIONS_META_FILE_PATH + ".good");
        doc = context.mappingDocument;
    }

    @Test
    public void testAvailableFormats() {
        String[] formats = doc.availableDataFormats(DataDocumentRole.SOURCE);
        assertThat(formats, notNullValue());
        assertThat(formats.length, is(1));
    }

    @Test
    public void testAddDataDocumentWithEmptyId() throws AtlasException {
        try {
            doc.addDataDocument("", DataDocumentRole.SOURCE, "test", "test");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), startsWith("The 'id' argument must not be empty"));
        }
    }

    @Test
    public void testAddDataDocumentWithNullType() throws AtlasException {
        try {
            doc.addDataDocument("id", null, "test", "test");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), startsWith("The 'role' argument must not be null"));
        }
    }

    @Test
    public void testAddDataDocumentWithEmptyDataFormat() throws AtlasException {
        try {
            doc.addDataDocument("id", DataDocumentRole.SOURCE, "", "test");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), startsWith("The 'dataFormat' argument must not be empty"));
        }
    }

    @Test
    public void testAddDataDocumentWithNullDocument() throws AtlasException {
        try {
            doc.addDataDocument("id", DataDocumentRole.SOURCE, "test", null);
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), startsWith("The 'document' argument must not be null"));
        }
    }

    @Test
    public void testAddDataDocumentWithUnsupportedDataFormat() throws AtlasException {
        try {
            doc.addDataDocument("id", DataDocumentRole.SOURCE, "java", "test");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), startsWith("The java data format is not supported for a source document"));
        }
    }

    @Test
    public void testAddDataDocumentWithUnsupportedDocumentType() throws AtlasException {
        try {
            doc.addDataDocument("id", DataDocumentRole.TARGET, "test", "test");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), startsWith("The test data format is not supported for a target document"));
        }
    }

    @Test
    public void testAddDataDocumentUnsupportedDocument() throws AtlasException {
        try {
            doc.addDataDocument("id", DataDocumentRole.SOURCE, "test", "");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), is("Must be 'test'"));
        }
    }

    @Test
    public void testAddDataDocument() throws AtlasException {
        doc.addDataDocument("id", DataDocumentRole.SOURCE, "test", "test");
        assertThat(context.dataDocumentDescriptors, not(empty()));
    }

    @Test(expected = AtlasRuntimeException.class)
    public void testRemoveDataDocumentWithNullId() {
        doc.removeDataDocument(null, DataDocumentRole.SOURCE);
    }

    @Test(expected = AtlasRuntimeException.class)
    public void testRemoveDataDocumentWithNullRole() {
        doc.removeDataDocument("id", null);
    }

    @Test(expected = AtlasRuntimeException.class)
    public void testRemoveDataDocumentNotFound() {
        doc.removeDataDocument("id", DataDocumentRole.SOURCE);
    }

    @Test
    public void testRemoveDataDocument() throws AtlasException {
        doc.addDataDocument("id", DataDocumentRole.SOURCE, "test", "test");
        assertThat(context.dataDocumentDescriptors, not(empty()));
        doc.removeDataDocument("id", DataDocumentRole.SOURCE);
        assertThat(context.dataDocumentDescriptors, empty());
    }

    @Test
    public void testAddMapping() throws AtlasException {
        doc.addDataDocument("id", DataDocumentRole.SOURCE, "test", "test");
        Mapping mapping = doc.addMapping("test", "/id/test");
        assertThat(doc.mappings(), not(empty()));
        assertThat(mapping, notNullValue());
        assertThat(mapping.transformations().size(), is(1));
        assertThat(mapping.transformations().get(0), instanceOf(MapTransformation.class));
    }

    @Test(expected = AtlasRuntimeException.class)
    public void testRemoveNullMapping() {
        doc.removeMapping(null);
    }

    @Test
    public void testRemoveMapping() throws AtlasException {
        doc.addDataDocument("id", DataDocumentRole.SOURCE, "test", "test");
        Mapping mapping = doc.addMapping("test", "/id/test");
        assertThat(doc.mappings(), not(empty()));
        doc.removeMapping(mapping);
        assertThat(doc.mappings(), empty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMappingsUnmodifiable() {
        doc.mappings().add(new MappingImpl(context));
    }
}
