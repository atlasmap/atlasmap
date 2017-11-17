package io.atlasmap.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Property;

public class AtlasTestData {

    public static List<Property> generateAtlasProperties() {
        List<Property> props = new ArrayList<Property>();
        Property p = new Property();
        p.setName("prop-boolean");
        p.setValue("false");
        p.setFieldType(FieldType.BOOLEAN);
        props.add(p);

        p = new Property();
        p.setName("prop-byte");
        p.setValue("92");
        p.setFieldType(FieldType.BYTE);
        props.add(p);

        p = new Property();
        p.setName("prop-char");
        p.setValue("z");
        p.setFieldType(FieldType.CHAR);
        props.add(p);

        p = new Property();
        p.setName("prop-double");
        p.setValue(Double.toString(Double.MIN_VALUE));
        p.setFieldType(FieldType.DOUBLE);
        props.add(p);

        p = new Property();
        p.setName("prop-float");
        p.setValue(Float.toString(Float.MIN_VALUE));
        p.setFieldType(FieldType.FLOAT);
        props.add(p);

        p = new Property();
        p.setName("prop-int");
        p.setValue(Integer.toString(Integer.MIN_VALUE));
        p.setFieldType(FieldType.INTEGER);
        props.add(p);

        p = new Property();
        p.setName("prop-long");
        p.setValue(Long.toString(Long.MIN_VALUE));
        p.setFieldType(FieldType.LONG);
        props.add(p);

        p = new Property();
        p.setName("prop-short");
        p.setValue(Short.toString(Short.MIN_VALUE));
        p.setFieldType(FieldType.SHORT);
        props.add(p);

        p = new Property();
        p.setName("prop-string");
        p.setValue("helloworld");
        p.setFieldType(FieldType.STRING);
        props.add(p);

        p = new Property();
        p.setName("dupe-string");
        p.setValue("whatup");
        p.setFieldType(FieldType.STRING);
        props.add(p);

        return props;
    }

    public static AtlasMapping generateAtlasMapping() {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();
        mapping.getProperties().getProperty().addAll(generateAtlasProperties());
        return mapping;
    }

    public static Map<String, Object> generateRuntimeProperties() {
        Map<String, Object> runtimeProps = new HashMap<String, Object>();
        runtimeProps.put("key-boolean", true);
        runtimeProps.put("key-byte", new String("b").getBytes()[0]);
        runtimeProps.put("key-char", new String("a").charAt(0));
        runtimeProps.put("key-double", Double.MAX_VALUE);
        runtimeProps.put("key-float", Float.MAX_VALUE);
        runtimeProps.put("key-int", Integer.MAX_VALUE);
        runtimeProps.put("key-long", Long.MAX_VALUE);
        runtimeProps.put("key-short", Short.MAX_VALUE);
        runtimeProps.put("key-string", "foobar");
        runtimeProps.put("dupe-string", "uh oh");
        return runtimeProps;
    }
}
