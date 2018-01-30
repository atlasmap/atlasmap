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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.v2.Json;


public class AtlasJsonModelFactoryTest {

    @Test
    public void testCreateJsonDocument() {
        JsonDocument jsonDoc = AtlasJsonModelFactory.createJsonDocument();
        assertNotNull(jsonDoc);
        assertNotNull(jsonDoc.getFields());
        assertNotNull(jsonDoc.getFields().getField());
        assertEquals(new Integer(0), new Integer(jsonDoc.getFields().getField().size()));
    }

    @Test
    public void testCreateJsonInspection() throws Exception {
        JsonInspectionRequest request = new JsonInspectionRequest();
        request.setType(InspectionType.INSTANCE);
        request.setJsonData("{\n" + "  \"id\": \"0001\",\n" + "  \"type\": \"donut\",\n" + "  \"name\": \"Cake\",\n"
                + "  \"ppu\": 0.55,\n" + "  \"batters\":\n" + "  {\n" + "    \"batter\":\n" + "    [\n"
                + "      { \"id\": \"1001\", \"type\": \"Regular\" },\n"
                + "      { \"id\": \"1002\", \"type\": \"Chocolate\" },\n"
                + "      { \"id\": \"1003\", \"type\": \"Blueberry\" },\n"
                + "      { \"id\": \"1004\", \"type\": \"Devil's Food\" }\n" + "    ]\n" + "  },\n" + "  \"topping\":\n"
                + "  [\n" + "    { \"id\": \"5001\", \"type\": \"None\" },\n"
                + "    { \"id\": \"5002\", \"type\": \"Glazed\" },\n"
                + "    { \"id\": \"5005\", \"type\": \"Sugar\" },\n"
                + "    { \"id\": \"5007\", \"type\": \"Powdered Sugar\" },\n"
                + "    { \"id\": \"5006\", \"type\": \"Chocolate with Sprinkles\" },\n"
                + "    { \"id\": \"5003\", \"type\": \"Chocolate\" },\n"
                + "    { \"id\": \"5004\", \"type\": \"Maple\" }\n" + "  ]\n" + "}\n" + "");

        ObjectMapper mapper = Json.mapper();
        mapper.writeValue(new File("target/json-inspection-request.json"), request);
    }
}
