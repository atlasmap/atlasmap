package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AbsoluteValue;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.ActionParameter;
import io.atlasmap.v2.Actions;
import io.atlasmap.v2.Add;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.GenerateUUID;
import io.atlasmap.v2.IndexOf;
import io.atlasmap.v2.SimpleField;
import io.atlasmap.v2.Trim;
import io.atlasmap.v2.Uppercase;

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
    public void testFindActionDetail() throws Exception {
        ActionDetail actionDetail = fieldActionsService.findActionDetail(new IndexOf(), FieldType.STRING);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail(new Action() {}, FieldType.STRING);
        assertNull(actionDetail);

        ActionDetail ad = new ActionDetail();
        ad.setName("IndexOf");
        ad.setSourceType(FieldType.INTEGER);
        fieldActionsService.listActionDetails().add(ad);

        actionDetail = fieldActionsService.findActionDetail(new IndexOf(), null);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail(new IndexOf(), FieldType.STRING);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail(new IndexOf(), FieldType.ANY);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail(new IndexOf(), FieldType.NONE);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail(new IndexOf(), FieldType.BOOLEAN);
        assertNotNull(actionDetail);
    }

    @Test(expected = AtlasConversionException.class)
    public void testProcessActionsActionsFieldAtlasConversionException() throws AtlasException {
        SimpleField field = new SimpleField();
        Object value = new Object();
        field.setValue(value);
        field.setFieldType(FieldType.INTEGER);
        Actions actions = new Actions();
        actions.getActions().add(new Add());
        field.setActions(actions);
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);
    }

    @Test
    public void testProcessActionsActionsField() throws AtlasException {
        SimpleField field = new SimpleField();

        field.setFieldType(FieldType.COMPLEX);
        field.setActions(null);
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);

        field.setValue(null);
        field.setFieldType(FieldType.INTEGER);
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);

        field.setValue(new Integer(0));
        field.setFieldType(FieldType.INTEGER);
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);

        @SuppressWarnings("serial")
        class MockActions extends Actions {
            @Override
            public List<Action> getActions() {
                return null;
            }
        }
        field.setActions(new MockActions());
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);

        field.setActions(new Actions());
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);

        field.getActions().getActions().add(new Trim());
        field.setValue("testString");
        field.setFieldType(FieldType.STRING);
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);

        field.setValue(new Integer(8));
        field.setFieldType(FieldType.NUMBER);
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);

    }

    @Test(expected = AtlasConversionException.class)
    public void testprocessActionsActionsObjectFieldTypeAtlasConversionException() throws AtlasException {
        SimpleField field = new SimpleField();
        Object value = new Object();
        field.setValue(value);
        field.setFieldType(FieldType.INTEGER);
        Actions actions = new Actions();
        actions.getActions().add(new Add());
        field.setActions(actions);
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);
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

    @Test
    public void testProcessActionForEachCollectionItem() throws Exception {
        DefaultAtlasSession session = mock(DefaultAtlasSession.class);
        FieldGroup fieldGroup = new FieldGroup();
        Field field1 = new SimpleField();
        field1.setValue("one");
        fieldGroup.getField().add(field1);
        Field field2 = new SimpleField();
        field2.setValue("two");
        fieldGroup.getField().add(field2);
        Field field3 = new SimpleField();
        field3.setValue("three");
        fieldGroup.getField().add(field3);
        Action action = new Uppercase();
        fieldGroup.setActions(new Actions());
        fieldGroup.getActions().getActions().add(action);
        Field answer = fieldActionsService.processActions(session, fieldGroup);
        assertEquals(FieldGroup.class, answer.getClass());
        FieldGroup answerGroup = (FieldGroup)answer;
        assertEquals("ONE", answerGroup.getField().get(0).getValue());
        assertEquals("TWO", answerGroup.getField().get(1).getValue());
        assertEquals("THREE", answerGroup.getField().get(2).getValue());
    }
}
