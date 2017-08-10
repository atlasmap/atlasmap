package io.atlasmap.reference.javaToJava;

import org.junit.Test;

import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.reference.AtlasBaseActionsTest;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.StringLength;

public class JavaJavaFieldActionsTest extends AtlasBaseActionsTest {

    public JavaJavaFieldActionsTest() {
        this.inputField = createField("/boxedStringField");
        this.outputField = createField("/boxedStringField");
        this.docURI = "atlas:java?className=io.atlasmap.java.test.TargetFlatPrimitiveClass";
    }

    protected Field createField(String path) {
        JavaField f = new JavaField();
        f.setPath(path);
        f.setFieldType(FieldType.STRING);
        return f;
    }

    @Override
    public void runStringLengthTest() throws Exception {
        this.outputField = createField("/boxedIntField");
        this.runActionTest(new StringLength(), "fname", new Integer(5));
        this.outputField = createField("/boxedStringField");
    }

    @Test
    public void runNoConversionTest() throws Exception {
        this.outputField = createField("/boxedIntField");
        this.runActionTestList(null, "fname", null);
        this.outputField = createField("/boxedStringField");
    }

    @Override
    public Object createInput(String inputFirstName) {
        TargetFlatPrimitiveClass c = new TargetFlatPrimitiveClass();
        c.setBoxedStringField(inputFirstName);
        return c;
    }

    public Object getOutputValue(Object output) {
        System.out.println("Extracting output value from: " + output);
        TargetFlatPrimitiveClass c = (TargetFlatPrimitiveClass) output;
        Object result = c.getBoxedStringField();
        if (this.outputField.getPath().equals("/boxedIntField")) {
            result = c.getBoxedIntField();
        }
        System.out.println("Output value extracted: " + result);
        return result;
    }
}
