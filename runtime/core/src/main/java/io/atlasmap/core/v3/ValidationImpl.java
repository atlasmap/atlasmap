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

import io.atlasmap.api.v3.MessageStatus;
import io.atlasmap.api.v3.Scope;
import io.atlasmap.api.v3.Validation;

/**
 *
 */
public class ValidationImpl extends MessageImpl implements Validation {

    private final Scope scope;
    private final Object context;

    public ValidationImpl(Scope scope, Object context, MessageStatus status, String message, Object... arguments) {
        super(status, message, arguments);
        this.scope = scope;
        this.context = context;
    }

    /**
     * @see io.atlasmap.api.v3.Validation#scope()
     */
    @Override
    public Scope scope() {
        return scope;
    }

    /**
     * @see io.atlasmap.api.v3.Validation#context()
     */
    @Override
    public Object context() {
        return context;
    }
}
