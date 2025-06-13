package io.atlasmap.customcode;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.atlasmap.v2.CustomMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vagrant
 * @version $ 10/9/24
 */
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class ObjectAutoMapping extends CustomMapping {
    protected List<Field> inputField;
    protected List<Field> outputField;
    protected List<String> fieldsMapping;

    public FieldGroup getInputFieldGroup() {
        return inputFieldGroup;
    }

    protected FieldGroup inputFieldGroup;

    public Map<String, String> getFieldsMappingAsMap() {
        Map<String, String> localfieldsMapping = new HashMap<>();
        if (fieldsMapping == null)
            return localfieldsMapping;
        for (int i= 0;i<fieldsMapping.size(); i = i+2) {
            localfieldsMapping.put(fieldsMapping.get(i), fieldsMapping.get(i+1));
        }
        return localfieldsMapping;
    }

    public List<String> getFieldsMapping() {
        if (fieldsMapping == null) {
            fieldsMapping = new ArrayList<String>();
        }
        return this.fieldsMapping;
    }

    public List<Field> getInputField() {
        if (inputField == null) {
            inputField = new ArrayList<Field>();
        }
        return this.inputField;
    }

    public List<Field> getOutputField() {
        if (outputField == null) {
            outputField = new ArrayList<Field>();
        }
        return this.outputField;
    }
}
