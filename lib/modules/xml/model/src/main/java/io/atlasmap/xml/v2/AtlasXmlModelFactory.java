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
package io.atlasmap.xml.v2;

import java.util.ArrayList;
import java.util.List;

import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;

/**
 * The model object factory for the XML Document.
 */
public class AtlasXmlModelFactory {

    /** URI format. */
    public static final String URI_FORMAT = "atlas:xml";

    /**
     * Creates the XML Document.
     * @return XML Document
     */
    public static XmlDocument createXmlDocument() {
        XmlDocument xmlDocument = new XmlDocument();
        xmlDocument.setFields(new Fields());
        return xmlDocument;
    }

    /**
     * Creates the XML complex field.
     * @return XML complex field
     */
    public static XmlComplexType createXmlComlexField() {
    
        XmlComplexType xmlComplexField = new XmlComplexType();
        xmlComplexField.setFieldType(FieldType.COMPLEX);
        return xmlComplexField;
    }

    /**
     * Creates the XML Field.
     * @return XML Field
     */
    public static XmlField createXmlField() {
        XmlField xmlField = new XmlField();
        return xmlField;
    }

    /**
     * Clones the XML Field.
     * @param field field
     * @param withActions true to also clone the field actions
     * @return cloned
     */
    public static XmlField cloneField(XmlField field, boolean withActions) {
        XmlField clone = new XmlField();
        copyField(field, clone, withActions);
        return clone;
    }

    /**
     * Clones the FieldGroup.
     * @param group FieldGroup
     * @return cloned
     */
    public static FieldGroup cloneFieldGroup(FieldGroup group) {
        FieldGroup clone = AtlasModelFactory.copyFieldGroup(group);
        List<Field> newChildren = new ArrayList<>();
        for (Field child : group.getField()) {
            if (child instanceof FieldGroup) {
                newChildren.add(cloneFieldGroup((FieldGroup)child));
            } else {
                newChildren.add(cloneField((XmlField)child, true));
            }
        }
        clone.getField().addAll(newChildren);
        return clone;
    }

    /**
     * Copies the XML Field.
     * @param from from
     * @param to to
     * @param withActions true to also clone the field actions
     */
    public static void copyField(Field from, Field to, boolean withActions) {
        AtlasModelFactory.copyField(from, to, withActions);

        // xml specific
        if (from instanceof XmlField && to instanceof XmlField) {
            XmlField fromXml = (XmlField)from;
            XmlField toXml = (XmlField)to;
            toXml.setAnnotations(fromXml.getAnnotations());
            toXml.setName(fromXml.getName());
            toXml.setNodeType(fromXml.getNodeType());
            toXml.setPrimitive(fromXml.isPrimitive());
            toXml.setRestrictions(fromXml.getRestrictions());
            toXml.setTypeName(fromXml.getTypeName());
            toXml.setUserCreated(fromXml.isUserCreated());
        }
    }

}
