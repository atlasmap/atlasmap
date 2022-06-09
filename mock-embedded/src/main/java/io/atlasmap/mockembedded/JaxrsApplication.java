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
package io.atlasmap.mockembedded;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

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

    @Override
    public Set<Object> getSingletons() {
        Set<Object> set = new HashSet<>();
        try {
            AtlasService atlasService = new AtlasService();
            set.add(atlasService);
            DocumentService documentService = new DocumentService(atlasService);
            set.add(documentService);
            set.add(new MappingService(atlasService));
            set.add(new CsvService(atlasService, documentService));
            set.add(new DfdlService(atlasService, documentService));
            set.add(new JavaService(atlasService, documentService));
            set.add(new JsonService(atlasService, documentService));
            set.add(new KafkaConnectService(atlasService, documentService));
            set.add(new XmlService(atlasService, documentService));
        } catch (AtlasException e) {
            throw new IllegalStateException(e);
        }
        return set;
    }
}
