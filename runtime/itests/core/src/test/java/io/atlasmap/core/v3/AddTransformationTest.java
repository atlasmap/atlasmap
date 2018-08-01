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
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.MappingDocument;
import io.atlasmap.api.v3.MappingDocument.DataDocumentRole;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.api.v3.Transformation.Descriptor;
import io.atlasmap.core.v3.transformation.AddTransformation;
import io.atlasmap.spi.v3.util.AtlasException;

/**
 *
 */
public class AddTransformationTest {

    private static final String SOURCE_ID = "SourceClass";
    private static final String TARGET_ID = "TargetClass";

    TestClass sourceClass;
    TestClass targetClass;
    File mappingFile;
    MappingDocument mappingDoc;
    Transformation addTransformation;

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
        mappingDoc.addDataDocument(SOURCE_ID, DataDocumentRole.SOURCE, "java", sourceClass);
        mappingDoc.addDataDocument(TARGET_ID, DataDocumentRole.TARGET, "java", targetClass);
        addTransformation = addTransformation(mappingDoc.addMapping(), AddTransformation.class);
    }

    @Test
    public void test() throws AtlasException {
        assertThat(mappingDoc.messages().size(), is(2));
        assertThat(mappingDoc.hasWarnings(), is(false));
        assertThat(mappingDoc.hasErrors(), is(true));
        assertThat(mappingDoc.messages().size(), is(2));

        verify(2, "booleanWrapper", () -> targetClass.booleanWrapper, false, 1);
        verify(0, "nullBooleanWrapper", () -> targetClass.booleanWrapper, false, 1);
        verify(1, "nullBooleanWrapper", () -> targetClass.booleanWrapper, false, 1);

        verify(0, "booleanWrapper", () -> targetClass.booleanWrapper, true, 2);
        verify(0, "byteWrapper", () -> targetClass.booleanWrapper, true, 1);
        verify(0, "shortWrapper", () -> targetClass.booleanWrapper, true, 1);
        verify(0, "integerWrapper", () -> targetClass.booleanWrapper, true, 1);
        verify(0, "longWrapper", () -> targetClass.booleanWrapper, true, 1);
        verify(0, "floatWrapper", () -> targetClass.booleanWrapper, true, 1);
        verify(0, "doubleWrapper", () -> targetClass.booleanWrapper, true, 1);
        verify(0, "characterWrapper", () -> targetClass.booleanWrapper, true, 2);
        verify(0, "string", () -> targetClass.booleanWrapper, false, 2);
        verify(0, "date", () -> targetClass.booleanWrapper, false, 2);
        verify(0, "numberString", () -> targetClass.booleanWrapper, true, 2);

        verify(1, "booleanPrimitive", () -> targetClass.booleanWrapper, true, 3);
        verify(1, "bytePrimitive", () -> targetClass.booleanWrapper, true, 2);
        verify(1, "shortPrimitive", () -> targetClass.booleanWrapper, true, 2);
        verify(1, "integerPrimitive", () -> targetClass.booleanWrapper, true, 2);
        verify(1, "longPrimitive", () -> targetClass.booleanWrapper, true, 2);
        verify(1, "floatPrimitive", () -> targetClass.booleanWrapper, true, 2);
        verify(1, "doublePrimitive", () -> targetClass.booleanWrapper, true, 2);
        verify(1, "characterPrimitive", () -> targetClass.booleanWrapper, true, 3);
        verify(1, "string", () -> targetClass.booleanWrapper, true, 3);

        verify(2, "byteWrapper", () -> targetClass.byteWrapper, (byte)16, 2);

        verify(0, "booleanWrapper", () -> targetClass.byteWrapper, (byte)1, 2);
        verify(0, "byteWrapper", () -> targetClass.byteWrapper, (byte)1, 1);
        verify(0, "shortWrapper", () -> targetClass.byteWrapper, (byte)2, 1);
        verify(0, "integerWrapper", () -> targetClass.byteWrapper, (byte)3, 1);
        verify(0, "longWrapper", () -> targetClass.byteWrapper, (byte)4, 1);
        verify(0, "floatWrapper", () -> targetClass.byteWrapper, (byte)5, 2);
        verify(0, "doubleWrapper", () -> targetClass.byteWrapper, (byte)6, 2);
        verify(0, "characterWrapper", () -> targetClass.byteWrapper, (byte)97, 2);
        verify(0, "string", () -> targetClass.byteWrapper, (byte)0, 2);
        verify(0, "date", () -> targetClass.byteWrapper, (byte)0, 2);
        verify(0, "numberString", () -> targetClass.byteWrapper, (byte)16, 2);

        verify(1, "booleanPrimitive", () -> targetClass.byteWrapper, (byte)17, 2);
        verify(1, "bytePrimitive", () -> targetClass.byteWrapper, (byte)17, 1);
        verify(1, "shortPrimitive", () -> targetClass.byteWrapper, (byte)15, 2);
        verify(1, "integerPrimitive", () -> targetClass.byteWrapper, (byte)15, 2);
        verify(1, "longPrimitive", () -> targetClass.byteWrapper, (byte)-1, 2);
        verify(1, "floatPrimitive", () -> targetClass.byteWrapper, (byte)-1, 2);
        verify(1, "doublePrimitive", () -> targetClass.byteWrapper, (byte)-1, 2);
        verify(1, "characterPrimitive", () -> targetClass.byteWrapper, (byte)15, 3);
        verify(1, "string", () -> targetClass.byteWrapper, (byte)16, 2);
        verify(1, "integerDouble", () -> targetClass.byteWrapper, (byte)23, 1);

        verify(2, "shortWrapper", () -> targetClass.shortWrapper, (short)23, 1);

        verify(0, "booleanWrapper", () -> targetClass.shortWrapper, (short)8, 1);
        verify(0, "byteWrapper", () -> targetClass.shortWrapper, (short)8, 0);
        verify(0, "shortWrapper", () -> targetClass.shortWrapper, (short)9, 0);
        verify(0, "integerWrapper", () -> targetClass.shortWrapper, (short)10, 0);
        verify(0, "longWrapper", () -> targetClass.shortWrapper, (short)11, 0);
        verify(0, "floatWrapper", () -> targetClass.shortWrapper, (short)12, 1);
        verify(0, "doubleWrapper", () -> targetClass.shortWrapper, (short)13, 1);
        verify(0, "characterWrapper", () -> targetClass.shortWrapper, (short)104, 1);
        verify(0, "string", () -> targetClass.shortWrapper, (short)7, 1);
        verify(0, "date", () -> targetClass.shortWrapper, (short)7, 1);
        verify(0, "numberString", () -> targetClass.shortWrapper, (short)23, 1);

        verify(1, "booleanPrimitive", () -> targetClass.shortWrapper, (short)17, 2);
        verify(1, "bytePrimitive", () -> targetClass.shortWrapper, (short)17, 1);
        verify(1, "shortPrimitive", () -> targetClass.shortWrapper, (short)-32753, 2);
        verify(1, "integerPrimitive", () -> targetClass.shortWrapper, (short)15, 2);
        verify(1, "longPrimitive", () -> targetClass.shortWrapper, (short)-1, 2);
        verify(1, "floatPrimitive", () -> targetClass.shortWrapper, (short)-1, 2);
        verify(1, "doublePrimitive", () -> targetClass.shortWrapper, (short)-1, 2);
        verify(1, "characterPrimitive", () -> targetClass.shortWrapper, (short)15, 3);
        verify(1, "string", () -> targetClass.shortWrapper, (short)16, 2);
        verify(1, "integerDouble", () -> targetClass.shortWrapper, (short)23, 1);

        verify(2, "integerWrapper", () -> targetClass.integerWrapper, 23, 1);

        verify(0, "booleanWrapper", () -> targetClass.integerWrapper, 8, 1);
        verify(0, "byteWrapper", () -> targetClass.integerWrapper, 8, 0);
        verify(0, "shortWrapper", () -> targetClass.integerWrapper, 9, 0);
        verify(0, "integerWrapper", () -> targetClass.integerWrapper, 10, 0);
        verify(0, "longWrapper", () -> targetClass.integerWrapper, 11, 0);
        verify(0, "floatWrapper", () -> targetClass.integerWrapper, 12, 1);
        verify(0, "doubleWrapper", () -> targetClass.integerWrapper, 13, 1);
        verify(0, "characterWrapper", () -> targetClass.integerWrapper, 104, 1);
        verify(0, "string", () -> targetClass.integerWrapper, 7, 1);
        verify(0, "date", () -> targetClass.integerWrapper, 7, 1);
        verify(0, "numberString", () -> targetClass.integerWrapper, 23, 1);

        verify(1, "booleanPrimitive", () -> targetClass.integerWrapper, 17, 2);
        verify(1, "bytePrimitive", () -> targetClass.integerWrapper, 17, 1);
        verify(1, "shortPrimitive", () -> targetClass.integerWrapper, 32783, 1);
        verify(1, "integerPrimitive", () -> targetClass.integerWrapper, -2147483633, 2);
        verify(1, "longPrimitive", () -> targetClass.integerWrapper, -1, 2);
        verify(1, "floatPrimitive", () -> targetClass.integerWrapper, 2147483647, 2);
        verify(1, "doublePrimitive", () -> targetClass.integerWrapper, 2147483647, 2);
        verify(1, "characterPrimitive", () -> targetClass.integerWrapper, 65551, 2);
        verify(1, "string", () -> targetClass.integerWrapper, 16, 2);
        verify(1, "integerDouble", () -> targetClass.integerWrapper, 23, 1);

        verify(2, "longWrapper", () -> targetClass.longWrapper, 23L, 1);

        verify(0, "booleanWrapper", () -> targetClass.longWrapper, 8L, 1);
        verify(0, "byteWrapper", () -> targetClass.longWrapper, 8L, 0);
        verify(0, "shortWrapper", () -> targetClass.longWrapper, 9L, 0);
        verify(0, "integerWrapper", () -> targetClass.longWrapper, 10L, 0);
        verify(0, "longWrapper", () -> targetClass.longWrapper, 11L, 0);
        verify(0, "floatWrapper", () -> targetClass.longWrapper, 12L, 1);
        verify(0, "doubleWrapper", () -> targetClass.longWrapper, 13L, 1);
        verify(0, "characterWrapper", () -> targetClass.longWrapper, 104L, 1);
        verify(0, "string", () -> targetClass.longWrapper, 7L, 1);
        verify(0, "date", () -> targetClass.longWrapper, 7L, 1);
        verify(0, "numberString", () -> targetClass.longWrapper, 23L, 1);

        verify(1, "booleanPrimitive", () -> targetClass.longWrapper, 17L, 2);
        verify(1, "bytePrimitive", () -> targetClass.longWrapper, 17L, 1);
        verify(1, "shortPrimitive", () -> targetClass.longWrapper, 32783L, 1);
        verify(1, "integerPrimitive", () -> targetClass.longWrapper, 2147483663L, 1);
        verify(1, "longPrimitive", () -> targetClass.longWrapper, 9223372036854775807L, 1);
        verify(1, "floatPrimitive", () -> targetClass.longWrapper, 9223372036854775807L, 2);
        verify(1, "doublePrimitive", () -> targetClass.longWrapper, 9223372036854775807L, 2);
        verify(1, "characterPrimitive", () -> targetClass.longWrapper, 65551L, 2);
        verify(1, "string", () -> targetClass.longWrapper, 16L, 2);
        verify(1, "integerDouble", () -> targetClass.longWrapper, 23L, 1);

        verify(2, "floatWrapper", () -> targetClass.floatWrapper, 23.0F, 1);

        verify(0, "booleanWrapper", () -> targetClass.floatWrapper, 8.0F, 1);
        verify(0, "byteWrapper", () -> targetClass.floatWrapper, 8.0F, 0);
        verify(0, "shortWrapper", () -> targetClass.floatWrapper, 9.0F, 0);
        verify(0, "integerWrapper", () -> targetClass.floatWrapper, 10.0F, 0);
        verify(0, "longWrapper", () -> targetClass.floatWrapper, 11.0F, 0);
        verify(0, "floatWrapper", () -> targetClass.floatWrapper, 12.5F, 0);
        verify(0, "doubleWrapper", () -> targetClass.floatWrapper, 13.9F, 1); // Not sure why an auto-convert msg gets created here?
        verify(0, "characterWrapper", () -> targetClass.floatWrapper, 104.0F, 1);
        verify(0, "string", () -> targetClass.floatWrapper, 7.0F, 1);
        verify(0, "date", () -> targetClass.floatWrapper, 7.0F, 1);
        verify(0, "numberString", () -> targetClass.floatWrapper, 23.0F, 1);

        verify(1, "booleanPrimitive", () -> targetClass.floatWrapper, 17.0F, 2);
        verify(1, "bytePrimitive", () -> targetClass.floatWrapper, 17.0F, 1);
        verify(1, "shortPrimitive", () -> targetClass.floatWrapper, 32783.0F, 1);
        verify(1, "integerPrimitive", () -> targetClass.floatWrapper, 2147483663.0F, 2);
        verify(1, "longPrimitive", () -> targetClass.floatWrapper, 9223372036854775807.0F, 1);
        verify(1, "floatPrimitive", () -> targetClass.floatWrapper, 3.4028235E38F, 1);
        verify(1, "doublePrimitive", () -> targetClass.floatWrapper, Float.POSITIVE_INFINITY, 2);
        verify(1, "characterPrimitive", () -> targetClass.floatWrapper, 65551.0F, 2);
        verify(1, "string", () -> targetClass.floatWrapper, 16.0F, 2);
        verify(1, "integerDouble", () -> targetClass.floatWrapper, 23.0F, 1);

        verify(2, "doubleWrapper", () -> targetClass.doubleWrapper, 23.0, 1);

        verify(0, "booleanWrapper", () -> targetClass.doubleWrapper, 8.0, 1);
        verify(0, "byteWrapper", () -> targetClass.doubleWrapper, 8.0, 0);
        verify(0, "shortWrapper", () -> targetClass.doubleWrapper, 9.0, 0);
        verify(0, "integerWrapper", () -> targetClass.doubleWrapper, 10.0, 0);
        verify(0, "longWrapper", () -> targetClass.doubleWrapper, 11.0, 0);
        verify(0, "floatWrapper", () -> targetClass.doubleWrapper, 12.5, 0);
        verify(0, "doubleWrapper", () -> targetClass.doubleWrapper, 13.9, 0);
        verify(0, "characterWrapper", () -> targetClass.doubleWrapper, 104.0, 1);
        verify(0, "string", () -> targetClass.doubleWrapper, 7.0, 1);
        verify(0, "date", () -> targetClass.doubleWrapper, 7.0, 1);
        verify(0, "numberString", () -> targetClass.doubleWrapper, 23.0, 1);

        verify(1, "booleanPrimitive", () -> targetClass.doubleWrapper, 17.0, 2);
        verify(1, "bytePrimitive", () -> targetClass.doubleWrapper, 17.0, 1);
        verify(1, "shortPrimitive", () -> targetClass.doubleWrapper, 32783.0, 1);
        verify(1, "integerPrimitive", () -> targetClass.doubleWrapper, 2147483663.0, 1);
        verify(1, "longPrimitive", () -> targetClass.doubleWrapper, 9223372036854775807.0, 1);
        verify(1, "floatPrimitive", () -> targetClass.doubleWrapper, 3.4028234663852886E38, 1);
        verify(1, "doublePrimitive", () -> targetClass.doubleWrapper, 1.7976931348623157E308, 1);
        verify(1, "characterPrimitive", () -> targetClass.doubleWrapper, 65551.0, 2);
        verify(1, "string", () -> targetClass.doubleWrapper, 16.0, 2);
        verify(1, "integerDouble", () -> targetClass.doubleWrapper, 23.0, 1);
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

    private void verify(int parameterIndex, String path, Supplier<?> actualValueSupplier, Object expectedValue, int messageCount) throws AtlasException {
        addTransformation.parameters().get(parameterIndex).setStringValue('/' + (parameterIndex == 2 ? TARGET_ID : SOURCE_ID) + '/' + path);
        verify(actualValueSupplier.get(), expectedValue, messageCount);
    }

    private void verify(Object actualValue, Object expectedValue, int messageCount) {
        System.out.println(mappingDoc.messages());
        System.out.println(actualValue);
        assertThat(mappingDoc.hasErrors(), is(false));
        assertThat(actualValue, is(expectedValue));
        assertThat(mappingDoc.messages().size(), is(messageCount));
    }
}
