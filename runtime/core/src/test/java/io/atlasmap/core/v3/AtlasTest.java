package io.atlasmap.core.v3;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.v3.MappingDocument;
import io.atlasmap.spi.v3.util.AtlasException;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;

public class AtlasTest {

    @Test(expected = AtlasRuntimeException.class)
    public void testConstructWithNullFile() throws AtlasException {
        new Atlas(null);
    }

    @Test
    public void testConstructWithNewMappingFile() throws AtlasException {
        atlas();
    }

    @Test
    public void testMappingDocument() throws AtlasException {
        assertThat(atlas().mappingDocument(), instanceOf(MappingDocument.class));
    }

    private Atlas atlas() throws AtlasException {
        return new Atlas(new File("test.json"));
    }
}
