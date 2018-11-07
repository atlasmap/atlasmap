/**
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
package io.atlasmap.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchema;

public class AtlasModelFactoryTest {

    @Test
    public void testCreateSeparateMapping() {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.SEPARATE);
        validateMapping(fm, MappingType.SEPARATE);
    }

    @Test
    public void testCreateMapMapping() {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        validateMapping(fm, MappingType.MAP);
    }

    @Test
    public void testCreateCombineMapping() {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.COMBINE);
        validateMapping(fm, MappingType.COMBINE);
    }

    @Test
    public void testCreateLookupMapping() {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.LOOKUP);
        validateMapping(fm, MappingType.LOOKUP);
    }

    @Test
    public void testCreateAtlasMapping() {
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
        assertNotNull(atlasMapping);
    }

    protected void validateMapping(Mapping fm, MappingType type) {
        assertNotNull(fm);
        assertNotNull(fm.getMappingType());
        assertEquals(type, fm.getMappingType());
        assertNull(fm.getAlias());
        assertNull(fm.getDescription());
        assertNotNull(fm.getOutputField());
        assertNotNull(fm.getInputField());
        assertEquals(new Integer(0), new Integer(fm.getOutputField().size()));
        assertEquals(new Integer(0), new Integer(fm.getOutputField().size()));
    }

    @Test
    public void testCloneActions() {
        List<Action> actionsList = Arrays.asList(new Camelize(), new Capitalize(), new CurrentDate(),
                new CurrentDateTime(), new CurrentTime(), new CustomAction(), new GenerateUUID(),
                new Lowercase(), new LowercaseChar(), new PadStringLeft(), new PadStringRight(),
                new SeparateByDash(), new SeparateByUnderscore(), new Length(), new SubString(),
                new SubStringAfter(), new SubStringBefore(), new Trim(), new TrimLeft(), new TrimRight(),
                new Uppercase(), new UppercaseChar());
        Actions actions = new Actions();
        actions.getActions().addAll(actionsList);
        Actions clones = AtlasModelFactory.cloneFieldActions(actions);
        assertNotNull(clones);
        assertNotSame(actions, clones);
    }

    @Test
    public void testCloneAction() throws Exception {
        XSSchema schema = ModelTestUtil.getCoreSchema();
        for (Entry<String, XSElementDecl> entry : schema.getElementDecls().entrySet()) {
            String name = entry.getKey();
            XSElementDecl element = entry.getValue();
            if ("Action".equals(element.getType().getBaseType().getName())) {
                Class<?> clazz = Class.forName("io.atlasmap.v2." + name);
                Object a = clazz.newInstance();
                assertNotNull(a);
                Action b = AtlasModelFactory.cloneAction((Action)a);
                assertNotNull(String.format("Add %s to AtlasModelFactory#cloneAction()", a.getClass().getSimpleName()), b);
                assertNotSame(a, b);
                assertEquals(a.getClass().getCanonicalName(), b.getClass().getCanonicalName());
            }
        }
    }

    @Test
    public void testCreatePropertyField() {
        PropertyField p = AtlasModelFactory.createPropertyField();
        assertNotNull(p);
        assertNull(p.getActions());
    }

    @Test
    public void testCloneField() {
        PropertyField p = AtlasModelFactory.createPropertyField();
        SimpleField s = AtlasModelFactory.cloneFieldToSimpleField(p);
        assertNotNull(s);
    }

    @Test
    public void testSimpleFieldToString() {
        SimpleField s = new SimpleField();
        System.out.println(AtlasModelFactory.toString(s));
    }
}
