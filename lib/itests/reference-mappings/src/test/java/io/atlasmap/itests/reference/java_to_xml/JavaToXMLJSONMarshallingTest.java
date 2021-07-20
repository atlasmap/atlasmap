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
package io.atlasmap.itests.reference.java_to_xml;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.atlasmap.v2.AtlasMapping;

public class JavaToXMLJSONMarshallingTest {
    public ObjectMapper mapper = null;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    @AfterEach
    public void tearDown() {
        mapper = null;
    }

    @Test
    public void testCombineMappingDemarshaller() throws Exception {
        // this test is for AT-466: issue saving mappings in combine mode (parser
        // complaining about strategy property)
        // the json has been changed from what the UI was sending, now the "actions"
        // property on the output field is "null" rather than "[]"
        String filename = "src/test/resources/javaToXml/javaToXmlMapping-combine.json";
        AtlasMapping uMapping = mapper.readValue(new File(filename), AtlasMapping.class);
        assertNotNull(uMapping);
    }

}
