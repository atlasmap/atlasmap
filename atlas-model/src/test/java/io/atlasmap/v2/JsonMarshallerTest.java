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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.v2.AtlasMapping;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;

public class JsonMarshallerTest extends BaseMarshallerTest {

    public ObjectMapper mapper = null;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.deleteTestFolders = false;

        mapper = new AtlasJsonMapper();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        mapper = null;
    }

    @Test
    public void testReferenceMapping() throws Exception {
        AtlasMapping atlasMapping = generateReferenceAtlasMapping();
        // Object to JSON in file
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping.json"), atlasMapping);
        AtlasMapping uMapping = mapper.readValue(new File("target" + File.separator + "junit" + File.separator + testName.getMethodName() + File.separator + "atlasmapping.json"), AtlasMapping.class);
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

    // @Test
    // public void testLookupTable() throws Exception {
    // AtlasMapping atlasMapping = generateAtlasMapping();
    // atlasMapping.getLookupTables().getLookupTable().add(generateLookupTable());
    // mapper.writeValue(new File("target" + File.separator + "junit" +
    // File.separator + testName.getMethodName()
    // + File.separator + "atlasmapping.json"), atlasMapping);
    // AtlasMapping uMapping = mapper.readValue(new File("target" + File.separator +
    // "junit" + File.separator + testName.getMethodName() + File.separator +
    // "atlasmapping.json"), AtlasMapping.class);
    // //AtlasMapping uMapping = mapper.readValue(new
    // File("src/test/resources/json/mockfield/atlasmapping-lookup.json"),
    // // AtlasMapping.class);
    // assertNotNull(uMapping);
    // validateAtlasMapping(uMapping);
    // assertNotNull(uMapping.getLookupTables());
    // assertNotNull(uMapping.getLookupTables().getLookupTable());
    // assertEquals(new Integer(1), new
    // Integer(uMapping.getLookupTables().getLookupTable().size()));
    // assertNotNull(uMapping.getLookupTables().getLookupTable().get(0).getLookupEntryList());
    // assertNotNull(uMapping.getLookupTables().getLookupTable().get(0).getLookupEntryList().getLookupEntry());
    // assertEquals(new Integer(2), new Integer(
    // uMapping.getLookupTables().getLookupTable().get(0).getLookupEntryList().getLookupEntry().size()));
    // }
    //
    
    @Test
    public void testFieldActions() throws Exception {
        AtlasMapping atlasMapping = generateReferenceAtlasMapping();
        BaseMapping fm = atlasMapping.getMappings().getMapping().get(0);
        ((Mapping) fm).getOutputField().get(0).setActions(new Actions());
        ((Mapping) fm).getOutputField().get(0).getActions().getActions().add(new Uppercase());
        ((Mapping) fm).getOutputField().get(0).getActions().getActions().add(new Lowercase());
        
        SubString subString = new SubString();
        subString.setEndIndex(5);
        subString.setStartIndex(2);
        ((Mapping) fm).getOutputField().get(0).getActions().getActions().add(subString);

        SubStringAfter subStringAfter = new SubStringAfter();
        subStringAfter.setMatch("a");
        subStringAfter.setEndIndex(5);
        subStringAfter.setStartIndex(2);
        ((Mapping) fm).getOutputField().get(0).getActions().getActions().add(subStringAfter);
        
        SubStringBefore subStringBefore = new SubStringBefore();
        subStringBefore.setMatch("z");
        subStringBefore.setEndIndex(5);
        subStringBefore.setStartIndex(2);
        ((Mapping) fm).getOutputField().get(0).getActions().getActions().add(subStringBefore);
        
        PadStringRight psr = new PadStringRight();
        psr.setPadCharacter("z");
        psr.setPadCount(25);
        ((Mapping) fm).getOutputField().get(0).getActions().getActions().add(psr);
        
        mapper.writeValue(new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.json"), atlasMapping);
        
        AtlasMapping rereadMapping = mapper.readValue(new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.json"), AtlasMapping.class);
    }
    
    //
    // @Test
    // public void testMulitSourceMapping() throws Exception {
    // AtlasMapping atlasMapping = generateMultiSourceMapping();
    // Mapping fm = atlasMapping.getMappings().getMapping().get(0);
    // ((Map) fm).getOutputField().setActions(new Actions());
    // ((Map) fm).getOutputField().getActions().setUppercase(new Uppercase());
    //
    // mapper.writeValue(new File("target/junit/" + testName.getMethodName() + "/" +
    // "atlasmapping.json"),atlasMapping);
    // StreamSource fileSource = new StreamSource(new File("target/junit/" +
    // testName.getMethodName() + "/" + "atlasmapping.json"));
    // }

}
