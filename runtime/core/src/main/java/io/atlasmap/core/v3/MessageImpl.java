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

import io.atlasmap.api.v3.Message;
import io.atlasmap.api.v3.MessageStatus;
import io.atlasmap.spi.v3.util.I18n;

/**
 * A localized message
 */
public class MessageImpl implements Message {

    private final MessageStatus status;
    private final String message;

    public MessageImpl(MessageStatus status, String message, Object... arguments) {
        this.status = status;
        this.message = I18n.localize(message, arguments);
    }

    /**
     * @see io.atlasmap.api.v3.Message#message()
     */
    @Override
    public String message() {
        return message;
    }

    /**
     * @see io.atlasmap.api.v3.Message#status()
     */
    @Override
    public MessageStatus status() {
        return status;
    }
}
