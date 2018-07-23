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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.v3.DocumentRole;
import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.MappingDocument;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.api.v3.TransformationDescriptor;
import io.atlasmap.core.transformation.AddTransformation;
import io.atlasmap.core.transformation.MapTransformation;
import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public class EndToEndTest {

    TestSourceClass sourceClass;
    TestTargetClass targetClass;
    File mappingFile;
    MappingDocument mappingDoc;

    @Before
    public void before() throws AtlasException {
        sourceClass = new TestSourceClass().setSourceName("Source").setSourceInteger(1).setSourceDouble(2.5);
        sourceClass.setSourceFirstName("First").setSourceName("Name");
        sourceClass.getSourceList().add(new TestItem().setName("item1"));
        sourceClass.getSourceList().add(new TestItem().setName("item2"));
        sourceClass.getSourceList().add(new TestItem().setName("item3"));
        targetClass = new TestTargetClass();
        mappingFile = new File("target/" + getClass().getSimpleName() + ".json");
        mappingFile.delete();
        Atlas atlas = new Atlas(mappingFile);
        mappingDoc = atlas.mappingDocument();
        mappingDoc.addDataDocument("SourceClass", DocumentRole.SOURCE, "java", sourceClass);
        mappingDoc.addDataDocument("TargetClass", DocumentRole.TARGET, "java", targetClass);
    }

    @Test
    public void test() throws AtlasException {
        Mapping mapping = mappingDoc.addMapping("/SourceClass/sourceName", "/TargetClass/targetName");
        assertThat(mappingDoc.mappings().isEmpty(), is(false));
        assertThat(targetClass.getTargetName(), is("Name"));
        assertThat(mapping.transformations().isEmpty(), is(false));
        Transformation mapTransformation = mapping.transformations().get(0);
        assertThat(mapTransformation.name(), is("Map"));
        Transformation addTransformation = addTransformation(mapping, AddTransformation.class);
        assertThat(mapping.properties().isEmpty(), is(false));
        assertThat(mapping.properties().contains(":1"), is(true));
        mapTransformation.parameter(MapTransformation.FROM_PARAMETER).setStringValue(":1");
        addTransformation.parameters().get(0).setStringValue("/SourceClass/sourceInteger");
        addTransformation.parameters().get(1).setStringValue("/SourceClass/sourceInteger");
        assertThat(targetClass.getTargetName(), is("2"));

        mapping = mappingDoc.addMapping();
        addTransformation = addTransformation(mapping, AddTransformation.class);
        addTransformation.parameters().get(0).setStringValue("/SourceClass/sourceInteger");
        addTransformation.parameters().get(1).setStringValue("/SourceClass/sourceInteger");
        addTransformation.parameters().get(2).setStringValue("/TargetClass/targetDouble");
        assertThat(targetClass.getTargetDouble(), is(2.0));
        addTransformation.parameters().get(1).setStringValue("/SourceClass/sourceDouble");
        addTransformation.parameters().get(2).setStringValue("/TargetClass/targetInteger");
        assertThat(targetClass.getTargetInteger(), is(3));

        targetClass = new TestTargetClass();
        Atlas atlas = new Atlas(mappingFile);
        mappingDoc = atlas.mappingDocument();
        assertThat(mappingDoc.mappings().isEmpty(), is(false));
        mappingDoc.addDataDocument("SourceClass", DocumentRole.SOURCE, "java", sourceClass);
        mappingDoc.addDataDocument("TargetClass", DocumentRole.TARGET, "java", targetClass);
        assertThat(targetClass.getTargetName(), is("2"));
}

    private Transformation addTransformation(Mapping mapping, Class<? extends Transformation> transformationClass) {
        TransformationDescriptor newDescriptor = null;
        for (TransformationDescriptor descriptor : mappingDoc.availableTransformationDescriptors()) {
            if (((TransformationDescriptorImpl)descriptor).transformationClass == transformationClass) {
                newDescriptor = descriptor;
                break;
            }
        }
        return mapping.addTransformation(newDescriptor);
    }
}
