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

/**
 * A collection of constants for Kafka Connect module.
 */
public final class KafkaConnectConstants {
    /** The key for schemaType option. */
    public static final String OPTIONS_SCHEMA_TYPE = "schemaType";
    /** The key for isKey option. */
    public static final String OPTIONS_IS_KEY = "isKey";
    /** The key for schemaCacheSize option. */
    public static final Object OPTIONS_SCHEMA_CACHE_SIZE = "schemaCacheSize";

}
