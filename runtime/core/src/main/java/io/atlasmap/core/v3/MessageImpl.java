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

import java.lang.reflect.Field;

import io.atlasmap.api.v3.MappingDocument;
import io.atlasmap.api.v3.Message;
import io.atlasmap.spi.v3.util.I18n;

/**
 * A localized message
 */
public class MessageImpl implements Message {

    private final Status status;
    private final Scope scope;
    private final Object context;
    private final String message;

    public MessageImpl(Status status, Scope scope, Object context, String message, Object... arguments) {
        this.status = status;
        this.scope = scope;
        this.context = context;
        this.message = I18n.localize(message, arguments);
    }

    /**
     * @see Message#status()
     */
    @Override
    public Status status() {
        return status;
    }

    /**
     * @see Message#scope()
     */
    @Override
    public Scope scope() {
        return scope;
    }

    /**
     * @see Message#context()
     */
    @Override
    public Object context() {
        return context;
    }

    /**
     * @see Message#message()
     */
    @Override
    public String message() {
        return message;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String contextName;
        Field field;
        try {
            field = context.getClass().getDeclaredField("name");
            field.setAccessible(true);
            contextName = field.get(context).toString();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ignored) {
            contextName = context.getClass().getName();
        }
        String contextClassName = null;
        for (Class<?> interfaceClass : context.getClass().getInterfaces()) {
            if (interfaceClass.getPackage() == MappingDocument.class.getPackage()) {
                contextClassName = interfaceClass.getSimpleName().toLowerCase();
                break;
            }
        }
        if (contextClassName == null) {
            contextClassName = context.getClass().getName();
        }
        String scopeName = scope.toString().toLowerCase().replace('_', ' ');
        scopeName = Character.toUpperCase(scopeName.charAt(0)) + scopeName.substring(1);
        return Message.class.getSimpleName() + ": " + scopeName + " " + status + " regarding the " + contextName + " " + contextClassName + ": " + message;
    }
}
