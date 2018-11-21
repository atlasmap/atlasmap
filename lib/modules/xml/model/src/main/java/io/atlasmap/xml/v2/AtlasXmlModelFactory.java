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
package io.atlasmap.xml.v2;

import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.Fields;

public class AtlasXmlModelFactory {

    public static final String URI_FORMAT = "atlas:xml";

    public static XmlDocument createXmlDocument() {
        XmlDocument xmlDocument = new XmlDocument();
        xmlDocument.setFields(new Fields());
        return xmlDocument;
    }

    public static XmlField createXmlField() {
        XmlField xmlField = new XmlField();
        return xmlField;
    }

    public static Field cloneField(Field field) {
        XmlField clone = new XmlField();
        copyField(field, clone, true);
        return clone;
    }

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
