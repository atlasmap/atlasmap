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
package io.atlasmap.xml.inspect;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals(3, answer.getXmlNamespaces().getXmlNamespace().size());
        for (XmlNamespace namespace : answer.getXmlNamespaces().getXmlNamespace()) {
            switch (namespace.getAlias()) {
            case "tns":
                Assert.assertEquals("io.atlasmap.xml.test:Root", namespace.getUri());
                Assert.assertEquals(null, namespace.isTargetNamespace());
                break;
            case "first":
                Assert.assertEquals("io.atlasmap.xml.test:First", namespace.getUri());
                Assert.assertEquals(null, namespace.isTargetNamespace());
                break;
            case "second":
                Assert.assertEquals("io.atlasmap.xml.test:Second", namespace.getUri());
                Assert.assertEquals(null, namespace.isTargetNamespace());
                break;
            default:
                Assert.fail(String.format("Unknown alias '%s'", namespace.getAlias()));
            }
        }

        List<Field> fields = answer.getFields().getField();
        Assert.assertEquals(1, fields.size());
        XmlComplexType complex = XmlComplexType.class.cast(fields.get(0));
        Assert.assertEquals("tns:RootDocument", complex.getName());
        List<XmlField> rootFields = complex.getXmlFields().getXmlField();
        Assert.assertEquals(4, rootFields.size());
        for (XmlField xmlField : rootFields) {
            switch (xmlField.getName()) {
            case "tns:Name":
                Assert.assertEquals(FieldType.STRING, xmlField.getFieldType());
                Assert.assertEquals("/tns:RootDocument/tns:Name", xmlField.getPath());
                break;
            case "tns:Value":
                Assert.assertEquals(FieldType.STRING, xmlField.getFieldType());
                Assert.assertEquals("/tns:RootDocument/tns:Value", xmlField.getPath());
                break;
            case "first:FirstElement":
                Assert.assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                Assert.assertEquals("/tns:RootDocument/first:FirstElement", xmlField.getPath());
                List<XmlField> firstFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                Assert.assertEquals(2, firstFields.size());
                for (XmlField firstField : firstFields) {
                    switch (firstField.getName()) {
                    case "first:Name":
                        Assert.assertEquals(FieldType.STRING, firstField.getFieldType());
                        Assert.assertEquals("/tns:RootDocument/first:FirstElement/first:Name", firstField.getPath());
                        break;
                    case "first:Value":
                        Assert.assertEquals(FieldType.STRING, firstField.getFieldType());
                        Assert.assertEquals("/tns:RootDocument/first:FirstElement/first:Value", firstField.getPath());
                        break;
                    default:
                        Assert.fail(String.format("Unknown field '%s'", firstField.getPath()));
                    }
                }
                break;
            case "second:SecondElement":
                Assert.assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                Assert.assertEquals("/tns:RootDocument/second:SecondElement", xmlField.getPath());
                List<XmlField> secondFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                Assert.assertEquals(2, secondFields.size());
                for (XmlField secondField : secondFields) {
                    switch (secondField.getName()) {
                    case "second:Name":
                        Assert.assertEquals(FieldType.STRING, secondField.getFieldType());
                        Assert.assertEquals("/tns:RootDocument/second:SecondElement/second:Name", secondField.getPath());
                        break;
                    case "second:Value":
                        Assert.assertEquals(FieldType.STRING, secondField.getFieldType());
                        Assert.assertEquals("/tns:RootDocument/second:SecondElement/second:Value", secondField.getPath());
                        break;
                    default:
                        Assert.fail(String.format("Unknown field '%s'", secondField.getPath()));
                    }
                }
                break;
            default:
                Assert.fail(String.format("Unknown field '%s'", xmlField.getPath()));
            }
        }
    }

    @Test
    public void testMultipleNoNamespaceSchemas() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/multiple-no-namespace-schemas.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument answer = service.inspectSchema(schemaFile);
        Assert.assertEquals(1, answer.getXmlNamespaces().getXmlNamespace().size());
        XmlNamespace namespace = answer.getXmlNamespaces().getXmlNamespace().get(0);
        Assert.assertEquals("tns", namespace.getAlias());
        Assert.assertEquals("io.atlasmap.xml.test:Root", namespace.getUri());
        Assert.assertEquals(null, namespace.isTargetNamespace());

        Assert.assertEquals(1,  answer.getFields().getField().size());
        XmlComplexType complex = XmlComplexType.class.cast(answer.getFields().getField().get(0));
        Assert.assertEquals("tns:RootDocument", complex.getName());
        Assert.assertEquals(2,  complex.getXmlFields().getXmlField().size());
        for (XmlField field : complex.getXmlFields().getXmlField()) {
            switch (field.getName()) {
            case "FirstElement":
                Assert.assertEquals(FieldType.COMPLEX, field.getFieldType());
                Assert.assertEquals("/tns:RootDocument/FirstElement", field.getPath());
                List<XmlField> firstFields = XmlComplexType.class.cast(field).getXmlFields().getXmlField();
                Assert.assertEquals(1, firstFields.size());
                XmlField firstField = firstFields.get(0);
                Assert.assertEquals("FirstValue", firstField.getName());
                Assert.assertEquals(FieldType.STRING, firstField.getFieldType());
                Assert.assertEquals("/tns:RootDocument/FirstElement/FirstValue", firstField.getPath());
                break;
            case "SecondElement":
                Assert.assertEquals(FieldType.COMPLEX, field.getFieldType());
                Assert.assertEquals("/tns:RootDocument/SecondElement", field.getPath());
                List<XmlField> secondFields = XmlComplexType.class.cast(field).getXmlFields().getXmlField();
                Assert.assertEquals(1, secondFields.size());
                XmlField secondField = secondFields.get(0);
                Assert.assertEquals("SecondValue", secondField.getName());
                Assert.assertEquals(FieldType.STRING, secondField.getFieldType());
                Assert.assertEquals("/tns:RootDocument/SecondElement/SecondValue", secondField.getPath());
                break;
            default:
                Assert.fail(String.format("Unknown field '%s'", field.getPath()));
            }
        }
    }

    @Test
    public void testMultipleNoNamespaceRootSchema() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/multiple-no-namespace-root-schema.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument answer = service.inspectSchema(schemaFile);
        Assert.assertEquals(1, answer.getXmlNamespaces().getXmlNamespace().size());
        XmlNamespace namespace = answer.getXmlNamespaces().getXmlNamespace().get(0);
        Assert.assertEquals("second", namespace.getAlias());
        Assert.assertEquals("io.atlasmap.xml.test:Second", namespace.getUri());
        Assert.assertEquals(null, namespace.isTargetNamespace());

        // Note that the FirstElement also appears here as it's a top level element of namespace ""
        Assert.assertEquals(2,  answer.getFields().getField().size());
        XmlComplexType complex = XmlComplexType.class.cast(answer.getFields().getField().get(1));
        Assert.assertEquals("RootDocument", complex.getName());
        Assert.assertEquals(2,  complex.getXmlFields().getXmlField().size());
        for (XmlField field : complex.getXmlFields().getXmlField()) {
            switch (field.getName()) {
            case "FirstElement":
                Assert.assertEquals(FieldType.COMPLEX, field.getFieldType());
                Assert.assertEquals("/RootDocument/FirstElement", field.getPath());
                List<XmlField> firstFields = XmlComplexType.class.cast(field).getXmlFields().getXmlField();
                Assert.assertEquals(1, firstFields.size());
                XmlField firstField = firstFields.get(0);
                Assert.assertEquals("FirstValue", firstField.getName());
                Assert.assertEquals(FieldType.STRING, firstField.getFieldType());
                Assert.assertEquals("/RootDocument/FirstElement/FirstValue", firstField.getPath());
                break;
            case "second:SecondElement":
                Assert.assertEquals(FieldType.COMPLEX, field.getFieldType());
                Assert.assertEquals("/RootDocument/second:SecondElement", field.getPath());
                List<XmlField> secondFields = XmlComplexType.class.cast(field).getXmlFields().getXmlField();
                Assert.assertEquals(1, secondFields.size());
                XmlField secondField = secondFields.get(0);
                Assert.assertEquals("second:SecondValue", secondField.getName());
                Assert.assertEquals(FieldType.STRING, secondField.getFieldType());
                Assert.assertEquals("/RootDocument/second:SecondElement/second:SecondValue", secondField.getPath());
                break;
            default:
                Assert.fail(String.format("Unknown field '%s'", field.getPath()));
            }
        }
    }

    @Test(expected = XmlInspectionException.class)
    public void testMultipleNoNamespaceSchemasConflict() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/multiple-no-namespace-schemas-conflict.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        service.inspectSchema(schemaFile);
    }

    @Test
    public void testSyndesis() throws Exception {
        File schemaFile = Paths.get("src/test/resources/inspect/multiple-namespaces-syndesis.xml").toFile();
        XmlInspectionService service = new XmlInspectionService();
        XmlDocument answer = service.inspectSchema(schemaFile);
        Assert.assertEquals(1, answer.getXmlNamespaces().getXmlNamespace().size());
        XmlNamespace namespace = answer.getXmlNamespaces().getXmlNamespace().get(0);
        Assert.assertEquals("tns", namespace.getAlias());
        Assert.assertEquals("http://syndesis.io/v1/swagger-connector-template/request", namespace.getUri());
        Assert.assertEquals(null, namespace.isTargetNamespace());

        List<Field> fields = answer.getFields().getField();
        Assert.assertEquals(1, fields.size());
        XmlComplexType complex = XmlComplexType.class.cast(fields.get(0));
        Assert.assertEquals("tns:request", complex.getName());
        List<XmlField> rootFields = complex.getXmlFields().getXmlField();
        Assert.assertEquals(1, rootFields.size());
        complex = XmlComplexType.class.cast(rootFields.get(0));
        Assert.assertEquals("tns:body", complex.getName());
        List<XmlField> bodyFields = complex.getXmlFields().getXmlField();
        Assert.assertEquals(1,  bodyFields.size());
        complex = XmlComplexType.class.cast(bodyFields.get(0));
        Assert.assertEquals("Pet", complex.getName());
        List<XmlField> petFields = complex.getXmlFields().getXmlField();
        Assert.assertEquals(6, petFields.size());
        for (XmlField xmlField : petFields) {
            switch (xmlField.getName()) {
            case "id":
                Assert.assertEquals(FieldType.DECIMAL, xmlField.getFieldType());
                Assert.assertEquals("/tns:request/tns:body/Pet/id", xmlField.getPath());
                break;
            case "Category":
                Assert.assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                Assert.assertEquals("/tns:request/tns:body/Pet/Category", xmlField.getPath());
                List<XmlField> categoryFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                Assert.assertEquals(2, categoryFields.size());
                for (XmlField categoryField : categoryFields) {
                    switch (categoryField.getName()) {
                    case "id":
                        Assert.assertEquals(FieldType.DECIMAL, categoryField.getFieldType());
                        Assert.assertEquals("/tns:request/tns:body/Pet/Category/id", categoryField.getPath());
                        break;
                    case "name":
                        Assert.assertEquals(FieldType.STRING, categoryField.getFieldType());
                        Assert.assertEquals("/tns:request/tns:body/Pet/Category/name", categoryField.getPath());
                        break;
                    default:
                        Assert.fail(String.format("Unknown field '%s'", categoryField.getPath()));
                    }
                }
                break;
            case "name":
                Assert.assertEquals(FieldType.STRING, xmlField.getFieldType());
                Assert.assertEquals("/tns:request/tns:body/Pet/name", xmlField.getPath());
                break;
            case "photoUrl":
                Assert.assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                Assert.assertEquals("/tns:request/tns:body/Pet/photoUrl", xmlField.getPath());
                List<XmlField> photoUrlFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                Assert.assertEquals(1, photoUrlFields.size());
                XmlField photoUrlField = photoUrlFields.get(0);
                Assert.assertEquals(FieldType.STRING, photoUrlField.getFieldType());
                Assert.assertEquals("/tns:request/tns:body/Pet/photoUrl/photoUrl", photoUrlField.getPath());
                break;
            case "tag":
                Assert.assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                Assert.assertEquals("/tns:request/tns:body/Pet/tag", xmlField.getPath());
                List<XmlField> tagFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                Assert.assertEquals(1, tagFields.size());
                XmlField tagField = tagFields.get(0);
                Assert.assertEquals(FieldType.COMPLEX, tagField.getFieldType());
                Assert.assertEquals("/tns:request/tns:body/Pet/tag/Tag", tagField.getPath());
                List<XmlField> tagTagFields = XmlComplexType.class.cast(tagField).getXmlFields().getXmlField();
                Assert.assertEquals(2, tagTagFields.size());
                for (XmlField tagTagField : tagTagFields) {
                    switch (tagTagField.getName()) {
                    case "id":
                        Assert.assertEquals(FieldType.DECIMAL, tagTagField.getFieldType());
                        Assert.assertEquals("/tns:request/tns:body/Pet/tag/Tag/id", tagTagField.getPath());
                        break;
                    case "name":
                        Assert.assertEquals(FieldType.STRING, tagTagField.getFieldType());
                        Assert.assertEquals("/tns:request/tns:body/Pet/tag/Tag/name", tagTagField.getPath());
                        break;
                    default:
                        Assert.fail(String.format("Unknown field '%s'", tagTagField.getPath()));
                    }
                }
                break;
            case "status":
                Assert.assertEquals(FieldType.STRING, xmlField.getFieldType());
                Assert.assertEquals("/tns:request/tns:body/Pet/status", xmlField.getPath());
                break;
            default:
                Assert.fail(String.format("Unknown field '%s'", xmlField.getPath()));
            }
        }
    }

}
