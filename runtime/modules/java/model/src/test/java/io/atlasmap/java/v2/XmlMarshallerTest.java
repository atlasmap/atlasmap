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
package io.atlasmap.java.v2;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.v2.AtlasMapping;

public class XmlMarshallerTest extends BaseMarshallerTest {

    private JAXBContext jaxbContext = null;
    private Marshaller marshaller = null;
    private Unmarshaller unmarshaller = null;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        this.deleteTestFolders = false;

        jaxbContext = JAXBContext.newInstance("io.atlasmap.v2:io.atlasmap.java.v2");

        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        unmarshaller = jaxbContext.createUnmarshaller();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        marshaller = null;
        unmarshaller = null;
        jaxbContext = null;
    }

    @Test
    public void testXmlJavaField() throws Exception {
        marshaller.marshal(generateAtlasMapping(), new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
        validateAtlasMapping(readAtlasMapping().getValue());
    }

    @Test
    public void testJsonSeparateJavaField() throws Exception {
        marshaller.marshal(generateSeparateAtlasMapping(), new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
        validateSeparateAtlasMapping(readAtlasMapping().getValue());
    }

    @Test
    public void testJsonCombineJavaField() throws Exception {
        marshaller.marshal(generateCombineMapping(), new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
        validateCombineAtlasMapping(readAtlasMapping().getValue());
    }

    @Test
    public void testJsonPropertyJavaField() throws Exception {
        marshaller.marshal(generatePropertyReferenceMapping(), new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
        validatePropertyAtlasMapping(readAtlasMapping().getValue());
    }

    @Test
    public void testJsonConstantJavaField() throws Exception {
        marshaller.marshal(generateConstantMapping(), new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
        validateConstantAtlasMapping(readAtlasMapping().getValue());
    }

    @Test
    public void testJsonMultisourceJavaField() throws Exception {
        marshaller.marshal(generateMultiSourceMapping(), new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
        validateMultisourceAtlasMapping(readAtlasMapping().getValue());
    }

    @Test
    public void testJsonCollectionJavaField() throws Exception {
        marshaller.marshal(generateCollectionMapping(), new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
        validateCollectionAtlasMapping(readAtlasMapping().getValue());
    }

    @Test
    public void testJsonActionJavaField() throws Exception {
        marshaller.marshal(generateActionMapping(), new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
        validateAtlasMapping(readAtlasMapping().getValue());
    }

    private JAXBElement<AtlasMapping> readAtlasMapping() throws JAXBException {
        StreamSource fileSource = new StreamSource(
                new File("target/junit/" + testName.getMethodName() + "/" + "atlasmapping.xml"));
        JAXBElement<AtlasMapping> mappingElem = unmarshaller.unmarshal(fileSource, AtlasMapping.class);
        assertNotNull(mappingElem);
        assertNotNull(mappingElem.getValue());
        return mappingElem;
    }
}
