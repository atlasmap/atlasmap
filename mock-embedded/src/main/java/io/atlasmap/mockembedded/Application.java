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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.core.DefaultAtlasContextFactory;

/**
 * SpringBoot application.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * The entry point.
     * @param args args
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        ConfigurableApplicationContext context = app.run(args);
        String version = DefaultAtlasContextFactory.getInstance().getProperties().get(AtlasContextFactory.PROPERTY_ATLASMAP_CORE_VERSION);
        LOG.info("### AtlasMap Data Mapper UI {} started at port: {} ###",
                version,
                context.getEnvironment().getProperty("local.server.port"));
    }

}
