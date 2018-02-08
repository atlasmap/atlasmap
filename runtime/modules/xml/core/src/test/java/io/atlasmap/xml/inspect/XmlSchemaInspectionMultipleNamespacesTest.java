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
                Assert.assertEquals(true, namespace.isTargetNamespace());
                break;
            case "first":
                Assert.assertEquals("io.atlasmap.xml.test:First", namespace.getUri());
                Assert.assertEquals(false, namespace.isTargetNamespace());
                break;
            case "second":
                Assert.assertEquals("io.atlasmap.xml.test:Second", namespace.getUri());
                Assert.assertEquals(false, namespace.isTargetNamespace());
                break;
            default:
                Assert.fail(String.format("Unknown alias '%s'", namespace.getAlias()));
            }
        }
        List<Field> fields = answer.getFields().getField();
        Assert.assertEquals(1, fields.size());
        XmlComplexType complex = XmlComplexType.class.cast(fields.get(0));
        Assert.assertEquals("RootDocument", complex.getName());
        List<XmlField> rootFields = complex.getXmlFields().getXmlField();
        Assert.assertEquals(4, rootFields.size());
        for (XmlField xmlField : rootFields) {
            switch (xmlField.getName()) {
            case "Name":
                Assert.assertEquals(FieldType.STRING, xmlField.getFieldType());
                Assert.assertEquals("/RootDocument/Name", xmlField.getPath());
                break;
            case "Value":
                Assert.assertEquals(FieldType.STRING, xmlField.getFieldType());
                Assert.assertEquals("/RootDocument/Value", xmlField.getPath());
                break;
            case "first:FirstElement":
                Assert.assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                Assert.assertEquals("/RootDocument/first:FirstElement", xmlField.getPath());
                List<XmlField> firstFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                Assert.assertEquals(2, firstFields.size());
                for (XmlField firstField : firstFields) {
                    switch (firstField.getName()) {
                    case "first:Name":
                        Assert.assertEquals(FieldType.STRING, firstField.getFieldType());
                        Assert.assertEquals("/RootDocument/first:FirstElement/first:Name", firstField.getPath());
                        break;
                    case "first:Value":
                        Assert.assertEquals(FieldType.STRING, firstField.getFieldType());
                        Assert.assertEquals("/RootDocument/first:FirstElement/first:Value", firstField.getPath());
                        break;
                    default:
                        Assert.fail(String.format("Unknown field '%s'", firstField.getName()));
                    }
                }
                break;
            case "second:SecondElement":
                Assert.assertEquals(FieldType.COMPLEX, xmlField.getFieldType());
                Assert.assertEquals("/RootDocument/second:SecondElement", xmlField.getPath());
                List<XmlField> secondFields = XmlComplexType.class.cast(xmlField).getXmlFields().getXmlField();
                Assert.assertEquals(2, secondFields.size());
                for (XmlField secondField : secondFields) {
                    switch (secondField.getName()) {
                    case "second:Name":
                        Assert.assertEquals(FieldType.STRING, secondField.getFieldType());
                        Assert.assertEquals("/RootDocument/second:SecondElement/second:Name", secondField.getPath());
                        break;
                    case "second:Value":
                        Assert.assertEquals(FieldType.STRING, secondField.getFieldType());
                        Assert.assertEquals("/RootDocument/second:SecondElement/second:Value", secondField.getPath());
                        break;
                    default:
                        Assert.fail(String.format("Unknown field '%s'", secondField.getName()));
                    }
                }
                break;
            default:
                Assert.fail(String.format("Unknown field '%s'", xmlField.getName()));
            }
        }
    }

}
