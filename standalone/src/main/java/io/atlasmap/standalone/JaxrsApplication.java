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
package io.atlasmap.standalone;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.atlasmap.api.AtlasException;
import io.atlasmap.csv.service.CsvService;
import io.atlasmap.dfdl.service.DfdlService;
import io.atlasmap.java.service.JavaService;
import io.atlasmap.json.service.JsonService;
import io.atlasmap.kafkaconnect.service.KafkaConnectService;
import io.atlasmap.service.AtlasService;
import io.atlasmap.service.DocumentService;
import io.atlasmap.service.MappingService;
import io.atlasmap.xml.service.XmlService;

/**
 * JAX-RS application.
 */
@Component
@ApplicationPath("/v2/atlas/")
public class JaxrsApplication extends Application {

    /**
     * Core service.
     * @return Core service
     * @throws AtlasException unexpected error
     */
    @Bean
    public AtlasService atlasService() throws AtlasException {
        return new AtlasService();
    }

    /**
     * Mapping service.
     * @return Mapping service
     * @throws AtlasException unexpected error
     */
    @Bean
    public MappingService mappingService() throws AtlasException {
        return new MappingService(atlasService());
    }

    /**
     * Document service.
     * @return Document service
     * @throws AtlasException unexpected error
     */
    @Bean
    public DocumentService documentService() throws AtlasException {
        return new DocumentService(atlasService());
    }

    /**
     * Java service.
     * @return Java service
     */
    @Bean
    public JavaService javaService() {
        return new JavaService();
    }

    /**
     * JSON service.
     * @return JSON service
     */
    @Bean
    public JsonService jsonService() {
        return new JsonService();
    }

    /**
     * XML service.
     * @return XML service
     */
    @Bean
    public XmlService xmlService() {
        return new XmlService();
    }

    /**
     * DFDL service.
     * @return DFDL service.
     */
    @Bean
    public DfdlService dfdlService() {
        return new DfdlService();
    }

    /**
     * CSV service.
     * @return CSV service.
     */
    @Bean
    public CsvService csvService() { return new CsvService(); }

    /**
     * Kafka Connect service.
     * @return Kafka Connect service.
     */
    @Bean
    public KafkaConnectService kafkaConnectService() { return new KafkaConnectService(); }
    
}
