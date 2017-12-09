package io.atlasmap.reference.java_to_java;

import org.junit.Test;

import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.reference.AtlasBaseActionsTest;
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
        this.targetField = createField("/boxedIntField");
        this.runActionTest(new Length(), "fname", new Integer(5), Integer.class);
        this.targetField = createField("/boxedStringField");
    }

    @Test
    public void runNoConversionTest() throws Exception {
        this.targetField = createField("/boxedIntField");
        this.runActionTestList(null, "fname", null, String.class);
        this.targetField = createField("/boxedStringField");
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
