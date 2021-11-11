/*
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
package io.atlasmap.xml.inspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlNamespace;

public class XmlSchemaInspectionMultipleNamespacesTest extends BaseXmlInspectionServiceTest {

    @Test
    public void testMultipleNamespaces() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/multiple-namespaces-schemaset.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument answer = service.inspectSchema(schemaFile);
        assertEquals(3, answer.getXmlNamespaces().getXmlNamespace().size());
        for (XmlNamespace namespace : answer.getXmlNamespaces().getXmlNamespace()) {
            switch (namespace.getAlias()) {
            case "tns":
                assertEquals("io.atlasmap.xml.test:Root", namespace.getUri());
                assertEquals(null, namespace.isTargetNamespace());
                break;
            case "first":
                assertEquals("io.atlasmap.xml.test:First", namespace.getUri());
                assertEquals(null, namespace.isTargetNamespace());
                break;
            case "second":
                assertEquals("io.atlasmap.xml.test:Second", namespace.getUri());
                assertEquals(null, namespace.isTargetNamespace());
                break;
            default:
                fail(String.format("Unknown alias '%s'", namespace.getAlias()));
            }
        }

        List<Field> fields = answer.getFields().getField();
        assertEquals(1, fields.size());
        XmlComplexType complex = XmlComplexType.class.cast(fields.get(0));
        assertEquals("tns:RootDocument", complex.getName());
        List<XmlField> rootFields = complex.getXmlFields().getXmlField();
        assertEquals(4, rootFields.size());
        for (XmlField xmlField : rootFields) {
            switch (xmlField.getName()) {
            case "Name":
                assertEquals(FieldType.STRING, xmlField.getFieldType());
                assertEquals("/tns:RootDocument/Name", xmlField.getPath());
                break;
            case "Value":
                assertEquals(FieldType.STRING, xmlField.getFieldType());
                assertEquals("/tns:RootDocument/Value", xmlField.getPath());
                break;
            case "first:FirstElement":
                assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                assertEquals("/tns:RootDocument/first:FirstElement", xmlField.getPath());
                List<XmlField> firstFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                assertEquals(2, firstFields.size());
                for (XmlField firstField : firstFields) {
                    switch (firstField.getName()) {
                    case "Name":
                        assertEquals(FieldType.STRING, firstField.getFieldType());
                        assertEquals("/tns:RootDocument/first:FirstElement/Name", firstField.getPath());
                        break;
                    case "Value":
                        assertEquals(FieldType.STRING, firstField.getFieldType());
                        assertEquals("/tns:RootDocument/first:FirstElement/Value", firstField.getPath());
                        break;
                    default:
                        fail(String.format("Unknown field '%s'", firstField.getPath()));
                    }
                }
                break;
            case "second:SecondElement":
                assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                assertEquals("/tns:RootDocument/second:SecondElement", xmlField.getPath());
                List<XmlField> secondFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                assertEquals(2, secondFields.size());
                for (XmlField secondField : secondFields) {
                    switch (secondField.getName()) {
                    case "Name":
                        assertEquals(FieldType.STRING, secondField.getFieldType());
                        assertEquals("/tns:RootDocument/second:SecondElement/Name", secondField.getPath());
                        break;
                    case "Value":
                        assertEquals(FieldType.STRING, secondField.getFieldType());
                        assertEquals("/tns:RootDocument/second:SecondElement/Value", secondField.getPath());
                        break;
                    default:
                        fail(String.format("Unknown field '%s'", secondField.getPath()));
                    }
                }
                break;
            default:
                fail(String.format("Unknown field '%s'", xmlField.getPath()));
            }
        }
    }

    @Test
    public void testMultipleNoNamespaceSchemas() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/multiple-no-namespace-schemas.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument answer = service.inspectSchema(schemaFile);
        assertEquals(1, answer.getXmlNamespaces().getXmlNamespace().size());
        XmlNamespace namespace = answer.getXmlNamespaces().getXmlNamespace().get(0);
        assertEquals("tns", namespace.getAlias());
        assertEquals("io.atlasmap.xml.test:Root", namespace.getUri());
        assertEquals(null, namespace.isTargetNamespace());

        assertEquals(1,  answer.getFields().getField().size());
        XmlComplexType complex = XmlComplexType.class.cast(answer.getFields().getField().get(0));
        assertEquals("tns:RootDocument", complex.getName());
        assertEquals(2,  complex.getXmlFields().getXmlField().size());
        for (XmlField field : complex.getXmlFields().getXmlField()) {
            switch (field.getName()) {
            case "FirstElement":
                assertEquals(FieldType.COMPLEX, field.getFieldType());
                assertEquals("/tns:RootDocument/FirstElement", field.getPath());
                List<XmlField> firstFields = XmlComplexType.class.cast(field).getXmlFields().getXmlField();
                assertEquals(1, firstFields.size());
                XmlField firstField = firstFields.get(0);
                assertEquals("FirstValue", firstField.getName());
                assertEquals(FieldType.STRING, firstField.getFieldType());
                assertEquals("/tns:RootDocument/FirstElement/FirstValue", firstField.getPath());
                break;
            case "SecondElement":
                assertEquals(FieldType.COMPLEX, field.getFieldType());
                assertEquals("/tns:RootDocument/SecondElement", field.getPath());
                List<XmlField> secondFields = XmlComplexType.class.cast(field).getXmlFields().getXmlField();
                assertEquals(1, secondFields.size());
                XmlField secondField = secondFields.get(0);
                assertEquals("SecondValue", secondField.getName());
                assertEquals(FieldType.STRING, secondField.getFieldType());
                assertEquals("/tns:RootDocument/SecondElement/SecondValue", secondField.getPath());
                break;
            default:
                fail(String.format("Unknown field '%s'", field.getPath()));
            }
        }
    }

    @Test
    public void testMultipleNoNamespaceRootSchema() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/multiple-no-namespace-root-schema.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument answer = service.inspectSchema(schemaFile);
        assertEquals(1, answer.getXmlNamespaces().getXmlNamespace().size());
        XmlNamespace namespace = answer.getXmlNamespaces().getXmlNamespace().get(0);
        assertEquals("second", namespace.getAlias());
        assertEquals("io.atlasmap.xml.test:Second", namespace.getUri());
        assertEquals(null, namespace.isTargetNamespace());

        // Note that the FirstElement also appears here as it's a top level element of namespace ""
        assertEquals(2,  answer.getFields().getField().size());
        XmlComplexType complex = XmlComplexType.class.cast(answer.getFields().getField().get(1));
        assertEquals("RootDocument", complex.getName());
        assertEquals(2,  complex.getXmlFields().getXmlField().size());
        for (XmlField field : complex.getXmlFields().getXmlField()) {
            switch (field.getName()) {
            case "FirstElement":
                assertEquals(FieldType.COMPLEX, field.getFieldType());
                assertEquals("/RootDocument/FirstElement", field.getPath());
                List<XmlField> firstFields = XmlComplexType.class.cast(field).getXmlFields().getXmlField();
                assertEquals(1, firstFields.size());
                XmlField firstField = firstFields.get(0);
                assertEquals("FirstValue", firstField.getName());
                assertEquals(FieldType.STRING, firstField.getFieldType());
                assertEquals("/RootDocument/FirstElement/FirstValue", firstField.getPath());
                break;
            case "second:SecondElement":
                assertEquals(FieldType.COMPLEX, field.getFieldType());
                assertEquals("/RootDocument/second:SecondElement", field.getPath());
                List<XmlField> secondFields = XmlComplexType.class.cast(field).getXmlFields().getXmlField();
                assertEquals(1, secondFields.size());
                XmlField secondField = secondFields.get(0);
                assertEquals("SecondValue", secondField.getName());
                assertEquals(FieldType.STRING, secondField.getFieldType());
                assertEquals("/RootDocument/second:SecondElement/SecondValue", secondField.getPath());
                break;
            default:
                fail(String.format("Unknown field '%s'", field.getPath()));
            }
        }
    }

    @Test
    public void testMultipleNoNamespaceSchemasConflict() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/multiple-no-namespace-schemas-conflict.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        assertThrows(XmlInspectionException.class, () -> {
            service.inspectSchema(schemaFile);
        });
    }

    @Test
    public void testSyndesis() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/multiple-namespaces-syndesis.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument answer = service.inspectSchema(schemaFile);
        assertEquals(1, answer.getXmlNamespaces().getXmlNamespace().size());
        XmlNamespace namespace = answer.getXmlNamespaces().getXmlNamespace().get(0);
        assertEquals("tns", namespace.getAlias());
        assertEquals("http://syndesis.io/v1/swagger-connector-template/request", namespace.getUri());
        assertEquals(null, namespace.isTargetNamespace());

        List<Field> fields = answer.getFields().getField();
        assertEquals(1, fields.size());
        XmlComplexType complex = XmlComplexType.class.cast(fields.get(0));
        assertEquals("tns:request", complex.getName());
        List<XmlField> rootFields = complex.getXmlFields().getXmlField();
        assertEquals(1, rootFields.size());
        complex = XmlComplexType.class.cast(rootFields.get(0));
        assertEquals("tns:body", complex.getName());
        List<XmlField> bodyFields = complex.getXmlFields().getXmlField();
        assertEquals(1,  bodyFields.size());
        complex = XmlComplexType.class.cast(bodyFields.get(0));
        assertEquals("Pet", complex.getName());
        List<XmlField> petFields = complex.getXmlFields().getXmlField();
        assertEquals(6, petFields.size());
        for (XmlField xmlField : petFields) {
            switch (xmlField.getName()) {
            case "id":
                assertEquals(FieldType.DECIMAL, xmlField.getFieldType());
                assertEquals("/tns:request/tns:body/Pet/id", xmlField.getPath());
                break;
            case "Category":
                assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                assertEquals("/tns:request/tns:body/Pet/Category", xmlField.getPath());
                List<XmlField> categoryFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                assertEquals(2, categoryFields.size());
                for (XmlField categoryField : categoryFields) {
                    switch (categoryField.getName()) {
                    case "id":
                        assertEquals(FieldType.DECIMAL, categoryField.getFieldType());
                        assertEquals("/tns:request/tns:body/Pet/Category/id", categoryField.getPath());
                        break;
                    case "name":
                        assertEquals(FieldType.STRING, categoryField.getFieldType());
                        assertEquals("/tns:request/tns:body/Pet/Category/name", categoryField.getPath());
                        break;
                    default:
                        fail(String.format("Unknown field '%s'", categoryField.getPath()));
                    }
                }
                break;
            case "name":
                assertEquals(FieldType.STRING, xmlField.getFieldType());
                assertEquals("/tns:request/tns:body/Pet/name", xmlField.getPath());
                break;
            case "photoUrl":
                assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                assertEquals("/tns:request/tns:body/Pet/photoUrl", xmlField.getPath());
                List<XmlField> photoUrlFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                assertEquals(1, photoUrlFields.size());
                XmlField photoUrlField = photoUrlFields.get(0);
                assertEquals(FieldType.STRING, photoUrlField.getFieldType());
                assertEquals("/tns:request/tns:body/Pet/photoUrl/photoUrl<>", photoUrlField.getPath());
                assertEquals(CollectionType.LIST, photoUrlField.getCollectionType());
                break;
            case "tag":
                assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                assertEquals("/tns:request/tns:body/Pet/tag", xmlField.getPath());
                List<XmlField> tagFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                assertEquals(1, tagFields.size());
                XmlField tagField = tagFields.get(0);
                assertEquals(FieldType.COMPLEX, tagField.getFieldType());
                assertEquals("/tns:request/tns:body/Pet/tag/Tag<>", tagField.getPath());
                assertEquals(CollectionType.LIST, tagField.getCollectionType());
                List<XmlField> tagTagFields = XmlComplexType.class.cast(tagField).getXmlFields().getXmlField();
                assertEquals(2, tagTagFields.size());
                for (XmlField tagTagField : tagTagFields) {
                    switch (tagTagField.getName()) {
                    case "id":
                        assertEquals(FieldType.DECIMAL, tagTagField.getFieldType());
                        assertEquals("/tns:request/tns:body/Pet/tag/Tag<>/id", tagTagField.getPath());
                        break;
                    case "name":
                        assertEquals(FieldType.STRING, tagTagField.getFieldType());
                        assertEquals("/tns:request/tns:body/Pet/tag/Tag<>/name", tagTagField.getPath());
                        break;
                    default:
                        fail(String.format("Unknown field '%s'", tagTagField.getPath()));
                    }
                }
                break;
            case "status":
                assertEquals(FieldType.STRING, xmlField.getFieldType());
                assertEquals("/tns:request/tns:body/Pet/status", xmlField.getPath());
                break;
            default:
                fail(String.format("Unknown field '%s'", xmlField.getPath()));
            }
        }
    }

}
