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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.TestHelper;
import twitter4j.Status;
import twitter4j.User;

public class CamelAtlasmap14Test {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/camel-atlasmap-14-mapping.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(generateTwitterStatus());
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getDefaultTargetDocument();
        assertEquals(String.class, output.getClass());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode outJson = mapper.readTree((String)output);
        assertNotNull(outJson.get("TwitterScreenName__c"));
        assertEquals("bobvila1982", outJson.get("TwitterScreenName__c").asText());
    }

    protected Status generateTwitterStatus() {
        Status status = mock(Status.class);
        User user = mock(User.class);
        when(user.getName()).thenReturn("Bob Vila");
        when(user.getScreenName()).thenReturn("bobvila1982");
        when(status.getUser()).thenReturn(user);
        when(status.getText()).thenReturn("Let's build a house!");
        return status;
    }

}
