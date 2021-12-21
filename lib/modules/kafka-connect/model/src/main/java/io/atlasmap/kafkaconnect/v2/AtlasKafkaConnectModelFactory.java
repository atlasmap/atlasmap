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
package io.atlasmap.kafkaconnect.v2;

import java.util.ArrayList;
import java.util.List;

import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;

/**
 * The model factory for Kafka Connect module.
 */
public class AtlasKafkaConnectModelFactory {
    /** URI format. */
    public static final String URI_FORMAT = "atlas:kafkaconnect";

    /**
     * Creates the Kafka Connect Document.
     * @return Document
     */
    public static KafkaConnectDocument createKafkaConnectDocument() {
        KafkaConnectDocument kafkaConnectDocument = new KafkaConnectDocument();
        kafkaConnectDocument.setFields(new Fields());
        return kafkaConnectDocument;
    }

    /**
     * Creates the Kafka Connect Field.
     * @return Field
     */
    public static KafkaConnectField createKafkaConnectField() {
        KafkaConnectField kafkaConnectField = new KafkaConnectField();
        return kafkaConnectField;
    }

    /**
     * Creates the Kafka Connect ComplexType.
     * @return Complex Field
     */
    public static KafkaConnectComplexType createKafkaConnectComplexType() {
        KafkaConnectComplexType kafkaConnectComplexField = new KafkaConnectComplexType();
        kafkaConnectComplexField.setKafkaConnectFields(new KafkaConnectFields());
        kafkaConnectComplexField.setFieldType(FieldType.COMPLEX);
        return kafkaConnectComplexField;
    }

    /**
     * Gets a string representation of the Kafka Connect Field.
     * @param f Field
     * @return string
     */
    public static String toString(KafkaConnectField f) {
        return "JsonField [name=" + f.getName() + ", primitive=" + f.isPrimitive() + ", typeName=" + f.getTypeName()
                + ", actions=" + f.getActions() + ", value=" + f.getValue()
                + ", arrayDimensions=" + f.getArrayDimensions() + ", arraySize=" + f.getArraySize()
                + ", collectionType=" + f.getCollectionType() + ", docId=" + f.getDocId() + ", index=" + f.getIndex()
                + ", path=" + f.getPath() + ", required=" + f.isRequired() + ", status=" + f.getStatus()
                + ", fieldType=" + f.getFieldType() + "]";
    }

    /**
     * Clones the Kafka Connect Field.
     * @param field Field
     * @param withActions true to also clone the field actions, or false
     * @return cloned
     */
    public static KafkaConnectField cloneField(KafkaConnectField field, boolean withActions) {
        KafkaConnectField clone = new KafkaConnectField();
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
                newChildren.add(cloneField((KafkaConnectField)child, true));
            }
        }
        clone.getField().addAll(newChildren);
        return clone;
    }

    /**
     * Copies the Field properties.
     * @param from from
     * @param to to
     * @param withActions true to also clone the field actions, or false
     */
    public static void copyField(Field from, Field to, boolean withActions) {
        AtlasModelFactory.copyField(from, to, withActions);

        // json specific
        if (from instanceof KafkaConnectField && to instanceof KafkaConnectField) {
            KafkaConnectField fromJson = (KafkaConnectField)from;
            KafkaConnectField toJson = (KafkaConnectField)to;
            toJson.setName(fromJson.getName());
            toJson.setPrimitive(fromJson.isPrimitive());
            toJson.setTypeName(fromJson.getTypeName());
        }
    }

}
