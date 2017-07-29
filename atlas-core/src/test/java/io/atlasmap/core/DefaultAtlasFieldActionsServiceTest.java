package io.atlasmap.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.Property;

public class DefaultAtlasFieldActionsServiceTest {

    private DefaultAtlasFieldActionService fieldActionsService = null;
    
    @Before
    public void setUp() throws Exception {
        fieldActionsService = new DefaultAtlasFieldActionService(DefaultAtlasConversionService.getInstance());
        fieldActionsService.init();
    }

    @After
    public void tearDown() throws Exception {
        fieldActionsService = null;
    }

    @Test
    public void testInit() {
        assertNotNull(fieldActionsService);
        assertNotNull(fieldActionsService.listActionDetails());
        assertTrue(fieldActionsService.listActionDetails().size() > 0);
    }

    @Test
    public void testListActionDetails() {
        assertNotNull(fieldActionsService);
        List<ActionDetail> actionDetails = fieldActionsService.listActionDetails();
        for(ActionDetail d : actionDetails) {
            if(d.getParameters() != null) {
                System.out.println("Action: " + d.getName());
                for(Property prop : d.getParameters().getProperty()) {
                    System.out.println("\t param: " + prop.getName());
                    System.out.println("\t type: " + prop.getFieldType().value());
                }
            }
        }
    }
}
