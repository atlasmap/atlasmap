package io.atlasmap.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultAtlasFieldActionsServiceTest {

    private DefaultAtlasFieldActionService fieldActionsService = null;
    
    @Before
    public void setUp() throws Exception {
        fieldActionsService = new DefaultAtlasFieldActionService();
    }

    @After
    public void tearDown() throws Exception {
        fieldActionsService = null;
    }

    @Test
    public void testInit() {
        assertNotNull(fieldActionsService);
        fieldActionsService.init();
        assertNotNull(fieldActionsService.listActionDetails());
        assertTrue(fieldActionsService.listActionDetails().size() > 0);
    }

    @Test
    public void testGetFieldActionInfo() {
    }

}
