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

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.atlasmap.v2.Field;
import io.atlasmap.v2.Fields;
import io.atlasmap.xml.v2.Restriction;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlFields;

public class BaseXmlInspectionServiceTest {

    protected void debugFields(Fields xmlFields) {
        for (Field field : xmlFields.getField()) {
            assertTrue(field instanceof XmlField);
            XmlField xmlField = (XmlField) field;
            printXmlField(xmlField);
            if (xmlField instanceof XmlComplexType) {
                debugFields(((XmlComplexType) xmlField).getXmlFields());
            }
        }
    }

    protected void debugFields(XmlFields xmlFields) {
        for (XmlField xmlField : xmlFields.getXmlField()) {
            printXmlField(xmlField);
            if (xmlField instanceof XmlComplexType && (((XmlComplexType) xmlField).getXmlFields() != null)) {
                debugFields(((XmlComplexType) xmlField).getXmlFields());
            }
        }
    }

    protected void printXmlField(XmlField xmlField) {
        System.out.println("Name --> " + xmlField.getName());
        System.out.println("Path --> " + xmlField.getPath());
        System.out.println("Attribute? --> " + xmlField.isAttribute());
        System.out.println("Value --> " + xmlField.getValue());
        if (xmlField.getFieldType() != null) {
            System.out.println("Type --> " + xmlField.getFieldType().name());
        }
        if (xmlField.getTypeName() != null) {
            System.out.println("Type Name --> " + xmlField.getTypeName());
        }
        if (xmlField.getCollectionType() != null) {
            System.out.println("Collection Type --> " + xmlField.getCollectionType().name());
        }
        if (xmlField.getRestrictions() != null && !xmlField.getRestrictions().getRestriction().isEmpty()) {
            for (Restriction restriction : xmlField.getRestrictions().getRestriction()) {
                if (restriction != null) {
                    System.out.println("Restriction Type--> " + restriction.getType());
                    System.out.println("Restriction Type Value--> " + restriction.getValue());
                }
            }
        }
        System.out.println();
    }

}
