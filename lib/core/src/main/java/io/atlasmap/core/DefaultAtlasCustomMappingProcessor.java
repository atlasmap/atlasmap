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
package io.atlasmap.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasMappingBuilder;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.CustomMapping;

public class DefaultAtlasCustomMappingProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasCustomMappingProcessor.class);
    private static DefaultAtlasCustomMappingProcessor instance;

    public static DefaultAtlasCustomMappingProcessor getInstance() {
        if (instance == null) {
            instance = new DefaultAtlasCustomMappingProcessor();
        }
        return instance;
    }

    public void process(DefaultAtlasSession session, CustomMapping customMapping) {
        String className = customMapping.getClassName();
        if (className == null || className.isEmpty()) {
            AtlasUtil.addAudit(session, className,
                    "Custom mapping class must be specified", AuditStatus.ERROR, className);
            return;
        }
        DefaultAtlasContextFactory factory = session.getAtlasContext().getContextFactory();
        AtlasMappingBuilder builder;
        try {
            Class<?> clazz = factory.getClassLoader().loadClass(className);
            builder = AtlasMappingBuilder.class.cast(clazz.getDeclaredConstructor().newInstance());
            builder.setAtlasSession(session);
            //AUTOMAP:we require custom mapping details inside processMapping method, so setting it in head of session similar to how other mappings are handled.
            session.head().setCustomMapping(customMapping);
        } catch (Exception e) {
            AtlasUtil.addAudit(session, className, String.format(
                    "Custom mapping class '%s' could not be loaded: %s",
                    className, e.getMessage()), AuditStatus.ERROR, className);
            if (LOG.isDebugEnabled()) {
                LOG.error("", e);
            }
            return;
        }

        builder.process();
    }

}
