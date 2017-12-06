package io.atlasmap.core.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.Audit;
import twitter4j.Status;
import twitter4j.User;

public class CamelAtlasmap14Test {

    private AtlasMappingService mappingService;

    @Before
    public void before() throws Exception {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue-camel-atlasmap-14-mapping.json");
        AtlasMapping mapping = mappingService.loadMapping(url, AtlasMappingFormat.JSON);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(generateTwitterStatus());
        context.process(session);
        assertFalse(printAudit(session), session.hasErrors());
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

    protected String printAudit(AtlasSession session) {
        StringBuilder buf = new StringBuilder("Audits: ");
        for (Audit a : session.getAudits().getAudit()) {
            buf.append('[');
            buf.append(a.getStatus());
            buf.append(", message=");
            buf.append(a.getMessage());
            buf.append("], ");
        }
        return buf.toString();
    }
}
