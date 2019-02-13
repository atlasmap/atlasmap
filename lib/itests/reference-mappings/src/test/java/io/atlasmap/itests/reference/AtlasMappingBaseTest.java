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
package io.atlasmap.itests.reference;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.Audit;

public abstract class AtlasMappingBaseTest {

    public static final List<String> FLAT_FIELDS = Arrays.asList("intField", "shortField", "longField", "doubleField",
            "floatField", "booleanField", "charField", "byteField", "boxedBooleanField", "boxedByteField",
            "boxedCharField", "boxedDoubleField", "boxedFloatField", "boxedIntField", "boxedLongField",
            "boxedStringField");

    protected AtlasContextFactory atlasContextFactory = null;

    @Before
    public void setUp() {
        atlasContextFactory = DefaultAtlasContextFactory.getInstance();
    }

    @After
    public void tearDown() {
        atlasContextFactory = null;
    }

    protected String printAudit(AtlasSession session) {
        StringBuilder buf = new StringBuilder("Audits: ");
        for (Audit a : session.getAudits().getAudit()) {
            buf.append('[');
            buf.append(a.getStatus());
            buf.append(", message=");
            buf.append(a.getMessage());
            buf.append(", path=");
            buf.append(a.getPath());
            buf.append("], ");
        }
        return buf.toString();
    }

}
