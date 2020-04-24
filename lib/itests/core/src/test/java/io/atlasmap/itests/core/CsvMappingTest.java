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
package io.atlasmap.itests.core;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.Mapping;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class CsvMappingTest {

    public static final String MAPPINGS_JSON = "mappings/atlasmapping-csv.json";
    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void testMapAllFields() throws Exception {
        AtlasContext context = createContext(MAPPINGS_JSON, "1", "2");
        AtlasSession session = context.createSession();
        session.setSourceDocument("source", "first_name,last_name\r\nBob,Johnson\r\nAndrew,Smith\r\n");
        context.process(session);

        assertFalse(session.hasErrors());
        Object csv = session.getTargetDocument("target-csv");
        assertThat(csv, CoreMatchers.is("first,last\r\nBob,Johnson\r\nAndrew,Smith\r\n"));
    }

    @Test
    public void testMapOneOfTwoFields() throws Exception {
        AtlasContext context = createContext(MAPPINGS_JSON, "3");
        AtlasSession session = context.createSession();
        session.setSourceDocument("source", "first_name,last_name\r\nBob,Johnson\r\nAndrew,Smith\r\n");
        context.process(session);

        assertFalse(session.hasErrors());
        Object csv = session.getTargetDocument("target-csv");
        assertThat(csv, CoreMatchers.is("last\r\nJohnson\r\nSmith\r\n"));
    }

    @Test
    public void testCapitalize() throws Exception {
        AtlasContext context = createContext(MAPPINGS_JSON, "4");
        AtlasSession session = context.createSession();
        session.setSourceDocument("source", "first_name,last_name\r\nbob,johnson\r\nandrew,smith\r\n");
        context.process(session);

        assertFalse(session.hasErrors());
        Object csv = session.getTargetDocument("target-csv");
        assertThat(csv, CoreMatchers.is("first\r\nBob\r\nAndrew\r\n"));
    }

    public AtlasContext createContext(String file, String... mappingIds) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-csv.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        List<String> ids = Arrays.asList(mappingIds);
        mapping.getMappings().getMapping().removeIf(m -> {
            if (m instanceof Mapping) {
                return !ids.contains(((Mapping) m).getId());
            } else if (m instanceof Collection) {
                Collection col = (Collection) m;
                col.getMappings().getMapping().removeIf(map -> !ids.contains(((Mapping) map).getId()));
                return col.getMappings().getMapping().isEmpty();
            } else {
                return false;
            }
        });
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        return context;
    }

}
