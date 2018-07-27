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

import io.atlasmap.api.v3.DocumentRole;
import io.atlasmap.spi.v3.DataHandler;
import io.atlasmap.spi.v3.util.AtlasRuntimeException;
import io.atlasmap.spi.v3.util.VerifyArgument;

/**
 *
 */
public class TestHandler extends DataHandler {

    /**
     * @see DataHandler#supportedDataFormats()
     */
    @Override
    public String[] supportedDataFormats() {
        return new String[] {"test"};
    }

    /**
     * @see DataHandler#supportedRoles()
     */
    @Override
    public DocumentRole[] supportedRoles() {
        return new DocumentRole[] {DocumentRole.SOURCE};
    }

    /**
     * @see DataHandler#setDocument(Object)
     */
    @Override
    public void setDocument(Object document) {
        if (!"test".equals(document)) {
            throw new AtlasRuntimeException("Must be 'test'");
        }
        super.setDocument(VerifyArgument.isInstanceOf("document", document, String.class));
    }

    /**
     * @see DataHandler#value(String)
     */
    @Override
    public Object value(String path) {
        return null;
    }

    /**
     * @see io.atlasmap.spi.v3.DataHandler#setValue(java.lang.String, java.lang.Object)
     */
    @Override
    public void setValue(String path, Object value) {
    }
}
