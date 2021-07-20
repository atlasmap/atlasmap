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
package io.atlasmap.itests.core.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.TestHelper;

/**
 * https://github.com/atlasmap/atlasmap/issues/381 .
 */
public class Atlasmap381Test {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/atlasmap-381-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD-HH-mm-ss.SSS");
        Date sourceDate = dateFormat.parse("2001-01-01-01-01-01.001");
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", new SourceClass().setSourceDate(sourceDate));
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        Date targetDate = ((TargetClass)output).getTargetDate();
        ZonedDateTime localTargetDate = ZonedDateTime.ofInstant(targetDate.toInstant(), ZoneId.systemDefault());
        assertEquals(Month.FEBRUARY, localTargetDate.getMonth());
    }

}
