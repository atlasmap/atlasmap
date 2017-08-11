package io.atlasmap.core.issue;

import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.v2.AtlasMapping;
import io.syndesis.connector.salesforce.Contact;
import twitter4j.Status;

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
        session.setInput(generateTwitterStatus());
        context.process(session);
        Object output = session.getOutput();
        Assert.assertEquals(Contact.class, output.getClass());
        Contact contact = (Contact)output;
        Assert.assertEquals("bobvila1982", contact.getTwitterScreenName__c());
    }
    
    protected Status generateTwitterStatus() {
        MockStatus status = new MockStatus();
        MockUser user = new MockUser();
        user.setName("Bob Vila");
        user.setScreenName("bobvila1982");
        status.setUser(user);
        status.setText("Let's build a house!");
        return status;
    }
}
