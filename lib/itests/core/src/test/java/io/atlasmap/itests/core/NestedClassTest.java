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
package io.atlasmap.itests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URL;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;

public class NestedClassTest {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-nested-class.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        SourceClass sc = new SourceClass();
        sc.setSomeField("some source class value");
        session.setSourceDocument("io.atlasmap.itests.core.SourceClass", sc);
        BaseClass.SomeNestedClass sic = new BaseClass.SomeNestedClass();
        sic.setSomeField("some nested class value");
        session.setSourceDocument("io.atlasmap.itests.core.BaseClass$SomeNestedClass", sic);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertFalse(session.hasWarns(), TestHelper.printAudit(session));
        Object tc = session.getTargetDocument("io.atlasmap.itests.core.TargetClass");
        assertEquals(TargetClass.class, tc.getClass());
        TargetClass target = TargetClass.class.cast(tc);
        assertEquals("some nested class value", target.getSomeField());
        Object tsic = session.getTargetDocument("io.atlasmap.itests.core.BaseClass$SomeNestedClass");
        assertEquals(BaseClass.SomeNestedClass.class, tsic.getClass());
        BaseClass.SomeNestedClass targetSic = BaseClass.SomeNestedClass.class.cast(tsic);
        assertEquals("some source class value", targetSic.getSomeField());
    }

}
