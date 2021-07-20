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
import java.nio.CharBuffer;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.test.StringTestClass;

public class JavaStringTypesTest {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-java-string-types.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        StringTestClass source = new StringTestClass();
        source.setTestCharBuffer(CharBuffer.wrap("testCharBuffer"));
        source.setTestCharSequence("testCharSequence");
        source.setTestString("testString");
        source.setTestStringBuffer(new StringBuffer("testStringBuffer"));
        source.setTestStringBuilder(new StringBuilder("testStringBuilder"));
        session.setDefaultSourceDocument(source);

        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getDefaultTargetDocument();
        assertEquals(StringTestClass.class, output.getClass());
        StringTestClass target = StringTestClass.class.cast(output);
        assertEquals("testCharBuffer", target.getTestCharSequence().toString());
        assertEquals("testCharSequence", target.getTestString());
        assertEquals("testString", target.getTestStringBuffer().toString());
        assertEquals("testStringBuffer", target.getTestStringBuilder().toString());
        assertEquals("testStringBuilder", target.getTestCharBuffer().toString());
    }

}
