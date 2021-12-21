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
package io.atlasmap.api;

/**
 * The Exception indicates that unsupported operation is performed.
 */
public class AtlasUnsupportedException extends AtlasException {

    private static final long serialVersionUID = 4276166328541103662L;

    /**
     * A constructor.
     */
    public AtlasUnsupportedException() {
        super();
    }

    /**
     * A constructor.
     * @param message message
     * @param cause cause
     */
    public AtlasUnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * A constructor.
     * @param message message
     */
    public AtlasUnsupportedException(String message) {
        super(message);
    }

    /**
     * A constructor.
     * @param cause cause
     */
    public AtlasUnsupportedException(Throwable cause) {
        super(cause);
    }
}
