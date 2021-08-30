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

public class AtlasKafkaConnectModelFactory {

    public static final String URI_FORMAT = "atlas:kafkaconnect";

    public static KafkaConnectDocument createKafkaConnectDocument() {
        KafkaConnectDocument kafkaConnectDocument = new KafkaConnectDocument();
        kafkaConnectDocument.setFields(new Fields());
        return kafkaConnectDocument;
    }

    public static KafkaConnectField createKafkaConnectField() {
        KafkaConnectField kafkaConnectField = new KafkaConnectField();
        return kafkaConnectField;
    }

    public static KafkaConnectComplexType createKafkaConnectComplexType() {
        KafkaConnectComplexType kafkaConnectComplexField = new KafkaConnectComplexType();
        kafkaConnectComplexField.setKafkaConnectFields(new KafkaConnectFields());
        kafkaConnectComplexField.setFieldType(FieldType.COMPLEX);
        return kafkaConnectComplexField;
    }

    public static String toString(KafkaConnectField f) {
        return "JsonField [name=" + f.getName() + ", primitive=" + f.isPrimitive() + ", typeName=" + f.getTypeName()
                + ", actions=" + f.getActions() + ", value=" + f.getValue()
                + ", arrayDimensions=" + f.getArrayDimensions() + ", arraySize=" + f.getArraySize()
                + ", collectionType=" + f.getCollectionType() + ", docId=" + f.getDocId() + ", index=" + f.getIndex()
                + ", path=" + f.getPath() + ", required=" + f.isRequired() + ", status=" + f.getStatus()
                + ", fieldType=" + f.getFieldType() + "]";
    }

    public static KafkaConnectField cloneField(KafkaConnectField field, boolean withActions) {
        KafkaConnectField clone = new KafkaConnectField();
        copyField(field, clone, withActions);
        return clone;
    }

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
