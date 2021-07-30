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
package io.atlasmap.builder;

import static io.atlasmap.api.AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID;
import static io.atlasmap.api.AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.atlasmap.core.BaseDefaultAtlasContextTest;
import io.atlasmap.v2.FieldType;

public class DefaultAtlasMappingBuilderTest extends BaseDefaultAtlasContextTest {

    @Test
    public void test() throws Exception {
        DefaultAtlasMappingBuilder builder = new DefaultAtlasMappingBuilder() {
            @Override
            public void processMapping() throws Exception {
                read(DEFAULT_SOURCE_DOCUMENT_ID, "/f1")
                    .write(DEFAULT_TARGET_DOCUMENT_ID, "/f2");
                readConstant("c3")
                    .writeProperty(DEFAULT_TARGET_DOCUMENT_ID, "p4");
                readProperty(DEFAULT_SOURCE_DOCUMENT_ID, "p5")
                    .write(DEFAULT_TARGET_DOCUMENT_ID, "/f6");
                write(DEFAULT_TARGET_DOCUMENT_ID, "/f7", "f7value");
            }
        };
        populateConstant("c3", "c3value");
        populateSourceField(DEFAULT_SOURCE_DOCUMENT_ID, FieldType.STRING, "f1", "f1value");
        populateProperty(DEFAULT_SOURCE_DOCUMENT_ID, "p5", "p5value");
        builder.setAtlasSession(session);
        builder.processMapping();
        assertEquals(0, session.getAudits().getAudit().size(), printAudit(session));
        assertEquals("f1value", getTargetFieldValue("/f2"));
        assertEquals("c3value", session.getTargetProperties().get("p4"));
        assertEquals("p5value", getTargetFieldValue("/f6"));
        assertEquals("f7value", getTargetFieldValue("/f7"));
    }
}
