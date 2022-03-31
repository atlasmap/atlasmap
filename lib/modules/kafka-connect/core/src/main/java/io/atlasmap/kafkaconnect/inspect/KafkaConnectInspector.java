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
package io.atlasmap.kafkaconnect.inspect;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Schema.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.kafkaconnect.core.KafkaConnectUtil;
import io.atlasmap.kafkaconnect.v2.AtlasKafkaConnectModelFactory;
import io.atlasmap.kafkaconnect.v2.KafkaConnectComplexType;
import io.atlasmap.kafkaconnect.v2.KafkaConnectDocument;
import io.atlasmap.kafkaconnect.v2.KafkaConnectEnumField;
import io.atlasmap.kafkaconnect.v2.KafkaConnectEnumFields;
import io.atlasmap.kafkaconnect.v2.KafkaConnectField;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

/**
 * Inspects Kafka Connect schema.
 */
public class KafkaConnectInspector {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConnectInspector.class);

    private ClassLoader classLoader;
    private KafkaConnectDocument output;

    /**
     * A constructor.
     * @param loader class loader
     */
    public KafkaConnectInspector(ClassLoader loader) {
        this.classLoader = loader;
    }

    /**
     * Inspects Kafka Connect JSON schema.
     * @param jsonSchema Kafka Connect JSON schema
     * @param options inspection options
     * @throws AtlasException unexpected error
     */
    public void inspectJson(InputStream jsonSchema, HashMap<String, ?> options) throws AtlasException {
        try {
            org.apache.kafka.connect.data.Schema connectSchema = KafkaConnectUtil.parseJson(jsonSchema, options);
            this.output = createDocument(connectSchema);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    /**
     * Inspects Kafka Connect AVRO schema.
     * @param avroSchema Kafka Connect AVRO schema
     * @param options inspection options
     * @throws AtlasException unexpected error
     */
    public void inspectAvro(InputStream avroSchema, HashMap<String, ?> options) throws AtlasException {
        try {
            org.apache.kafka.connect.data.Schema connectSchema = KafkaConnectUtil.parseAvro(avroSchema, options);
            this.output = createDocument(connectSchema);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    /**
     * Gets the result Document of Kafka Connect schema inspection.
     * @return Document
     */
    public KafkaConnectDocument getKafkaConnectDocument() {
        return this.output;
    }

    private KafkaConnectDocument createDocument(org.apache.kafka.connect.data.Schema schema) {
        KafkaConnectDocument doc = AtlasKafkaConnectModelFactory.createKafkaConnectDocument();
        doc.setRootSchemaType(schema.type());
        doc.setName(schema.name());
        Schema connectSchema = schema;
        AtlasPath path;
        if (Type.ARRAY == connectSchema.type()) {
            path = new AtlasPath(AtlasPath.PATH_SEPARATOR + AtlasPath.PATH_LIST_SUFFIX);
            doc.setCollectionType(CollectionType.LIST);
            connectSchema = connectSchema.valueSchema();
        } else if (Type.MAP == connectSchema.type()) {
            path = new AtlasPath(AtlasPath.PATH_SEPARATOR + AtlasPath.PATH_MAP_SUFFIX);
            doc.setCollectionType(CollectionType.MAP);
            connectSchema = connectSchema.valueSchema();
        } else {
            path = new AtlasPath("");
        }

        doc.setPath(path.toString());
        if (connectSchema.parameters() != null) {
            doc.setEnumeration(true);
            List<KafkaConnectEnumField> symbols = doc.getEnumFields().getKafkaConnectEnumField();
            for (Entry<String,String> entry : connectSchema.parameters().entrySet()) {
                if ("io.confluent".equals(entry.getKey())) {
                    continue;
                }
                KafkaConnectEnumField f = new KafkaConnectEnumField();
                f.setName(entry.getValue());
                symbols.add(f);
            }
            doc.setFieldType(KafkaConnectUtil.getFieldType(connectSchema.type()));
        } else if (!connectSchema.type().isPrimitive()) {
            doc.setFieldType(FieldType.COMPLEX);
            List<KafkaConnectField> children = populateFields(connectSchema.fields(), path);
            doc.getFields().getField().addAll(children);
        } else {
            doc.setFieldType(KafkaConnectUtil.getFieldType(connectSchema.type()));
        }
        return doc;
    }

    private List<KafkaConnectField> populateFields(List<org.apache.kafka.connect.data.Field> kcFields, AtlasPath parentPath) {
        List<KafkaConnectField> answer = new ArrayList<>();
        for (org.apache.kafka.connect.data.Field connectField : kcFields) {
            KafkaConnectField field;
            AtlasPath path = parentPath.clone();
            CollectionType collectionType = CollectionType.NONE;
            Schema connectSchema = connectField.schema();
            if (Type.ARRAY == connectSchema.type()) {
                path.appendField(connectField.name() + AtlasPath.PATH_LIST_SUFFIX);
                collectionType = CollectionType.LIST;
                connectSchema = connectSchema.valueSchema();
            } else if (Type.MAP == connectSchema.type()) {
                path.appendField(connectField.name() + AtlasPath.PATH_MAP_SUFFIX);
                collectionType = CollectionType.MAP;
                connectSchema = connectSchema.valueSchema();
            } else {
                path.appendField(connectField.name());
            }

            if (connectSchema.parameters() != null) {
                KafkaConnectComplexType complex = AtlasKafkaConnectModelFactory.createKafkaConnectComplexType();
                complex.setKafkaConnectEnumFields(new KafkaConnectEnumFields());
                complex.setEnumeration(true);
                List<KafkaConnectEnumField> symbols = complex.getKafkaConnectEnumFields().getKafkaConnectEnumField();
                boolean first = true;
                for (Entry<String,String> entry : connectSchema.parameters().entrySet()) {
                    // FIXME dirty hack - it seems the first entry is for some control, the others are the actual values
                    if (first) {
                        first = false;
                        continue;
                    }
                    KafkaConnectEnumField f = new KafkaConnectEnumField();
                    f.setName(entry.getValue());
                    symbols.add(f);
                }
                field = complex;
            } else if (connectSchema.type().isPrimitive()) {
                field = AtlasKafkaConnectModelFactory.createKafkaConnectField();
                field.setFieldType(KafkaConnectUtil.getFieldType(connectSchema.type()));
            } else {
                KafkaConnectComplexType complex = AtlasKafkaConnectModelFactory.createKafkaConnectComplexType();
                List<KafkaConnectField> children = populateFields(connectSchema.fields(), path);
                if ("io.confluent.connect.avro.Union".equals(connectSchema.name())) {
                    // We don't support union until it's built into Kafka Connect Schema
                    complex.setStatus(FieldStatus.UNSUPPORTED);
                }
                complex.getKafkaConnectFields().getKafkaConnectField().addAll(children);
                field = complex;
            }
            field.setName(connectField.name());
            field.setPath(path.toString());
            field.setCollectionType(collectionType);
            answer.add(field);
        }
        return answer;
    }
}
