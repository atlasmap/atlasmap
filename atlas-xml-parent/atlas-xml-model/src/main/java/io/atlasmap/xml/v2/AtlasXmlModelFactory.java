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
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;

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
        XmlField that = (XmlField) field;

        // generic from Field
        if (field.getActions() != null) {
            clone.setActions(AtlasModelFactory.cloneFieldActions(field.getActions()));
        }
        if (field.getArrayDimensions() != null) {
            clone.setArrayDimensions(Integer.valueOf(field.getArrayDimensions()));
        }
        if (field.getArraySize() != null) {
            clone.setArraySize(Integer.valueOf(field.getArraySize()));
        }
        if (field.getCollectionType() != null) {
            clone.setCollectionType(CollectionType.fromValue(field.getCollectionType().value()));
        }
        if (field.getDocId() != null) {
            clone.setDocId(new String(field.getDocId()));
        }
        if (field.getFieldType() != null) {
            clone.setFieldType(FieldType.fromValue(field.getFieldType().value()));
        }
        if (field.getIndex() != null) {
            clone.setIndex(Integer.valueOf(field.getIndex()));
        }
        if (field.getPath() != null) {
            clone.setPath(new String(field.getPath()));
        }
        if (field.isRequired() != null) {
            clone.setRequired(Boolean.valueOf(field.isRequired()));
        }
        if (field.getStatus() != null) {
            clone.setStatus(FieldStatus.fromValue(field.getStatus().value()));
        }
        // we can't deep clone value, so leave as null
        // clone.setValue();

        // xml specific
        clone.setAnnotations(that.getAnnotations());
        clone.setName(that.getName());
        clone.setNodeType(that.getNodeType());
        clone.setPrimitive(that.isPrimitive());
        clone.setRestrictions(that.getRestrictions());
        clone.setTypeName(that.getTypeName());
        clone.setUserCreated(that.isUserCreated());

        return clone;
    }
}
