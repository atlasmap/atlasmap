/*
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
package io.atlasmap.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Constants;
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
        mapping.setName("generated.mapping." + UUID.randomUUID().toString().replace('-', '.'));
        mapping.getProperties().getProperty().addAll(generateAtlasProperties());
        mapping.setConstants(new Constants());
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

    public static AtlasSession generateAtlasSession() throws Exception {
        AtlasMapping mappings = generateAtlasMapping();
        Map<String, Object> runtimeProperties = generateRuntimeProperties();
        return new DefaultAtlasSession(new DefaultAtlasContext(null)) {
            @Override
            public Map<String, Object> getSourceProperties() {
                return runtimeProperties;
            }
            @Override
            public AtlasMapping getMapping() {
                return mappings;
            }
        };
    }
}
