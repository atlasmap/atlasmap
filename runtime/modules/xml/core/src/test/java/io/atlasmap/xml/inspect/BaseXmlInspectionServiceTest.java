package io.atlasmap.xml.inspect;

import org.junit.Assert;

import io.atlasmap.v2.Field;
import io.atlasmap.v2.Fields;
import io.atlasmap.xml.v2.Restriction;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlField;
import io.atlasmap.xml.v2.XmlFields;

public class BaseXmlInspectionServiceTest {

    protected void debugFields(Fields xmlFields) {
        for (Field field : xmlFields.getField()) {
            Assert.assertTrue(field instanceof XmlField);
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
