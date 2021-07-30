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
package io.atlasmap.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasMappingBuilder;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.DefaultAtlasSession;
import io.atlasmap.spi.AtlasConversionService;
import io.atlasmap.spi.AtlasFieldActionService;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.SimpleField;

/**
 * A base {@code AtlasMappingBuilder} with some common utility methods.
 * In most cases user can extend this class and just implement {@link #processMapping()}.
 * @see AtlasField
 */
public abstract class DefaultAtlasMappingBuilder implements AtlasMappingBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAtlasMappingBuilder.class);
    private DefaultAtlasSession session;
    private AtlasConversionService conversionService;
    private AtlasFieldActionService fieldActionService;

    public AtlasField read(String docId, String path) throws AtlasException {
        return new AtlasField(session).read(docId, path);
    }

    public AtlasField readConstant(String name) throws AtlasException {
        return new AtlasField(session).readConstant(name);
    }

    public AtlasField readProperty(String scope, String name) throws AtlasException {
        return new AtlasField(session).readProperty(scope, name);
    }

    public void write(String docId, String path, Object value) throws AtlasException {
        SimpleField source = new SimpleField();
        if (value != null) {
            source.setValue(value);
            source.setFieldType(this.conversionService.fieldTypeFromClass(value.getClass()));
        }
        new AtlasField(session).setRawField(source).write(docId, path);
    }

    @Override
    public void process() {
        try {
            processMapping();
        } catch (Exception e) {
            addAudit(e);
        }
    }

    /**
     * Define custom mapping logic. User can extend this class and implement
     * custom mapping logic in this method. The thrown Exception will be catched
     * in {@link #process()} and added as an Audit.
     * @throws Exception Indicate mapping error to be recorded as an Audit
     */
    public abstract void processMapping() throws Exception;

    @Override
    public void setAtlasSession(AtlasSession session) throws AtlasException {
        if (!(session instanceof DefaultAtlasSession)) {
            throw new IllegalArgumentException(String.format(
                    "This version of MappingBuilder doesn't support %s",
                    session.getClass().getName()));
        }
        this.session = (DefaultAtlasSession) session;
        this.conversionService = session.getAtlasContext().getContextFactory().getConversionService();
        this.fieldActionService = session.getAtlasContext().getContextFactory().getFieldActionService();
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
