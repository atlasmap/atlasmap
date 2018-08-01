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

import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.MappingDocument;
import io.atlasmap.api.v3.MappingDocument.DataDocumentRole;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.api.v3.Transformation.Descriptor;
import io.atlasmap.core.v3.transformation.AddTransformation;
import io.atlasmap.core.v3.transformation.MapTransformation;
import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public class EndToEndTest {

    TestClass sourceClass;
    TestClass targetClass;
    File mappingFile;
    MappingDocument mappingDoc;

    @Before
    public void before() throws AtlasException {
        sourceClass = new TestClass();
        sourceClass.getList().add(new TestItem().setName("item1"));
        sourceClass.getList().add(new TestItem().setName("item2"));
        sourceClass.getList().add(new TestItem().setName("item3"));
        targetClass = new TestClass();
        mappingFile = new File("target/" + getClass().getSimpleName() + ".json");
        mappingFile.delete();
        Atlas atlas = new Atlas(mappingFile);
        mappingDoc = atlas.mappingDocument();
        mappingDoc.addDataDocument("SourceClass", DataDocumentRole.SOURCE, "java", sourceClass);
        mappingDoc.addDataDocument("TargetClass", DataDocumentRole.TARGET, "java", targetClass);
    }

    @Test
    public void test() throws AtlasException {
        Mapping mapping = mappingDoc.addMapping("/SourceClass/string", "/TargetClass/string");
        assertThat(mappingDoc.mappings().isEmpty(), is(false));
        assertThat(targetClass.getString(), is("string"));
        assertThat(mapping.transformations().isEmpty(), is(false));
        assertThat(mapping.messages().isEmpty(), is(true));
        Transformation mapTransformation = mapping.transformations().get(0);
        assertThat(mapTransformation.name(), is(MapTransformation.NAME));
        Transformation addTransformation = addTransformation(mapping, AddTransformation.class);
        assertThat(mapping.outputPropertyNames().isEmpty(), is(false));
        assertThat(mapping.outputPropertyNames().contains(":1"), is(true));
        assertThat(mapping.messages().isEmpty(), is(false));
        mapTransformation.parameter(MapTransformation.FROM_PARAMETER).setStringValue(":1");
        assertThat(mapping.messages().isEmpty(), is(true));
        addTransformation.parameters().get(0).setStringValue("/SourceClass/integerPrimitive");
        assertThat(mapping.messages().isEmpty(), is(true));
        addTransformation.parameters().get(1).setStringValue("/SourceClass/integerPrimitive");
        assertThat(mapping.messages().isEmpty(), is(true));
        assertThat(targetClass.getString(), is("4294967294"));
        assertThat(mapping.messages().isEmpty(), is(true));

        mapping = mappingDoc.addMapping();
        addTransformation = addTransformation(mapping, AddTransformation.class);
        addTransformation.parameters().get(0).setStringValue("/SourceClass/integerPrimitive");
        addTransformation.parameters().get(1).setStringValue("/SourceClass/integerPrimitive");
        addTransformation.parameters().get(2).setStringValue("/TargetClass/doublePrimitive");
        assertThat(targetClass.getDoublePrimitive(), is(4.294967294E9));
        addTransformation.parameters().get(1).setStringValue("/SourceClass/doublePrimitive");
        addTransformation.parameters().get(2).setStringValue("/TargetClass/integerPrimitive");
        assertThat(targetClass.integerPrimitive(), is(2147483647));

        targetClass = new TestClass();
        Atlas atlas = new Atlas(mappingFile);
        mappingDoc = atlas.mappingDocument();
        assertThat(mappingDoc.mappings().isEmpty(), is(false));
        mappingDoc.addDataDocument("SourceClass", DataDocumentRole.SOURCE, "java", sourceClass);
        mappingDoc.addDataDocument("TargetClass", DataDocumentRole.TARGET, "java", targetClass);
        assertThat(targetClass.getString(), is("4294967294"));
}

    private Transformation addTransformation(Mapping mapping, Class<? extends Transformation> transformationClass) {
        Descriptor newDescriptor = null;
        for (Descriptor descriptor : mappingDoc.availableTransformationDescriptors()) {
            if (((TransformationDescriptorImpl)descriptor).transformationClass == transformationClass) {
                newDescriptor = descriptor;
                break;
            }
        }
        return mapping.addTransformation(newDescriptor);
    }
}
