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
package io.atlasmap.kafka.smt;

import java.io.File;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContext;
import io.atlasmap.core.DefaultAtlasContextFactory;

public class AtlasMapSMT<R extends ConnectRecord<R>> implements Transformation<R> {

    public static final String OVERVIEW_DOC = "Process AtlasMap data mapping with a Kafka Connect record";

    private interface ConfigName {
        String ADM_PATH = "adm.path";
        String DOCID_SOURCE_KEY = "docid.source.key";
        String DOCID_SOURCE_VALUE = "docid.source.value";
        String DOCID_TARGET_KEY = "docid.target.key";
        String DOCID_TARGET_VALUE = "docid.target.value";
    }

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(ConfigName.ADM_PATH, ConfigDef.Type.STRING, "", ConfigDef.Importance.HIGH, "Path for the ADM file")
        .define(ConfigName.DOCID_SOURCE_KEY, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, "Document ID for the source key")
        .define(ConfigName.DOCID_SOURCE_VALUE, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, "Document ID for the source value")
        .define(ConfigName.DOCID_TARGET_KEY, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, "Document ID for the target key")
        .define(ConfigName.DOCID_TARGET_VALUE, ConfigDef.Type.STRING, "", ConfigDef.Importance.MEDIUM, "Document ID for the target value");

    private static final Logger LOG = LoggerFactory.getLogger(AtlasMapSMT.class);

    private String admPath;
    private String docIdSourceKey;
    private String docIdSourceValue;
    private String docIdTargetKey;
    private String docIdTargetValue;
    private DefaultAtlasContext atlasContext;

    @Override
    public void configure(Map<String, ?> props) {
        final SimpleConfig config = new SimpleConfig(CONFIG_DEF, props);
        admPath = config.getString(ConfigName.ADM_PATH);
        docIdSourceKey = config.getString(ConfigName.DOCID_SOURCE_KEY);
        docIdSourceValue = config.getString(ConfigName.DOCID_SOURCE_VALUE);
        docIdTargetKey = config.getString(ConfigName.DOCID_TARGET_KEY);
        docIdTargetValue = config.getString(ConfigName.DOCID_TARGET_VALUE);

        try {
            atlasContext = DefaultAtlasContextFactory.getInstance().createContext(new File(admPath));
        } catch (Exception e) {
            LOG.error("Could not load ADM archive file: {}", e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
        }
    }

    @Override
    public R apply(R record) {
        try {
            AtlasSession session = atlasContext.createSession();
            if (docIdSourceKey != null && !docIdSourceKey.isEmpty()) {
                session.setSourceDocument(docIdSourceKey, record.key());
            }
            if (docIdSourceValue != null && !docIdSourceValue.isEmpty()) {
                session.setSourceDocument(docIdSourceValue, record.value());
            } else {
                session.setDefaultSourceDocument(record.value());
            }
            atlasContext.process(session);
            Object outKey = null, outValue = null;
            if (docIdTargetKey != null && !docIdTargetKey.isEmpty()) {
                outKey = session.getTargetDocument(docIdTargetKey);
            }
            if (docIdTargetValue != null && !docIdTargetValue.isEmpty()) {
                outValue = session.getTargetDocument(docIdTargetValue);
            } else {
                outValue = session.getDefaultTargetDocument();
            }
            return record.newRecord(record.topic(), record.kafkaPartition(),
                null, outKey, null, outValue, record.timestamp());
        } catch (Exception e) {
            LOG.error("Could not process AtlasMap mapping: {}", e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
            return record;
        }
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public void close() {
    }

}
