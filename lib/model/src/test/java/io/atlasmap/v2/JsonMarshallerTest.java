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

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMarshallerTest extends BaseMarshallerTest {

    public ObjectMapper mapper = null;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.deleteTestFolders = false;

        mapper = Json.mapper();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        mapper = null;
    }

    @Test
    public void testReferenceMapping() throws Exception {
        AtlasMapping atlasMapping = generateReferenceAtlasMapping();
        // Object to JSON in file
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("target" + File.separator + "junit" + File.separator
                + testName.getMethodName() + File.separator + "atlasmapping.json"), atlasMapping);
        AtlasMapping uMapping = mapper.readValue(new File("target" + File.separator + "junit" + File.separator
                + testName.getMethodName() + File.separator + "atlasmapping.json"), AtlasMapping.class);
        assertNotNull(uMapping);
        validateReferenceAtlasMapping(uMapping);
    }

    @Test
    public void testLookupMapping() throws Exception {
        AtlasMapping atlasMapping = generateReferenceAtlasMapping();
        atlasMapping.getLookupTables().getLookupTable().add(generateLookupTable());
        // Object to JSON in file
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("target" + File.separator + "junit" + File.separator
                + testName.getMethodName() + File.separator + "atlasmapping.json"), atlasMapping);
        AtlasMapping uMapping = mapper.readValue(new File("target" + File.separator + "junit" + File.separator
                + testName.getMethodName() + File.separator + "atlasmapping.json"), AtlasMapping.class);
        assertNotNull(uMapping);
        validateReferenceAtlasMapping(uMapping);
    }

    @Test
    public void testFieldActions() throws Exception {
        AtlasMapping atlasMapping = generateReferenceAtlasMapping();
        BaseMapping fm = atlasMapping.getMappings().getMapping().get(0);
        ((Mapping) fm).getOutputField().get(0).setActions(new Actions());

        List<Action> actionsList = ModelTestUtil.getAllOOTBActions();
        ((Mapping) fm).getOutputField().get(0).getActions().getActions().addAll(actionsList);

        for (Action a : ((Mapping) fm).getOutputField().get(0).getActions().getActions()) {
            if (a instanceof CustomAction) {
                CustomAction customAction = (CustomAction) a;
                customAction.setName("Bar");
                customAction.setClassName("io.foo.Bar");
                customAction.setMethodName("doStuff");
                customAction.setInputFieldType(FieldType.STRING);
                customAction.setOutputFieldType(FieldType.STRING);
            } else if (a instanceof ReplaceFirst) {
                ReplaceFirst replace = (ReplaceFirst) a;
                replace.setMatch("test");
                replace.setNewString("h");
            } else if (a instanceof SubString) {
                SubString subString = (SubString) a;
                subString.setEndIndex(5);
                subString.setStartIndex(2);
            } else if (a instanceof SubStringAfter) {
                SubStringAfter subStringAfter = (SubStringAfter) a;
                subStringAfter.setMatch("a");
                subStringAfter.setEndIndex(5);
                subStringAfter.setStartIndex(2);
            } else if (a instanceof SubStringBefore) {
                SubStringBefore subStringBefore = (SubStringBefore) a;
                subStringBefore.setMatch("z");
                subStringBefore.setEndIndex(5);
                subStringBefore.setStartIndex(2);
            } else if (a instanceof PadStringLeft) {
                PadStringLeft psl = (PadStringLeft) a;
                psl.setPadCharacter("a");
                psl.setPadCount(25);
            } else if (a instanceof PadStringRight) {
                PadStringRight psr = (PadStringRight) a;
                psr.setPadCharacter("z");
                psr.setPadCount(25);
            } else if (a instanceof ConvertAreaUnit) {
                ConvertAreaUnit cau = (ConvertAreaUnit) a;
                cau.setFromUnit(AreaUnitType.SQUARE_FOOT);
                cau.setToUnit(AreaUnitType.SQUARE_METER);
            } else if (a instanceof ConvertDistanceUnit) {
                ConvertDistanceUnit cdu = (ConvertDistanceUnit) a;
                cdu.setFromUnit(DistanceUnitType.FOOT_FT);
                cdu.setToUnit(DistanceUnitType.METER_M);
            } else if (a instanceof ConvertMassUnit) {
                ConvertMassUnit cmu = (ConvertMassUnit) a;
                cmu.setFromUnit(MassUnitType.KILOGRAM_KG);
                cmu.setToUnit(MassUnitType.POUND_LB);
            } else if (a instanceof ConvertVolumeUnit) {
                ConvertVolumeUnit cvu = (ConvertVolumeUnit) a;
                cvu.setFromUnit(VolumeUnitType.CUBIC_FOOT);
                cvu.setToUnit(VolumeUnitType.CUBIC_METER);
            }
        }

        mapper.writeValue(new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.json"),
                atlasMapping);
        AtlasMapping generatedMapping = mapper.readValue(
                new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.json"), AtlasMapping.class);

        List<Action> generatedActions = ((Mapping) generatedMapping.getMappings().getMapping().get(0))
                                            .getOutputField().get(0).getActions().getActions();
        assertEquals(actionsList.size(), generatedActions.size());
        for (int i=0; i<actionsList.size(); i++) {
            assertEquals(String.format(
                    "Did you forget to add '%s' in ActionsJsonDeserializer/ActionsJsonSerializer?",
                    actionsList.get(i).getClass().getName()),
                    actionsList.get(i).getClass().getName(), generatedActions.get(i).getClass().getName());
        }
    }

}
