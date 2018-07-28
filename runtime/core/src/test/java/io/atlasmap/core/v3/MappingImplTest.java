/**
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.atlasmap.core.v3;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.v3.MappingDocument.DataDocumentRole;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.core.transformation.MapTransformation;
import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public class MappingImplTest {

    MappingImpl mapping;

    @Before
    public void before() throws AtlasException {
        File mappingFile = new File("target/" + getClass().getSimpleName() + ".json");
        mappingFile.delete();
        Context context = new Context(mappingFile);
        context.loadDataHandlers(Context.DATA_HANDLER_META_FILE_PATH + ".good");
        context.loadTransformations(Context.TRANSFORMATIONS_META_FILE_PATH + ".good");
        MappingDocumentImpl doc = context.mappingDocument;
        doc.addDataDocument("id", DataDocumentRole.SOURCE, "test", "test");
        mapping = (MappingImpl)doc.addMapping("test", "/id/test");
    }

    @Test
    public void testName() {
        assertThat(mapping.name(), is(MappingImpl.NAME));
    }

    @Test
    public void testSetName() {
        mapping.setName(" test ");
        assertThat(mapping.name(), is("test"));
        mapping.setName(null);
        assertThat(mapping.name(), is(MappingImpl.NAME));
        mapping.setName("");
        assertThat(mapping.name(), is(MappingImpl.NAME));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTransformationsUnmodifiable() {
        List<Transformation> transformations = mapping.transformations();
        transformations.add(new MapTransformation());
    }
}
