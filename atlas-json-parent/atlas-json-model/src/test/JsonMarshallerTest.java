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
package io.atlasmap.json.v2;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.json.v2.JsonInspectionRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class JsonMarshallerTest extends BaseMarshallerTest {

    public ObjectMapper mapper = null;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.deleteTestFolders = false;

        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        mapper = null;
    }

    @Test
    public void testJsonJsonInspectionRequest() throws Exception {
        JsonInspectionRequest request = generateInspectionRequest();
        // Object to JSON in file
        mapper.writeValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName()
                + File.separator + "atlasmapping-jsoninspection-request.json"), request);
        JsonInspectionRequest uRequest = mapper
                .readValue(
                        new File("target" + File.separator + "junit" + File.separator + testName.getMethodName()
                                + File.separator + "atlasmapping-jsoninspection-request.json"),
                        JsonInspectionRequest.class);
        assertNotNull(uRequest);
    }
}
