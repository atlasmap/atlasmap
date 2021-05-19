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

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasPropertyStrategy;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.PropertyField;

public class PropertyModule extends BaseAtlasModule {
    private static final Logger LOG = LoggerFactory.getLogger(PropertyModule.class);

    private AtlasPropertyStrategy defaultStrategy;

    public PropertyModule(AtlasPropertyStrategy propertyStrategy) {
        this.defaultStrategy = propertyStrategy;
    }

    @Override
    public void processPreValidation(AtlasInternalSession session) throws AtlasException {
        // TODO
    }

    @Override
    public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
        // no-op
    }

    @Override
    public void readSourceValue(AtlasInternalSession session) throws AtlasException {
        AtlasPropertyStrategy strategy = session.getAtlasPropertyStrategy() != null
                ? session.getAtlasPropertyStrategy() : this.defaultStrategy;
        Field sourceField = session.head().getSourceField();
        if (sourceField instanceof PropertyField) {
            PropertyField sourcePropertyField = (PropertyField)sourceField;
            strategy.readProperty(session, sourcePropertyField);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processed source PropertyField: Name={} Scope={} Value={} Strategy={}",
                        sourcePropertyField.getName(), sourcePropertyField.getScope(),
                        sourceField.getValue(), strategy.getClass().getName());
            }
        }
    }

    @Override
    public void processPostSourceExecution(AtlasInternalSession session) throws AtlasException {
        // no-op
    }

    @Override
    public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
        // no-op
    }

    @Override
    public void writeTargetValue(AtlasInternalSession session) throws AtlasException {
        AtlasPropertyStrategy strategy = session.getAtlasPropertyStrategy() != null
                ? session.getAtlasPropertyStrategy() : this.defaultStrategy;
        Field targetField = session.head().getTargetField();
        if (targetField instanceof PropertyField) {
            PropertyField targetPropertyField = (PropertyField)targetField;
            strategy.writeProperty(session, targetPropertyField);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processed target PropertyField: Name={} Value={} Strategy={}",
                        targetPropertyField.getName(), targetPropertyField.getValue(),
                        strategy.getClass().getName());
            }
        }
    }

    @Override
    public void processPostTargetExecution(AtlasInternalSession session) throws AtlasException {
        // no-op
    }

    @Override
    public void processPostValidation(AtlasInternalSession session) throws AtlasException {
        // no-op
    }

    @Override
    public Boolean isSupportedField(Field field) {
        return field instanceof PropertyField;
    }

    @Override
    public PropertyField cloneField(Field field) throws AtlasException {
        if (field == null || !(field instanceof PropertyField)) {
            return null;
        }
        PropertyField orig = (PropertyField)field;
        PropertyField clone = new PropertyField();
        AtlasModelFactory.copyField(orig, clone, true);
        clone.setScope(orig.getScope());
        return clone;
    }

    @Override
    public void setDocName(String docName) {
    }

    @Override
    public String getDocName() {
        return "Properties";
    }

    @Override
    public PropertyField createField() {
        return new PropertyField();
    }

}
