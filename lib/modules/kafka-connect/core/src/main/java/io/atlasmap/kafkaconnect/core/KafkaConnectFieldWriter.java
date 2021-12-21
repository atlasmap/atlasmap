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
package io.atlasmap.kafkaconnect.core;

import java.util.LinkedList;
import java.util.List;

import org.apache.kafka.connect.data.ConnectSchema;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasPath.SegmentContext;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldWriter;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;

/**
 * The {@link AtlasFieldWriter} implementation for Kafka Connect.
 */
public class KafkaConnectFieldWriter implements AtlasFieldWriter {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(KafkaConnectFieldWriter.class);

    private AtlasConversionService conversionService;
    private Object root = null;
    private org.apache.kafka.connect.data.Schema schema;

    /**
     * A constructor.
     * @param conversion conversion service
     */
    public KafkaConnectFieldWriter(AtlasConversionService conversion) {
        this.conversionService = conversion;
    }

    /**
     * Gets the Document.
     * @return Document
     */
    public Object getDocument() {
        return root;
    }

    /**
     * Sets the schema.
     * @param schema schema
     */
    public void setSchema(org.apache.kafka.connect.data.Schema schema) {
        this.schema = schema;
    }

    @Override
    public void write(AtlasInternalSession session) throws AtlasException {
        if (this.schema == null) {
            throw new AtlasException("Kafka Connect schema must be set to write Kafka Connect object");
        }
        Field targetField = session.head().getTargetField();
        if (targetField == null) {
            throw new AtlasException(new IllegalArgumentException("Target field cannot be null"));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Field: " + AtlasModelFactory.toString(targetField));
            LOG.debug("Field type=" + targetField.getFieldType() + " path=" + targetField.getPath() + " v="
                    + targetField.getValue());
        }

        AtlasPath path = new AtlasPath(targetField.getPath());
        this.root = writeSegment(path, 0, root, schema, targetField);
    }

    private Object writeSegment(AtlasPath path, int index, Object parent, Schema parentSchema, Field field) throws AtlasException {
        Object answer;
        Schema schema;
        List<SegmentContext> segments = path.getSegments(true);
        SegmentContext segment = segments.get(index);
        // terminal field
        if (segments.size() == index + 1) {
            if (segment.getCollectionType() != CollectionType.NONE) {
                List<Object> collection = new LinkedList<Object>();
                if (parent == null) {
                    answer = collection;
                } else {
                    Object array = ((Struct)parent).get(segment.getName());
                    if (array == null) {
                        ((Struct)parent).put(segment.getName(), collection);
                    } else {
                        collection = (List<Object>) array;
                    }
                    answer = parent;
                }
                int pos = segment.getCollectionIndex();
                while (collection.size() < pos + 1) {
                    collection.add(null);
                }
                collection.set(pos, field.getValue());
            } else {
                if (parent == null) {
                    answer = field.getValue();
                } else {
                    ((Struct)parent).put(segment.getName(), field.getValue());
                    answer = parent;
                }
            }
            return answer;
        }

        // non-terminal field

        // non-collection segment
        if (segment.getCollectionType() == null || segment.getCollectionType() == CollectionType.NONE) {
            if (index == 0) {
                answer = root != null ? root : new Struct(enforceDefaultOrOptional(parentSchema));
                schema = parentSchema;
            } else {
                schema = parentSchema.field(segment.getName()).schema();
                if (((Struct)parent).getStruct(segment.getName()) == null) {
                    ((Struct)parent).put(segment.getName(), new Struct(schema));
                }
                answer = ((Struct)parent).getStruct(segment.getName());
            }
            writeSegment(path, index + 1, answer, schema, field);
            return answer;
        }

        // collection segment
        List<Object> array;
        if (index == 0) {
            array = root != null ? (List<Object>)root : new LinkedList<Object>();
            schema = enforceDefaultOrOptional(parentSchema.valueSchema());
        } else {
            schema = parentSchema.field(segment.getName()).schema().valueSchema();
            array = ((Struct) parent).getArray(segment.getName());
            if (array == null) {
                array = new LinkedList<>();
                ((Struct) parent).put(segment.getName(), array);
            }
        }
        int pos = segment.getCollectionIndex();
        while (array.size() < pos + 1) {
            array.add(null);
        }
        if (array.get(pos) == null) {
            array.set(pos, new Struct(schema));
        }
        writeSegment(path, index + 1, array.get(pos), schema, field);
        return array;
    }

    /**
     * Dirty hack. In current Kafka Connect implementation, <code>Struct#put()</code> throws an Exception
     * if non-optional child doesn't have a value.
     * @param schema
     * @return modified schema
     */
    private Schema enforceDefaultOrOptional(Schema schema) {
        if (!schema.isOptional() && schema.defaultValue() == null) {
            LOG.warn("Enforcing schema to be optional since there is no default value. "
                + "It is recommended to either 1) define a default value or 2) set it optional in the Kafka Connect schema.");
            try {
                java.lang.reflect.Field optionalField = ConnectSchema.class.getDeclaredField("optional");
                optionalField.setAccessible(true);
                optionalField.setBoolean(schema, true);
            } catch (Exception e) {
                LOG.warn("", e);
            }
        }
        switch (schema.type()) {
            case ARRAY:
                enforceDefaultOrOptional(schema.valueSchema());
                break;
            case STRUCT:
                schema.fields().forEach(f -> enforceDefaultOrOptional(f.schema()));
                break;
            case MAP:
                enforceDefaultOrOptional(schema.keySchema());
                enforceDefaultOrOptional(schema.valueSchema());
                break;
            default:
        }
        return schema;
    }
}
