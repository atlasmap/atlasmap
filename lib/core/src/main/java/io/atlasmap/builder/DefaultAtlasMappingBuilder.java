/**
 * Copyright (C) 2021 Red Hat, Inc.
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
package io.atlasmap.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasMappingBuilder;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.DefaultAtlasSession;
import io.atlasmap.v2.AuditStatus;

/**
 * A base {@code AtlasMappingBuilder} with some common utility methods.
 * In most cases user can extend this class and just implement {@link #processMapping()}.
 * @see AtlasField
 */
public abstract class DefaultAtlasMappingBuilder implements AtlasMappingBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasMappingBuilder.class);
    private DefaultAtlasSession session;

    public AtlasField read(String docId, String path) throws AtlasException {
        return new AtlasField(session).read(docId, path);
    }

    public AtlasField readConstant(String name) throws AtlasException {
        return new AtlasField(session).readConstant(name);
    }

    public AtlasField readProperty(String scope, String name) throws AtlasException {
        return new AtlasField(session).readProperty(scope, name);
    }

    @Override
    public void setAtlasSession(AtlasSession session) {
        if (!(session instanceof DefaultAtlasSession)) {
            throw new IllegalArgumentException(String.format(
                    "This version of MappingBuilder doesn't support %s",
                    session.getClass().getName()));
        }
        this.session = (DefaultAtlasSession) session;
    };

    /**
     * Get {@code DefaultAtlasSession}.
     * @return {@code AtlasSession}.
     */
    public AtlasSession getAtlasSession() {
        return this.session;
    };

    public void addAudit(Exception e) {
        AtlasUtil.addAudit(this.session, this.getClass().getName(),
                e.getMessage(), AuditStatus.ERROR, null);
        if (LOG.isDebugEnabled()) {
            LOG.error("", e);
        }
    }

}
