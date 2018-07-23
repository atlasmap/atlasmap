/**
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.atlasmap.core.v3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.v3.Mapping;
import io.atlasmap.api.v3.Message;
import io.atlasmap.api.v3.MessageStatus;
import io.atlasmap.api.v3.Parameter;
import io.atlasmap.api.v3.Scope;
import io.atlasmap.api.v3.Session;
import io.atlasmap.api.v3.Transformation;
import io.atlasmap.api.v3.Validation;

/**
 *
 */
public class SessionImpl implements Session {

    private static final Logger LOG = LoggerFactory.getLogger(SessionImpl.class);

    private Context context;
    private List<Message> executionMessages = new ArrayList<>();
    private List<Validation> validations = new ArrayList<>();

    public SessionImpl(Context context) {
        this.context = context;
    }

    /**
     * @see io.atlasmap.api.v3.Session#validateMappings()
     */
    @Override
    public void validateMappings() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Begin validate mapping for {}", this);
        }

        for (Mapping mapping : context.mappingDocument.mappings()) {
            for (Transformation transformation : mapping.transformations()) {
                for (Parameter parameter : transformation.parameters()) {
                    if (parameter.valueRequired() && parameter.value() == null) {
                        new ValidationImpl(Scope.TRANSFORMATION, parameter, MessageStatus.ERROR,
                                           "A value is required for the %s parameter of the %s transformation in mapping %s.",
                                           parameter.name(), transformation.name(), mapping.name());
                    }
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("End validate mapping for {}", this);
        }
    }

    /**
     * @see io.atlasmap.api.v3.Session#validations()
     */
    @Override
    public List<Validation> validations() {
        return validations;
    }

    /**
     * @see io.atlasmap.api.v3.Session#executeMappings()
     */
    @Override
    public void executeMappings() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Begin execute mapping for {}", this);
        }

        // Validate
        validations.clear();
        executionMessages.clear();
        validateMappings();
        int errors = 0;
        for (Validation msg : validations) {
            executionMessages.add(msg);
            if (msg.status() == MessageStatus.ERROR) {
                errors++;
            }
        }
        if (errors > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.error("Aborting due to {} validation errors", errors);
            }
            return;
        }

        for (Mapping mapping : context.mappingDocument.mappings()) {
            for (Transformation transformation : mapping.transformations()) {
                for (Parameter parameter : transformation.parameters()) {
                    if (parameter.valueRequired() && parameter.value() == null) {
                        new ValidationImpl(Scope.TRANSFORMATION, parameter, MessageStatus.ERROR,
                                           "A value is required for the %s parameter of the %s transformation in mapping %s.",
                                           parameter.name(), transformation.name(), mapping.name());
                    }
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("End execute mapping for {}", this);
        }
    }

    /**
     * @see io.atlasmap.api.v3.Session#executionMessages()
     */
    @Override
    public List<Message> executionMessages() {
        return Collections.unmodifiableList(executionMessages);
    }
}
