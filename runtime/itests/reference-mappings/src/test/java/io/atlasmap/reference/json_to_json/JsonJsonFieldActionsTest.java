package io.atlasmap.reference.json_to_json;

import io.atlasmap.json.v2.JsonField;
import io.atlasmap.reference.AtlasBaseActionsTest;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

public class JsonJsonFieldActionsTest extends AtlasBaseActionsTest {

    public JsonJsonFieldActionsTest() {
        this.inputField = createField("/contact/firstName");
        this.outputField = createField("/contact/firstName");
        this.docURI = "atlas:json";
    }

    protected Field createField(String path) {
        JsonField f = new JsonField();
        f.setPath(path);
        f.setFieldType(FieldType.STRING);
        return f;
    }

    @Override
    public Object createInput(String inputFirstName) {
        return "{ \"contact\": { \"firstName\": \"" + inputFirstName + "\" } }";
    }

    public Object getOutputValue(Object output, Class<?> outputClassExpected) {
        System.out.println("Extracting output value from: " + output);
        String result = (String) output;

        if(outputClassExpected != null && outputClassExpected.equals(Integer.class)) {
            result = result.substring("{\"contact\":{\"firstName\":".length());
            result = result.substring(0, result.length() - "}}".length());
            return Integer.valueOf(result);
        } else if(outputClassExpected != null && outputClassExpected.equals(Boolean.class)) {
            result = result.substring("{\"contact\":{\"firstName\":".length());
            result = result.substring(0, result.length() - "}}".length());
            return Boolean.valueOf(result);
        } else {
            // everything else is a string for JSON
            result = result.substring("{\"contact\":{\"firstName\":\"".length());
            result = result.substring(0, result.length() - "\"}}".length());
        }
        return result;
    }

}
