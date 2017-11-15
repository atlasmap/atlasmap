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
package io.atlasmap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.v2.AtlasJsonMapper;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.StringMap;
import io.atlasmap.v2.StringMapEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AtlasServiceTest {

    private AtlasService service = null;
    private ObjectMapper mapper = null;

    @Before
    public void setUp() throws Exception {
        service = new AtlasService();
        mapper = new AtlasJsonMapper();
    }

    @After
    public void tearDown() throws Exception {
        service = null;
        mapper = null;
    }

    @Test
    public void testListMappings() throws Exception {
        Response resp = service.listMappings(
                generateTestUriInfo("http://localhost:8686/v2/atlas", "http://localhost:8686/v2/atlas/mappings"), null);
        StringMap sMap = (StringMap) resp.getEntity();
        System.out.println("Found " + sMap.getStringMapEntry().size() + " objects");
        for (StringMapEntry s : sMap.getStringMapEntry()) {
            System.out.println("\t n: " + s.getName() + " v: " + s.getValue());
        }
    }

    @Test
    public void testGetMapping() throws Exception {
        Response resp = service.getMappingRequest("junit3");
        assertEquals(AtlasMapping.class, resp.getEntity().getClass());
    }

    @Test
    public void testFilenameMatch() throws Exception {
        String fileName = "atlasmapping-foo.xml";
        assertTrue(fileName.matches("atlasmapping-[a-zA-Z0-9]+.xml"));
    }

    @Test
    public void testActionDeserialization() throws Exception {
        File file = new File("src/test/resources/atlasmapping-actions.json");
        AtlasMapping mapping = mapper.readValue(file, AtlasMapping.class);

        Mappings mappings = mapping.getMappings();
        for (BaseMapping baseMapping : mappings.getMapping()) {
            if (MappingType.MAP.equals(baseMapping.getMappingType())) {
                List<Field> fields = ((Mapping) baseMapping).getOutputField();
                for (Field f : fields) {
                    if (f.getActions() != null && f.getActions().getActions() != null
                            && !f.getActions().getActions().isEmpty()) {
                        System.out.println("Found actions: " + f.getActions().getActions().size());
                    }
                }
            }
        }
    }

    protected UriInfo generateTestUriInfo(String baseUri, String absoluteUri) throws Exception {
        return new TestUriInfo(new URI(baseUri), new URI(absoluteUri));
    }

}
