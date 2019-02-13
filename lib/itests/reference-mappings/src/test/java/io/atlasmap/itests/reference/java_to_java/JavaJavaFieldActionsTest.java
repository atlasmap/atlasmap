/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.atlasmap.itests.reference.java_to_java;

import org.junit.Test;

import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.itests.reference.AtlasBaseActionsTest;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Length;

public class JavaJavaFieldActionsTest extends AtlasBaseActionsTest {

    public JavaJavaFieldActionsTest() {
        this.sourceField = createField("/boxedStringField");
        this.targetField = createField("/boxedStringField");
        this.docURI = "atlas:java?className=io.atlasmap.java.test.TargetFlatPrimitiveClass";
    }

    protected Field createField(String path) {
        JavaField f = new JavaField();
        f.setPath(path);
        f.setFieldType(FieldType.STRING);
        return f;
    }

    @Override
    public void runLengthTest() throws Exception {
        this.runActionTest(new Length(), "fname", "5", String.class);
    }

    @Test
    public void runNoConversionTest() throws Exception {
        this.runActionTestList(null, "fname", null, String.class);
    }

    @Override
    public Object createSource(String sourceFirstName) {
        TargetFlatPrimitiveClass c = new TargetFlatPrimitiveClass();
        c.setBoxedStringField(sourceFirstName);
        return c;
    }

    @Override
    public Object getTargetValue(Object target, Class<?> targetClassExpected) {
        System.out.println("Extracting target value from: " + target);
        TargetFlatPrimitiveClass c = (TargetFlatPrimitiveClass) target;
        Object result = c.getBoxedStringField();
        if (this.targetField.getPath().equals("/boxedIntField")) {
            result = c.getBoxedIntField();
        }
        System.out.println("Output value extracted: " + result);
        return result;
    }
}
