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

import static net.mguenther.kafka.junit.EmbeddedConnectConfig.kafkaConnect;
import static net.mguenther.kafka.junit.EmbeddedKafkaCluster.provisionWith;
import static net.mguenther.kafka.junit.EmbeddedKafkaClusterConfig.newClusterConfig;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import net.mguenther.kafka.junit.EmbeddedConnectConfig.EmbeddedConnectConfigBuilder;
import net.mguenther.kafka.junit.EmbeddedKafkaCluster;
import net.mguenther.kafka.junit.EmbeddedKafkaClusterConfig.EmbeddedKafkaClusterConfigBuilder;

public class AtlasMapSMTTest {

    private EmbeddedKafkaCluster kafka;

    @BeforeEach
    void before() {
        Properties source = new Properties();
        source.put("name", "source-connector");
        source.put("connector.class", "FileStreamSource");
        source.put("file", "src/test/resources/source.json");
        source.put("topic", "topic");
        source.put("topics", "topic");
        source.put("tasks.max", 1);
        source.put("transforms", "atlasmap");
        source.put("transforms.atlasmap.type", "io.atlasmap.kafka.smt.AtlasMapSMT");
        source.put("transforms.atlasmap.adm.path", "src/test/resources/atlasmap-mapping.adm");
        source.put("transforms.atlasmap.docid.source.value", "source-c7ee0c6f-d615-4d53-9563-d91750745cf9");
        source.put("transforms.atlasmap.docid.target.value", "target-4e4d7eb5-8dff-4de8-9780-0707a07a9bad");
        Properties sink = new Properties();
        sink.put("name", "sink-connector-value");
        sink.put("connector.class", "FileStreamSink");
        sink.put("file", "target/target.xml");
        sink.put("topic", "topic");
        sink.put("topics", "topic");
        sink.put("tasks.max", 1);
        EmbeddedConnectConfigBuilder connect = kafkaConnect()
                .deployConnector(source).deployConnector(sink);
        EmbeddedKafkaClusterConfigBuilder cluster = newClusterConfig().configure(connect);
        kafka = provisionWith(cluster);
        kafka.start();
    }

    @AfterEach
    void after() {
        kafka.stop();
    }

    @Test
    public void test() throws Exception {
        File f = new File("target/target.xml");
        for(int i=0; !f.exists() && i < 5; i++) {
            Thread.sleep(1000);
            f = new File("target/target.xml");
        }
        assertTrue(f.exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line = reader.readLine();
            for (int i=0; line == null && i < 5; i++) {
                Thread.sleep(1000);
                line = reader.readLine();
            }
            assertNotNull(line);
            Diff d = DiffBuilder.compare(Input.fromFile("src/test/resources/target.xml").build())
                .withTest(Input.fromString(line).build())
                .ignoreWhitespace().build();
            assertFalse(d.hasDifferences(), d.toString() + ": " + line);
        }
    }

}
