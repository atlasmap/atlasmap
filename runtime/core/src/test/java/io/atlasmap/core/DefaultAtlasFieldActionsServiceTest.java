package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.AbsoluteValue;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.ActionParameter;
import io.atlasmap.v2.Actions;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.GenerateUUID;
import io.atlasmap.v2.SimpleField;
import io.atlasmap.v2.Trim;

public class DefaultAtlasFieldActionsServiceTest {

    private DefaultAtlasFieldActionService fieldActionsService = null;

    @Before
    public void setUp() {
        fieldActionsService = new DefaultAtlasFieldActionService(DefaultAtlasConversionService.getInstance());
        fieldActionsService.init();
    }

    @After
    public void tearDown() {
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
        for (ActionDetail d : actionDetails) {
            if (d.getParameters() != null) {
                System.out.println("Action: " + d.getName());
                for (ActionParameter param : d.getParameters().getParameter()) {
                    System.out.println("\t param: " + param.getName());
                    System.out.println("\t type: " + param.getFieldType().value());
                }
            }
        }
    }

    @Test
    public void testFindActionDetail() {
        ActionDetail actionDetail = fieldActionsService.findActionDetail("IndexOf", FieldType.STRING);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail("Index", FieldType.STRING);
        assertNull(actionDetail);

        ActionDetail ad = new ActionDetail();
        ad.setName("IndexOf");
        ad.setSourceType(FieldType.INTEGER);
        fieldActionsService.listActionDetails().add(ad);

        actionDetail = fieldActionsService.findActionDetail("IndexOf", null);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail("IndexOf", FieldType.STRING);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail("IndexOf", FieldType.ANY);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail("IndexOf", FieldType.NONE);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail("IndexOf", FieldType.BOOLEAN);
        assertNotNull(actionDetail);
    }

    @Test(expected = AtlasConversionException.class)
    public void testProcessActionsActionsFieldAtlasConversionException() throws AtlasException {
        Actions actions = null;

        SimpleField field = new SimpleField();
        Object value = new Object();
        field.setValue(value);
        field.setFieldType(FieldType.INTEGER);
        fieldActionsService.processActions(actions, field);
    }

    @Test
    public void testProcessActionsActionsField() throws AtlasException {
        Actions actions = null;
        SimpleField field = new SimpleField();

        field.setFieldType(FieldType.COMPLEX);
        fieldActionsService.processActions(actions, field);

        field.setValue(null);
        field.setFieldType(FieldType.INTEGER);
        fieldActionsService.processActions(actions, field);

        field.setValue(new Integer(0));
        field.setFieldType(FieldType.INTEGER);
        fieldActionsService.processActions(actions, field);

        @SuppressWarnings("serial")
        class MockActions extends Actions {
            @Override
            public List<Action> getActions() {
                return null;
            }
        }
        fieldActionsService.processActions(new MockActions(), field);

        actions = new Actions();
        fieldActionsService.processActions(actions, field);

        actions.getActions().add(new Trim());
        field.setValue("testString");
        field.setFieldType(FieldType.STRING);
        fieldActionsService.processActions(actions, field);

        field.setValue(new Integer(8));
        field.setFieldType(FieldType.NUMBER);
        fieldActionsService.processActions(actions, field);

    }

    @Test(expected = AtlasConversionException.class)
    public void testprocessActionsActionsObjectFieldTypeAtlasConversionException() throws AtlasException {
        Actions actions = null;

        SimpleField field = new SimpleField();
        Object value = new Object();
        field.setValue(value);
        field.setFieldType(FieldType.INTEGER);
        fieldActionsService.processActions(actions, field);
    }

    @Test
    public void testprocessActionsActionsObjectFieldType() throws AtlasException {
        Actions actions = new Actions();

        assertNotNull(fieldActionsService.processActions(actions, "testString", FieldType.STRING));

        assertNotNull(fieldActionsService.processActions(actions, new Integer(8), FieldType.STRING));
    }

    @Test
    public void testProcessActionWithActionActionDetailObject() throws AtlasException {
        ActionDetail actionDetail = null;
        Object sourceObject = "String";
        Action action = new Trim();
        assertEquals(sourceObject, fieldActionsService.processAction(action, actionDetail, sourceObject));

        action = new GenerateUUID();
        actionDetail = new ActionDetail();
        actionDetail.setClassName("io.atlasmap.actions.StringComplexFieldActions");
        actionDetail.setSourceType(FieldType.ANY);
        actionDetail.setMethod("genareteUUID");
        assertNotNull(fieldActionsService.processAction(action, actionDetail, sourceObject));
    }

    @Test(expected = AtlasException.class)
    public void testProcessActionWithActionActionDetailObjectAtlasException() throws AtlasException {
        Action action = new AbsoluteValue();
        Object sourceObject = new Integer("1");
        ActionDetail actionDetail = new ActionDetail();
        actionDetail.setClassName("io.atlasmap.actions.NumberFieldActions");
        actionDetail.setSourceType(FieldType.INTEGER);
        actionDetail.setMethod("absoluteValue");

        fieldActionsService.processAction(action, actionDetail, sourceObject);
    }

    @Test(expected = AtlasException.class)
    public void testProcessActionWithActionActionDetailObjectAtlasExceptionNoMethod() throws AtlasException {
        Action action = new AbsoluteValue();
        Object sourceObject = new Integer("1");
        ActionDetail actionDetail = new ActionDetail();
        actionDetail.setClassName("io.atlasmap.actions.NumberFieldActions");
        actionDetail.setSourceType(FieldType.NUMBER);
        // actionDetail.setMethod("absolute");

        fieldActionsService.processAction(action, actionDetail, sourceObject);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetActionDetailByActionName() {
        assertNotNull(fieldActionsService.getActionDetailByActionName("Add"));
        assertNull(fieldActionsService.getActionDetailByActionName("AtlasAdd"));
    }

    @Test
    public void testCamelize() {
        assertNull(DefaultAtlasFieldActionService.camelize(null));
        assertEquals("", DefaultAtlasFieldActionService.camelize(""));
    }
}
