package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasConversionException;
import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AbsoluteValue;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.ActionDetail;
import io.atlasmap.v2.ActionParameter;
import io.atlasmap.v2.Add;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.GenerateUUID;
import io.atlasmap.v2.IndexOf;
import io.atlasmap.v2.Prepend;
import io.atlasmap.v2.SimpleField;
import io.atlasmap.v2.Trim;
import io.atlasmap.v2.Uppercase;

public class DefaultAtlasFieldActionsServiceTest {

    private DefaultAtlasFieldActionService fieldActionsService = null;

    @Before
    public void setUp() {
        fieldActionsService = DefaultAtlasFieldActionService.getInstance();
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

        ActionDetail actionDetail = fieldActionsService.findActionDetail(new Prepend(), FieldType.STRING);
        assertNotNull(actionDetail);
        System.out.println(new ObjectMapper().writeValueAsString(actionDetail.getActionSchema()));

        actionDetail = fieldActionsService.findActionDetail(new IndexOf(), FieldType.STRING);
        assertNotNull(actionDetail);

        actionDetail = fieldActionsService.findActionDetail(new Action() {}, FieldType.STRING);
        assertNull(actionDetail);

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
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Add());
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

        field.setActions(new ArrayList<Action>());
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);

        field.setActions(new ArrayList<Action>());
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);

        field.getActions().add(new Trim());
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
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Add());
        field.setActions(actions);
        fieldActionsService.processActions(mock(AtlasInternalSession.class), field);
    }

    @Test
    public void testProcessActionWithActionActionDetailObject() throws AtlasException {
        DefaultAtlasFieldActionService.ActionProcessor processor = null;
        Object sourceObject = "String";
        Action action = new Trim();
        processor = fieldActionsService.findActionProcessor(action, FieldType.STRING);
        assertEquals(sourceObject, processor.process(action, sourceObject));

        action = new GenerateUUID();
        processor = fieldActionsService.findActionProcessor(action, FieldType.NONE);
        assertNotNull(processor.process(action, sourceObject));
    }

    @Test
    public void testProcessActionWithActionActionDetailObjectAssignableType() throws AtlasException {
        Action action = new AbsoluteValue();
        Object sourceObject = new Integer("1");
        DefaultAtlasFieldActionService.ActionProcessor processor = fieldActionsService.findActionProcessor(action, FieldType.STRING);
        assertEquals(1L, processor.process(action, sourceObject));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetActionDetailByActionName() {
        assertNotNull(fieldActionsService.getActionDetailByActionName("Add"));
        assertNull(fieldActionsService.getActionDetailByActionName("AtlasAdd"));
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
        fieldGroup.setActions(new ArrayList<Action>());
        fieldGroup.getActions().add(action);
        Field answer = fieldActionsService.processActions(session, fieldGroup);
        assertEquals(FieldGroup.class, answer.getClass());
        FieldGroup answerGroup = (FieldGroup)answer;
        assertEquals("ONE", answerGroup.getField().get(0).getValue());
        assertEquals("TWO", answerGroup.getField().get(1).getValue());
        assertEquals("THREE", answerGroup.getField().get(2).getValue());
    }
}
