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

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.connect.data.Schema.Type;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonConverterConfig;
import org.apache.kafka.connect.storage.StringConverterConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.apicurio.registry.serde.avro.AvroSchemaUtils;
import io.apicurio.registry.utils.converter.avro.AvroData;
import io.atlasmap.kafkaconnect.v2.KafkaConnectConstants;
import io.atlasmap.v2.FieldType;

/**
 * A collection of utility methods for Kafka Connect module.
 */
public class KafkaConnectUtil {

    /**
     * Converts Kafka Connect from {@link Type} to AtlasMap {@link FieldType}.
     * @param type type
     * @return FieldType
     */
    public static FieldType getFieldType(Type type) {
        switch (type) {
            case ARRAY:
                return FieldType.COMPLEX;
            case BOOLEAN:
                return FieldType.BOOLEAN;
            case BYTES:
                return FieldType.BYTE_ARRAY;
            case FLOAT32:
                return FieldType.FLOAT;
            case FLOAT64:
                return FieldType.DOUBLE;
            case INT16:
                return FieldType.SHORT;
            case INT32:
                return FieldType.INTEGER;
            case INT64:
                return FieldType.LONG;
            case INT8:
                return FieldType.BYTE;
            case MAP:
                return FieldType.COMPLEX;
            case STRING:
                return FieldType.STRING;
            case STRUCT:
                return FieldType.COMPLEX;
            default:
                return FieldType.UNSUPPORTED;

        }
    }

    /**
     * Parses the Kafka Connect JSON schema and returns the {@link org.apache.kafka.connect.data.Schema}.
     * @param jsonSchema Kafka Connect JSON schema
     * @param options passed into the {@link JsonConverter#configure(Map)}
     * @return Kafka Connect schema
     * @throws Exception unexpected error
     * @see JsonConverter
     */
    public static org.apache.kafka.connect.data.Schema parseJson(String jsonSchema, HashMap<String, ?> options) throws Exception {
        try (JsonConverter converter = new JsonConverter()) {
            converter.configure(options);
            JsonNode parsed = new ObjectMapper().readTree(jsonSchema);
            return converter.asConnectSchema(parsed);
        }
    }

    /**
     * Parses the Kafka Connect AVRO schema and returns the {@link org.apache.kafka.connect.data.Schema}.
     * @param avroSchema Kafka Connect AVRO schema
     * @param options unused
     * @return Kafka Connect schema
     * @throws Exception unexpected error
     * @see AvroSchemaUtils
     * @see AvroData
     */
    public static org.apache.kafka.connect.data.Schema parseAvro(String avroSchema, HashMap<String, ?> options) throws Exception {
        org.apache.avro.Schema schema = AvroSchemaUtils.parse(avroSchema);
        AvroData avro = new AvroData(0);
        return avro.toConnectSchema(schema);
    }

    /**
     * Repack KafkaConnect parser options from inspection request options.
     * @param requestOptions inspection request options
     * @return KafkaConnect parser options
     */
    public static HashMap<String, Object> repackParserOptions(Map<String, String> requestOptions) {
        HashMap<String, Object> options = new HashMap<>();
        String isKeyStr = requestOptions.get(KafkaConnectConstants.OPTIONS_IS_KEY);
        boolean isKey = isKeyStr != null && Boolean.parseBoolean(isKeyStr) ? true : false;
        String cacheSizeStr = requestOptions.get(KafkaConnectConstants.OPTIONS_SCHEMA_CACHE_SIZE);
        options.put(JsonConverterConfig.SCHEMAS_CACHE_SIZE_CONFIG,
            cacheSizeStr != null ? Integer.parseInt(cacheSizeStr) : Integer.valueOf(0));
        options.put(StringConverterConfig.TYPE_CONFIG, isKey ? "key" : "value");
        return options;
    }
}
