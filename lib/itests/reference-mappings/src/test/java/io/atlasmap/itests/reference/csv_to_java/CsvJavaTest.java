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
package io.atlasmap.itests.reference.csv_to_java;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.itests.reference.AtlasTestUtil;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.json.test.AtlasJsonTestUnrootedMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CsvJavaTest extends AtlasMappingBaseTest {

    protected TargetFlatPrimitiveClass executeMapping(String fileName) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(fileName).toURI());
        AtlasSession session = context.createSession();
        String source = AtlasTestUtil.loadFileAsString(
            "src/test/resources/csvToJava/atlasmapping-csvWithHeaders.csv");
        session.setDefaultSourceDocument(source);
        context.process(session);

        assertFalse(session.hasErrors(), printAudit(session));
        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        return (TargetFlatPrimitiveClass) object;
    }

    @Test
    public void testProcess() throws Exception {
        TargetFlatPrimitiveClass object = executeMapping("src/test/resources/csvToJava/atlasmapping-csvWithHeaders.json");
        assertEquals("row0", object.getBoxedStringArrayField()[0]);
        assertEquals("row1", object.getBoxedStringArrayField()[1]);
    }
}
