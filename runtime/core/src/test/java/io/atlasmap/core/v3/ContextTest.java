package io.atlasmap.core.v3;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.v3.MappingDocument;
import io.atlasmap.spi.v3.BaseTransformation;
import io.atlasmap.spi.v3.DataHandler;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;

public class ContextTest {

    private static final String DATA_HANDLERS_PREFIX = Context.DATA_HANDLER_META_FILE_PATH + '.';
    private static final String TRANSFORMATIONS_PREFIX = Context.TRANSFORMATIONS_META_FILE_PATH + '.';

    @Test
    public void testMappingFile() throws AtlasException {
        assertThat(context().mappingFile, is(new File("test.json")));
    }

    @Test
    public void testMappingDocument() throws AtlasException {
        assertThat(context().mappingDocument, instanceOf(MappingDocument.class));
    }

    @Test
    public void testLoadHandlerWithNonClassInMetaFile() throws AtlasException {
        try {
            context().loadDataHandlers(DATA_HANDLERS_PREFIX + "nonClass");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), startsWith("Unable to load class "));
            assertThat(e.getCause(), notNullValue());
        }
    }

    @Test
    public void testLoadHandlerWithNonHandlerClassInMetaFile() throws AtlasException {
        try {
            context().loadDataHandlers(DATA_HANDLERS_PREFIX + "nonHandler");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), endsWith("does not subclass " + DataHandler.class));
        }
    }

    @Test
    public void testLoadHandlerWithBadConstructor() throws AtlasException {
        try {
            context().loadDataHandlers(DATA_HANDLERS_PREFIX + "badConstructor");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), startsWith("Unable to create data handler"));
        }
    }

    @Test
    public void testLoadHandlerWithNoDataFormats() throws AtlasException {
        try {
            context().loadDataHandlers(DATA_HANDLERS_PREFIX + "noDataFormats");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), endsWith("supports no data formats"));
        }
    }

    @Test
    public void testLoadTransformationsWithNonTransformationClassInMetaFile() throws AtlasException {
        try {
            context().loadTransformations(TRANSFORMATIONS_PREFIX + "nonTransformation");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), endsWith("does not subclass " + BaseTransformation.class));
        }
    }

    @Test
    public void testLoadTransformationsWithBadConstructor() throws AtlasException {
        try {
            context().loadTransformations(TRANSFORMATIONS_PREFIX + "badConstructor");
            fail();
        } catch (AtlasRuntimeException e) {
            assertThat(e.getMessage(), startsWith("Unable to create transformation"));
        }
    }

    private Context context() throws AtlasException {
        return new Context(new File("test.json"));
    }
}
